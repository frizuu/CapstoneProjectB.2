package main

import (
	"log"
	"os"

	amqp "github.com/rabbitmq/amqp091-go"
)

func main() {
	amqpURL := os.Getenv("RABBITMQ_URL")
	if amqpURL == "" {
		amqpURL = "amqp://guest:guest@localhost:5672/"
	}

	conn, err := amqp.Dial(amqpURL)
	if err != nil {
		log.Fatalf("Failed to connect: %v", err)
	}
	defer conn.Close()

	ch, err := conn.Channel()
	if err != nil {
		log.Fatalf("Failed to open channel: %v", err)
	}
	defer ch.Close()

	// Declare the same Fanout exchange
	err = ch.ExchangeDeclare("tx_events_exchange", "fanout", true, false, false, false, nil)

	// Create a dedicated queue for this prototype worker (e.g., Analytics/Logging)
	q, err := ch.QueueDeclare("analytics_queue", true, false, false, false, nil)

	// Bind the queue to the exchange
	err = ch.QueueBind(q.Name, "", "tx_events_exchange", false, nil)

	msgs, err := ch.Consume(q.Name, "", true, false, false, false, nil)

	log.Printf(" [*] Worker ready. Waiting for events...")

	forever := make(chan struct{})

	go func() {
		for d := range msgs {
			log.Printf(" [x] ASYNC TASK TRIGGERED: Received Transaction Payload: %s", d.Body)
			// Here you would implement:
			// - Inserting into an ElasticSearch logging index
			// - Sending a push notification
			// - Updating a real-time Looker Studio/Analytics dashboard
		}
	}()

	<-forever
}
