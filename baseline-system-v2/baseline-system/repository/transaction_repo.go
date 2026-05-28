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

		ALTER TABLE public.transactions
		ADD COLUMN IF NOT EXISTS reference_no character varying(100);

		ALTER TABLE public.transactions
		ADD COLUMN IF NOT EXISTS reversal_of_transaction_id integer REFERENCES public.transactions(id);

		CREATE INDEX IF NOT EXISTS idx_transactions_recipient_user_id
		ON public.transactions(recipient_user_id);

		CREATE UNIQUE INDEX IF NOT EXISTS idx_transactions_reference_no
		ON public.transactions(reference_no)
		WHERE reference_no IS NOT NULL;

		CREATE INDEX IF NOT EXISTS idx_transactions_reversal_of_transaction_id
		ON public.transactions(reversal_of_transaction_id);
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
	var referenceNo sql.NullString
	var reversalOfTransactionID sql.NullInt64
	if t.ReferenceNo != "" {
		referenceNo = sql.NullString{String: t.ReferenceNo, Valid: true}
	}
	if t.ReversalOfTransactionID > 0 {
		reversalOfTransactionID = sql.NullInt64{Int64: int64(t.ReversalOfTransactionID), Valid: true}
	}

	err := tx.QueryRow(
		`INSERT INTO transactions(user_id, merchant_id, recipient_user_id, amount, status, transaction_type, reference_no, reversal_of_transaction_id)
		 VALUES($1,$2,$3,$4,$5,$6,$7,$8) RETURNING id`,
		t.UserID,
		merchantID,
		recipientUserID,
		t.Amount,
		t.Status,
		t.TransactionType,
		referenceNo,
		reversalOfTransactionID,
	).Scan(&id)
	return id, err
}

func (r *TransactionRepo) GetByReferenceNoWithTx(tx *sql.Tx, referenceNo string) (*model.Transaction, error) {
	t := &model.Transaction{}
	var merchantID, recipientUserID, reversalOfTransactionID sql.NullInt64
	var reference sql.NullString
	err := tx.QueryRow(
		`SELECT id, user_id, merchant_id, recipient_user_id, amount, status, transaction_type, reference_no, reversal_of_transaction_id, created_at
		 FROM transactions
		 WHERE reference_no = $1
		 FOR UPDATE`,
		referenceNo,
	).Scan(
		&t.ID,
		&t.UserID,
		&merchantID,
		&recipientUserID,
		&t.Amount,
		&t.Status,
		&t.TransactionType,
		&reference,
		&reversalOfTransactionID,
		&t.CreatedAt,
	)
	if err != nil {
		return nil, err
	}
	if merchantID.Valid {
		t.MerchantID = int(merchantID.Int64)
	}
	if recipientUserID.Valid {
		t.RecipientUserID = int(recipientUserID.Int64)
	}
	if reference.Valid {
		t.ReferenceNo = reference.String
	}
	if reversalOfTransactionID.Valid {
		t.ReversalOfTransactionID = int(reversalOfTransactionID.Int64)
	}
	return t, nil
}

func (r *TransactionRepo) GetByIDForUpdate(tx *sql.Tx, transactionID int) (*model.Transaction, error) {
	t := &model.Transaction{}
	var merchantID, recipientUserID, reversalOfTransactionID sql.NullInt64
	var reference sql.NullString
	err := tx.QueryRow(
		`SELECT id, user_id, merchant_id, recipient_user_id, amount, status, transaction_type, reference_no, reversal_of_transaction_id, created_at
		 FROM transactions
		 WHERE id = $1
		 FOR UPDATE`,
		transactionID,
	).Scan(
		&t.ID,
		&t.UserID,
		&merchantID,
		&recipientUserID,
		&t.Amount,
		&t.Status,
		&t.TransactionType,
		&reference,
		&reversalOfTransactionID,
		&t.CreatedAt,
	)
	if err != nil {
		return nil, err
	}
	if merchantID.Valid {
		t.MerchantID = int(merchantID.Int64)
	}
	if recipientUserID.Valid {
		t.RecipientUserID = int(recipientUserID.Int64)
	}
	if reference.Valid {
		t.ReferenceNo = reference.String
	}
	if reversalOfTransactionID.Valid {
		t.ReversalOfTransactionID = int(reversalOfTransactionID.Int64)
	}
	return t, nil
}

func (r *TransactionRepo) UpdateStatusWithTx(tx *sql.Tx, transactionID int, status string) error {
	_, err := tx.Exec(
		`UPDATE transactions SET status = $1 WHERE id = $2`,
		status,
		transactionID,
	)
	return err
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
			t.reference_no,
			t.reversal_of_transaction_id,
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
		var merchantID, recipientUserID, reversalOfTransactionID sql.NullInt64
		var senderName, merchantName, merchantCode, recipientName, status, transactionType, referenceNo sql.NullString
		var createdAt time.Time

		err := rows.Scan(&id, &uid, &senderName, &merchantID, &merchantName, &merchantCode, &recipientUserID, &recipientName, &amount, &status, &transactionType, &referenceNo, &reversalOfTransactionID, &createdAt)
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
		if referenceNo.Valid {
			record["reference_no"] = referenceNo.String
		} else {
			record["reference_no"] = nil
		}
		if reversalOfTransactionID.Valid {
			record["reversal_of_transaction_id"] = reversalOfTransactionID.Int64
		} else {
			record["reversal_of_transaction_id"] = nil
		}

		results = append(results, record)
	}

	return results, rows.Err()
}

func (r *TransactionRepo) GetStatusByID(transactionID int) (map[string]interface{}, error) {
	var id, userID, amount int
	var merchantID, recipientUserID, reversalOfTransactionID sql.NullInt64
	var merchantCode, status, transactionType, referenceNo sql.NullString
	var createdAt time.Time

	err := r.DB.QueryRow(`
		SELECT
			t.id,
			t.user_id,
			t.merchant_id,
			m.merchant_code,
			t.recipient_user_id,
			t.amount,
			t.status,
			t.transaction_type,
			t.reference_no,
			t.reversal_of_transaction_id,
			t.created_at
		FROM transactions t
		LEFT JOIN merchants m ON m.id = t.merchant_id
		WHERE t.id = $1
	`, transactionID).Scan(&id, &userID, &merchantID, &merchantCode, &recipientUserID, &amount, &status, &transactionType, &referenceNo, &reversalOfTransactionID, &createdAt)
	if err != nil {
		return nil, err
	}

	record := map[string]interface{}{
		"transaction_id": id,
		"user_id":        userID,
		"amount":         amount,
		"created_at":     createdAt,
	}
	if merchantID.Valid {
		record["merchant_id"] = merchantID.Int64
	} else {
		record["merchant_id"] = nil
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
	if referenceNo.Valid {
		record["reference_no"] = referenceNo.String
	} else {
		record["reference_no"] = nil
	}
	if reversalOfTransactionID.Valid {
		record["reversal_of_transaction_id"] = reversalOfTransactionID.Int64
	} else {
		record["reversal_of_transaction_id"] = nil
	}

	return record, nil
}
