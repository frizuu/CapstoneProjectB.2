package repository

import (
	"baseline-system/model"
	"database/sql"
)

type TransactionRepo struct {
	DB *sql.DB
}

func (r *TransactionRepo) SaveWithTx(tx *sql.Tx, t *model.Transaction) (int, error) {
	var id int
	err := tx.QueryRow(
		`INSERT INTO transactions(user_id, merchant_id, amount, status, transaction_type)
		 VALUES($1,$2,$3,$4,$5) RETURNING id`,
		t.UserID,
		t.MerchantID,
		t.Amount,
		t.Status,
		t.TransactionType,
	).Scan(&id)
	return id, err
}

func (r *TransactionRepo) GetByUserID(userID int) ([]map[string]interface{}, error) {
	rows, err := r.DB.Query(`
		SELECT id, user_id, merchant_id, amount, status, transaction_type, created_at
		FROM transactions
		WHERE user_id = $1
		ORDER BY created_at DESC
	`, userID)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var results []map[string]interface{}

	for rows.Next() {
		var id, uid, merchantID, amount int
		var status, transactionType, createdAt string

		err := rows.Scan(&id, &uid, &merchantID, &amount, &status, &transactionType, &createdAt)
		if err != nil {
			return nil, err
		}

		record := map[string]interface{}{
			"id":               id,
			"user_id":          uid,
			"merchant_id":      merchantID,
			"amount":           amount,
			"status":           status,
			"transaction_type": transactionType,
			"created_at":       createdAt,
		}

		results = append(results, record)
	}

	return results, nil
}
