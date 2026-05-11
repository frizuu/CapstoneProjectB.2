package model

type AuditEntry struct {
	ID           int
	EventType    string
	EventSubType string
	ReferenceID  int
	Status       string
	Message      string
	Payload      string
	CreatedAt    string
}
