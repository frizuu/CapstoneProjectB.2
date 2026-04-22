package repository

import "database/sql"

type UserRepo struct {
	DB *sql.DB
}

func (r *UserRepo) GetBalance(userID int) (int, error) {
	var balance int
	err := r.DB.QueryRow(
		"SELECT balance FROM users WHERE id=$1",
		userID,
	).Scan(&balance)

	return balance, err
}

func (r *UserRepo) UpdateBalance(userID int, newBalance int) error {
	_, err := r.DB.Exec(
		"UPDATE users SET balance=$1 WHERE id=$2",
		newBalance, userID,
	)
	return err
}
