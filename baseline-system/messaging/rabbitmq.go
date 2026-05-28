package messaging

import (
	"context"
	"encoding/json"
	"log"
	"time"

	amqp "github.com/rabbitmq/amqp091-go"
)

// ==========================================
// 1. TRANSACTION NOTIFICATION EVENTS
// ==========================================

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

// ==========================================
// 2. NEW AUDIT BACKGROUND WORKER EVENTS
// ==========================================

// AuditPayload represents the asynchronous message for the background worker
type AuditPayload struct {
	EventType    string `json:"event_type"`
	EventSubType string `json:"event_sub_type"`
	ReferenceID  int    `json:"reference_id"`
	Status       string `json:"status"`
	Message      string `json:"message"`
	Payload      string `json:"payload"`
	CreatedAt    string `json:"created_at"`
}

// PublishAuditEvent sends the audit log directly to the worker queue
func PublishAuditEvent(ch *amqp.Channel, audit AuditPayload) {
	if ch == nil {
		log.Println("RabbitMQ channel is nil, skipping audit publish")
		return
	}

	// Ensure the queue exists specifically for audits
	q, err := ch.QueueDeclare(
		"audit_logs_queue", // name
		true,               // durable (survives RabbitMQ restarts)
		false,              // delete when unused
		false,              // exclusive
		false,              // no-wait
		nil,                // arguments
	)
	if err != nil {
		log.Printf("Failed to declare an audit queue: %v", err)
		return
	}

	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel()

	body, _ := json.Marshal(audit)

	// Publish directly to the audit queue (using the default empty exchange)
	err = ch.PublishWithContext(ctx,
		"",     // default exchange
		q.Name, // routing key (matches queue name exactly)
		false,  // mandatory
		false,  // immediate
		amqp.Publishing{
			ContentType:  "application/json",
			DeliveryMode: amqp.Persistent, // Important: Save to disk so audit logs are never lost
			Body:         body,
		})

	if err != nil {
		log.Printf("Failed to publish audit event: %v", err)
	}
}
