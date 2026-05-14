package repository

import (
	"baseline-system/model"
	"database/sql"
)

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

func (r *UserRepo) GetByID(userID int) (*model.User, error) {
	user := &model.User{}
	err := r.DB.QueryRow(
		"SELECT id, name, balance FROM users WHERE id=$1",
		userID,
	).Scan(&user.ID, &user.Name, &user.Balance)
	if err != nil {
		return nil, err
	}

	return user, nil
}

func (r *UserRepo) GetForUpdate(tx *sql.Tx, userID int) (*model.User, error) {
	user := &model.User{}
	err := tx.QueryRow(
		"SELECT id, name, balance FROM users WHERE id=$1 FOR UPDATE",
		userID,
	).Scan(&user.ID, &user.Name, &user.Balance)
	if err != nil {
		return nil, err
	}

	return user, nil
}

func (r *UserRepo) UpdateBalanceWithTx(tx *sql.Tx, userID int, newBalance int) error {
	_, err := tx.Exec(
		"UPDATE users SET balance=$1 WHERE id=$2",
		newBalance, userID,
	)
	return err
}

func (r *UserRepo) DebitBalanceWithTx(tx *sql.Tx, userID int, amount int) (bool, error) {
	result, err := tx.Exec(
		"UPDATE users SET balance = balance - $1 WHERE id=$2 AND balance >= $1",
		amount, userID,
	)
	if err != nil {
		return false, err
	}

	rowsAffected, err := result.RowsAffected()
	if err != nil {
		return false, err
	}

	return rowsAffected > 0, nil
}

func (r *UserRepo) CreditBalanceWithTx(tx *sql.Tx, userID int, amount int) error {
	_, err := tx.Exec(
		"UPDATE users SET balance = balance + $1 WHERE id=$2",
		amount, userID,
	)
	return err
}
