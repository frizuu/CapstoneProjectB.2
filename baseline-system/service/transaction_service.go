package service

import (
	"baseline-system/cache"
	"baseline-system/legacy"
	"baseline-system/messaging"
	"baseline-system/repository"
	"database/sql"
	"fmt"
	"strconv"
	"time"

	amqp "github.com/rabbitmq/amqp091-go"
)

type TransactionService struct {
	DB           *sql.DB
	Repo         *repository.TransactionRepo
	UserRepo     *repository.UserRepo
	MerchantRepo *repository.MerchantRepo
	Cache        *cache.RedisCache
	RabbitMQ     *amqp.Channel // Injected RabbitMQ Channel
}

// Process - pembayaran umum (existing)
func (s *TransactionService) Process(userID int, amount int) string {

	// READ: cek saldo dari cache dulu
	balance, err := s.getUserBalanceWithCache(userID)
	if err != nil {
		return "USER_NOT_FOUND"
	}

	if balance < amount {
		return "INSUFFICIENT_BALANCE"
	}

	// WRITE: Start Transaction
	tx, err := s.DB.Begin()
	if err != nil {
		return "FAILED_START_TRANSACTION"
	}

	result := legacy.ProcessTransaction()
	newBalance := balance - amount

	// Execute Write Query 1: update saldo
	_, err = tx.Exec(
		"UPDATE users SET balance=$1 WHERE id=$2",
		newBalance, userID,
	)
	if err != nil {
		tx.Rollback()
		return "FAILED"
	}

	// Execute Write Query 2: simpan transaksi
	_, err = tx.Exec(
		"INSERT INTO transactions(user_id, amount, status) VALUES($1,$2,$3)",
		userID, amount, result,
	)
	if err != nil {
		tx.Rollback()
		return "FAILED"
	}

	// Commit
	if err := tx.Commit(); err != nil {
		tx.Rollback()
		return "FAILED"
	}

	// Update Cache setelah commit berhasil
	s.Cache.Set(
		cache.KeyUserBalance(userID),
		strconv.Itoa(newBalance),
		10*time.Minute,
	)

	// ---> ASYNC DECOUPLING: Broadcast standard payment event
	if s.RabbitMQ != nil {
		go messaging.PublishTransactionEvent(s.RabbitMQ, messaging.EventPayload{
			TransactionID: fmt.Sprintf("TX-REG-%d-%d", userID, time.Now().Unix()),
			UserID:        userID,
			Amount:        amount,
			Status:        result,
			Timestamp:     time.Now().Format(time.RFC3339),
		})
	}

	return result
}

// ProcessQRIS - pembayaran via QRIS
func (s *TransactionService) ProcessQRIS(userID int, merchantCode string, amount int) string {

	// READ: cek saldo user dari cache dulu
	userBalance, err := s.getUserBalanceWithCache(userID)
	if err != nil {
		return "USER_NOT_FOUND"
	}

	if userBalance < amount {
		return "INSUFFICIENT_BALANCE"
	}

	// READ: inquiry merchant
	merchant, err := s.MerchantRepo.GetByCode(merchantCode)
	if err != nil {
		return "MERCHANT_NOT_FOUND"
	}

	// Call legacy processor
	result := legacy.ProcessTransaction()
	if result != "SUCCESS" {
		return result
	}

	// WRITE: Start Transaction
	tx, err := s.DB.Begin()
	if err != nil {
		return "FAILED_START_TRANSACTION"
	}

	// Execute Write Query 1: Debit saldo user
	newUserBalance := userBalance - amount
	_, err = tx.Exec(
		"UPDATE users SET balance=$1 WHERE id=$2",
		newUserBalance, userID,
	)
	if err != nil {
		tx.Rollback()
		return "FAILED_DEBIT_USER"
	}

	// Execute Write Query 2: Kredit saldo merchant
	newMerchantBalance := merchant.Balance + int64(amount)
	_, err = tx.Exec(
		"UPDATE merchants SET balance=$1 WHERE id=$2",
		newMerchantBalance, merchant.ID,
	)
	if err != nil {
		tx.Rollback()
		return "FAILED_CREDIT_MERCHANT"
	}

	// Execute Write Query 3: Simpan transaksi
	_, err = tx.Exec(
		"INSERT INTO transactions(user_id, amount, status) VALUES($1,$2,$3)",
		userID, amount, result,
	)
	if err != nil {
		tx.Rollback()
		return "FAILED_SAVE_TRANSACTION"
	}

	// Commit semua sekaligus
	if err := tx.Commit(); err != nil {
		tx.Rollback()
		return "FAILED_COMMIT"
	}

	// Update Cache setelah commit berhasil
	s.Cache.Set(
		cache.KeyUserBalance(userID),
		strconv.Itoa(newUserBalance),
		10*time.Minute,
	)
	s.Cache.Set(
		cache.KeyMerchantBalance(merchant.ID),
		fmt.Sprintf("%d", newMerchantBalance),
		10*time.Minute,
	)

	// ---> ASYNC DECOUPLING: Broadcast QRIS payment event
	if s.RabbitMQ != nil {
		go messaging.PublishTransactionEvent(s.RabbitMQ, messaging.EventPayload{
			TransactionID: fmt.Sprintf("TX-QRIS-%d-%d", userID, time.Now().Unix()),
			UserID:        userID,
			MerchantCode:  merchantCode,
			Amount:        amount,
			Status:        result,
			Timestamp:     time.Now().Format(time.RFC3339),
		})
	}

	return result
}

// getUserBalanceWithCache - READ dengan cache
func (s *TransactionService) getUserBalanceWithCache(userID int) (int, error) {
	// Cek cache dulu
	cached, err := s.Cache.Get(cache.KeyUserBalance(userID))
	if err == nil {
		balance, err := strconv.Atoi(cached)
		if err == nil {
			return balance, nil
		}
	}

	// Cache miss - ambil dari DB
	balance, err := s.UserRepo.GetBalance(userID)
	if err != nil {
		return 0, err
	}

	// Simpan ke cache
	s.Cache.Set(
		cache.KeyUserBalance(userID),
		strconv.Itoa(balance),
		10*time.Minute,
	)

	return balance, nil
}

func (s *TransactionService) GetUserTransactions(userID int) ([]map[string]interface{}, error) {
	return s.Repo.GetByUserID(userID)
}
