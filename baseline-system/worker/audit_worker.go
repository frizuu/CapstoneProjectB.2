package worker

import (
	"baseline-system/messaging"
	"baseline-system/model"
	"baseline-system/repository"
	"encoding/json"
	"log"

	amqp "github.com/rabbitmq/amqp091-go"
)

func StartAuditWorker(ch *amqp.Channel, auditRepo *repository.AuditRepo) {
	q, err := ch.QueueDeclare(
		"audit_logs_queue", true, false, false, false, nil,
	)
	if err != nil {
		log.Fatalf("Failed to declare a queue: %s", err)
	}

	msgs, err := ch.Consume(
		q.Name, "", true, false, false, false, nil,
	)
	if err != nil {
		log.Fatalf("Failed to register a consumer: %s", err)
	}

	log.Println("[*] Audit Worker started. Waiting for logs...")

	// Run in the background
	go func() {
		for d := range msgs {
			var payload messaging.AuditPayload
			if err := json.Unmarshal(d.Body, &payload); err != nil {
				log.Printf("Error decoding audit message: %s", err)
				continue
			}

			// Map payload to database model
			entry := &model.AuditEntry{
				EventType:    payload.EventType,
				EventSubType: payload.EventSubType,
				ReferenceID:  payload.ReferenceID,
				Status:       payload.Status,
				Message:      payload.Message,
				Payload:      payload.Payload,
			}

			// Save to database synchronously inside the worker
			_, err := auditRepo.RecordNoTx(entry)
			if err != nil {
				log.Printf("Failed to save audit log to DB: %v", err)
			} else {
				log.Printf("Saved async audit log for ReferenceID: %d", entry.ReferenceID)
			}
		}
	}()
}
