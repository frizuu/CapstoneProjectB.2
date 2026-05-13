package repository

import (
	"baseline-system/model"
	"database/sql"
	"time"
)

type TransactionRepo struct {
	DB *sql.DB
}

func (r *TransactionRepo) EnsureSchema() error {
	_, err := r.DB.Exec(`
		ALTER TABLE public.transactions
		ADD COLUMN IF NOT EXISTS recipient_user_id integer REFERENCES public.users(id);

		CREATE INDEX IF NOT EXISTS idx_transactions_recipient_user_id
		ON public.transactions(recipient_user_id);
	`)
	return err
}

func (r *TransactionRepo) SaveWithTx(tx *sql.Tx, t *model.Transaction) (int, error) {
	var id int
	var merchantID sql.NullInt64
	var recipientUserID sql.NullInt64
	if t.MerchantID > 0 {
		merchantID = sql.NullInt64{Int64: int64(t.MerchantID), Valid: true}
	}
	if t.RecipientUserID > 0 {
		recipientUserID = sql.NullInt64{Int64: int64(t.RecipientUserID), Valid: true}
	}

	err := tx.QueryRow(
		`INSERT INTO transactions(user_id, merchant_id, recipient_user_id, amount, status, transaction_type)
		 VALUES($1,$2,$3,$4,$5,$6) RETURNING id`,
		t.UserID,
		merchantID,
		recipientUserID,
		t.Amount,
		t.Status,
		t.TransactionType,
	).Scan(&id)
	return id, err
}

func (r *TransactionRepo) GetByUserID(userID int) ([]map[string]interface{}, error) {
	rows, err := r.DB.Query(`
		SELECT
			t.id,
			t.user_id,
			sender.name,
			t.merchant_id,
			m.name,
			m.merchant_code,
			t.recipient_user_id,
			recipient.name,
			t.amount,
			t.status,
			t.transaction_type,
			t.created_at
		FROM transactions t
		LEFT JOIN users sender ON sender.id = t.user_id
		LEFT JOIN merchants m ON m.id = t.merchant_id
		LEFT JOIN users recipient ON recipient.id = t.recipient_user_id
		WHERE t.user_id = $1 OR t.recipient_user_id = $1
		ORDER BY t.created_at DESC
	`, userID)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var results []map[string]interface{}

	for rows.Next() {
		var id, uid, amount int
		var merchantID, recipientUserID sql.NullInt64
		var senderName, merchantName, merchantCode, recipientName, status, transactionType sql.NullString
		var createdAt time.Time

		err := rows.Scan(&id, &uid, &senderName, &merchantID, &merchantName, &merchantCode, &recipientUserID, &recipientName, &amount, &status, &transactionType, &createdAt)
		if err != nil {
			return nil, err
		}

		record := map[string]interface{}{
			"id":         id,
			"user_id":    uid,
			"amount":     amount,
			"created_at": createdAt,
		}
		if uid == userID {
			record["direction"] = "OUT"
		} else {
			record["direction"] = "IN"
		}
		if senderName.Valid {
			record["sender_name"] = senderName.String
		} else {
			record["sender_name"] = nil
		}
		if merchantID.Valid {
			record["merchant_id"] = merchantID.Int64
		} else {
			record["merchant_id"] = nil
		}
		if merchantName.Valid {
			record["merchant_name"] = merchantName.String
		} else {
			record["merchant_name"] = nil
		}
		if merchantCode.Valid {
			record["merchant_code"] = merchantCode.String
		} else {
			record["merchant_code"] = nil
		}
		if recipientUserID.Valid {
			record["recipient_user_id"] = recipientUserID.Int64
		} else {
			record["recipient_user_id"] = nil
		}
		if recipientName.Valid {
			record["recipient_user_name"] = recipientName.String
		} else {
			record["recipient_user_name"] = nil
		}
		if status.Valid {
			record["status"] = status.String
		} else {
			record["status"] = nil
		}
		if transactionType.Valid {
			record["transaction_type"] = transactionType.String
		} else {
			record["transaction_type"] = nil
		}

		results = append(results, record)
	}

	return results, rows.Err()
}
