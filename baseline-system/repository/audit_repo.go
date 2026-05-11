package repository

import (
	"baseline-system/model"
	"database/sql"
)

type AuditRepo struct {
	DB *sql.DB
}

func (r *AuditRepo) Record(tx *sql.Tx, audit *model.AuditEntry) (int, error) {
	var id int
	err := tx.QueryRow(
		`INSERT INTO audit_logs(event_type, event_sub_type, reference_id, status, message, payload)
		 VALUES($1,$2,$3,$4,$5,$6) RETURNING id`,
		audit.EventType,
		audit.EventSubType,
		audit.ReferenceID,
		audit.Status,
		audit.Message,
		audit.Payload,
	).Scan(&id)
	return id, err
}
