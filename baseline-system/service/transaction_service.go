package service

import (
	"baseline-system/legacy"
	"baseline-system/repository"
)

type TransactionService struct {
	Repo     *repository.TransactionRepo
	UserRepo *repository.UserRepo
}

func (s *TransactionService) Process(userID int, amount int) string {

	balance, err := s.UserRepo.GetBalance(userID)
	if err != nil {
		return "USER_NOT_FOUND"
	}

	// 2. Validasi saldo
	if balance < amount {
		return "INSUFFICIENT_BALANCE"
	}

	// 3. Call legacy (masih blocking)
	result := legacy.ProcessTransaction()

	// 4. Update saldo
	newBalance := balance - amount
	s.UserRepo.UpdateBalance(userID, newBalance)

	// 5. Simpan transaksi
	s.Repo.Save(userID, amount, result)

	return result
}
