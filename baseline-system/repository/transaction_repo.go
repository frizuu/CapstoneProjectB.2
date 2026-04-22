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