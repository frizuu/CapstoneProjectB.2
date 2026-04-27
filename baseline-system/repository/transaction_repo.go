package repository

import "database/sql"

type TransactionRepo struct {
	DB *sql.DB
}

func (r *TransactionRepo) Save(userID int, amount int, status string) error {
	_, err := r.DB.Exec(
		"INSERT INTO transactions(user_id, amount, status) VALUES($1,$2,$3)",
		userID, amount, status,
	)
	return err
}

func (r *TransactionRepo) GetByUserID(userID int) ([]map[string]interface{}, error) {
	rows, err := r.DB.Query(`
		SELECT id, user_id, amount, status, created_at
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
		var id, uid, amount int
		var status string
		var createdAt string

		err := rows.Scan(&id, &uid, &amount, &status, &createdAt)
		if err != nil {
			return nil, err
		}

		results = append(results, map[string]interface{}{
			"id":         id,
			"user_id":    uid,
			"amount":     amount,
			"status":     status,
			"created_at": createdAt,
		})
	}

	return results, nil
}
