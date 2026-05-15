package messaging

import (
	"context"
	"encoding/json"
	"log"
	"time"

	amqp "github.com/rabbitmq/amqp091-go"
)

type EventPayload struct {
	TransactionID string `json:"transaction_id"`
	UserID        int    `json:"user_id"`
	MerchantCode  string `json:"merchant_code,omitempty"`
	Amount        int    `json:"amount"`
	Status        string `json:"status"`
	Timestamp     string `json:"timestamp"`
}

func ConnectRabbitMQ(url string) (*amqp.Connection, *amqp.Channel) {
	conn, err := amqp.Dial(url)
	if err != nil {
		log.Fatalf("Failed to connect to RabbitMQ: %v", err)
	}

	ch, err := conn.Channel()
	if err != nil {
		log.Fatalf("Failed to open a channel: %v", err)
	}

	// Setup a Fanout Exchange for prototype scope
	// This will broadcast the transaction event to all bound queues (Logging, Analytics, etc.)
	err = ch.ExchangeDeclare(
		"tx_events_exchange", // name
		"fanout",             // type
		true,                 // durable
		false,                // auto-deleted
		false,                // internal
		false,                // no-wait
		nil,                  // arguments
	)
	if err != nil {
		log.Fatalf("Failed to declare exchange: %v", err)
	}

	return conn, ch
}

func PublishTransactionEvent(ch *amqp.Channel, payload EventPayload) {
	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel()

	body, _ := json.Marshal(payload)

	err := ch.PublishWithContext(ctx,
		"tx_events_exchange", // exchange
		"",                   // routing key (ignored for fanout)
		false,                // mandatory
		false,                // immediate
		amqp.Publishing{
			ContentType: "application/json",
			Body:        body,
		})

	if err != nil {
		log.Printf("Failed to publish a message: %v", err)
	}
}
