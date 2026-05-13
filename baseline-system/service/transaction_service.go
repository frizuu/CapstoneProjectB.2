package service

import (
	"baseline-system/legacy"
	"baseline-system/model"
	"baseline-system/repository"
	"database/sql"
	"fmt"
)

type TransactionService struct {
	DB           *sql.DB
	Repo         *repository.TransactionRepo
	UserRepo     *repository.UserRepo
	MerchantRepo *repository.MerchantRepo
	AuditRepo    *repository.AuditRepo
	LedgerRepo   *repository.LedgerRepo
}

type TransactionResult struct {
	Status        string `json:"status"`
	Code          string `json:"code,omitempty"`
	Message       string `json:"message,omitempty"`
	TransactionID int    `json:"transaction_id,omitempty"`
	AuditID       int    `json:"audit_id,omitempty"`
	HostReference string `json:"host_reference,omitempty"`
	NeedReversal  bool   `json:"need_reversal,omitempty"`
	LegacyProfile string `json:"legacy_profile,omitempty"`
	LegacyLatency int64  `json:"legacy_latency_ms,omitempty"`
}

func (s *TransactionService) Process(userID int, amount int) TransactionResult {
	req := &legacy.TransactionRequest{
		Type:   legacy.TypePayment,
		UserID: userID,
		Amount: amount,
	}
	return s.execute(req)
}

func (s *TransactionService) ProcessToMerchant(userID int, merchantID int, merchantCode string, amount int) TransactionResult {
	req := &legacy.TransactionRequest{
		Type:         legacy.TypePayment,
		UserID:       userID,
		MerchantCode: merchantCode,
		Amount:       amount,
	}
	return s.executeWithMerchantID(req, merchantID)
}

func (s *TransactionService) ProcessTransfer(userID int, recipientUserID int, amount int) TransactionResult {
	req := &legacy.TransactionRequest{
		Type:            legacy.TypeTransfer,
		UserID:          userID,
		RecipientUserID: recipientUserID,
		Amount:          amount,
	}
	return s.execute(req)
}

func (s *TransactionService) ProcessQRIS(userID int, merchantCode string, amount int) TransactionResult {
	req := &legacy.TransactionRequest{
		Type:         legacy.TypeQRIS,
		UserID:       userID,
		MerchantCode: merchantCode,
		Amount:       amount,
	}
	return s.execute(req)
}

func (s *TransactionService) execute(req *legacy.TransactionRequest) TransactionResult {
	return s.executeWithMerchantID(req, 0)
}

func (s *TransactionService) executeWithMerchantID(req *legacy.TransactionRequest, merchantIDInput int) TransactionResult {
	tx, err := s.DB.Begin()
	if err != nil {
		return TransactionResult{Status: legacy.StatusSystemBusy, Code: "91", Message: "unable to start transaction"}
	}
	defer func() {
		if err != nil {
			tx.Rollback()
		}
	}()

	user, err := s.UserRepo.GetForUpdate(tx, req.UserID)
	if err != nil {
		return TransactionResult{Status: legacy.StatusInvalidInput, Code: "14", Message: "user not found"}
	}

	var merchant *model.Merchant
	if req.Type == legacy.TypeQRIS || req.MerchantCode != "" {
		merchant, err = s.MerchantRepo.GetByCodeForUpdate(tx, req.MerchantCode)
		if err != nil {
			return TransactionResult{Status: legacy.StatusInvalidInput, Code: "15", Message: "merchant not found or inactive"}
		}
	} else if merchantIDInput > 0 {
		merchant, err = s.MerchantRepo.GetByIDForUpdate(tx, merchantIDInput)
		if err != nil {
			return TransactionResult{Status: legacy.StatusInvalidInput, Code: "15", Message: "merchant not found or inactive"}
		}
		req.MerchantCode = merchant.MerchantCode
	}

	var recipient *model.User
	if req.Type == legacy.TypeTransfer {
		if req.RecipientUserID == 0 {
			return TransactionResult{Status: legacy.StatusInvalidInput, Code: "14", Message: "recipient_user_id is required"}
		}
		if req.RecipientUserID == req.UserID {
			return TransactionResult{Status: legacy.StatusInvalidInput, Code: "12", Message: "sender and recipient cannot be the same user"}
		}
		recipient, err = s.UserRepo.GetForUpdate(tx, req.RecipientUserID)
		if err != nil {
			return TransactionResult{Status: legacy.StatusInvalidInput, Code: "14", Message: "recipient user not found"}
		}
	}

	result := legacy.ExecuteTransaction(req, user, merchant)

	audit := &model.AuditEntry{
		EventType:    "TRANSACTION",
		EventSubType: string(req.Type),
		ReferenceID:  req.UserID,
		Status:       result.Status,
		Message:      result.Message,
		Payload:      fmt.Sprintf("user=%d recipient_user=%d amount=%d merchant=%s ref=%s host_ref=%s profile=%s latency_ms=%d need_reversal=%t", req.UserID, req.RecipientUserID, req.Amount, req.MerchantCode, req.ReferenceNo, result.HostRef, result.Profile, result.LatencyMs, result.NeedReversal),
	}
	auditID, auditErr := s.AuditRepo.Record(tx, audit)
	if auditErr != nil {
		tx.Rollback()
		return TransactionResult{Status: legacy.StatusSystemBusy, Code: "96", Message: "failed to log audit"}
	}

	if result.Status != legacy.StatusSuccess {
		if commitErr := tx.Commit(); commitErr != nil {
			tx.Rollback()
			return TransactionResult{Status: legacy.StatusSystemBusy, Code: "91", Message: "failed to complete failed transaction path"}
		}
		return TransactionResult{
			Status:        result.Status,
			Code:          result.Code,
			Message:       result.Message,
			AuditID:       auditID,
			HostReference: result.HostRef,
			NeedReversal:  result.NeedReversal,
			LegacyProfile: result.Profile,
			LegacyLatency: result.LatencyMs,
		}
	}

	newUserBalance := user.Balance - req.Amount
	if err := s.UserRepo.UpdateBalanceWithTx(tx, user.ID, newUserBalance); err != nil {
		tx.Rollback()
		return TransactionResult{Status: legacy.StatusFailed, Code: "94", Message: "failed to debit user account"}
	}

	var merchantID int
	var newMerchantBalance int64
	if merchant != nil {
		newMerchantBalance = merchant.Balance + int64(req.Amount)
		if err := s.MerchantRepo.UpdateBalanceWithTx(tx, merchant.ID, newMerchantBalance); err != nil {
			tx.Rollback()
			return TransactionResult{Status: legacy.StatusFailed, Code: "95", Message: "failed to credit merchant account"}
		}
		merchantID = merchant.ID
	}

	var recipientUserID int
	if recipient != nil {
		if err := s.UserRepo.CreditBalanceWithTx(tx, recipient.ID, req.Amount); err != nil {
			tx.Rollback()
			return TransactionResult{Status: legacy.StatusFailed, Code: "95", Message: "failed to credit recipient user account"}
		}
		recipientUserID = recipient.ID
	}

	transaction := &model.Transaction{
		UserID:          user.ID,
		MerchantID:      merchantID,
		RecipientUserID: recipientUserID,
		Amount:          req.Amount,
		Status:          result.Status,
		TransactionType: string(req.Type),
	}
	transactionID, err := s.Repo.SaveWithTx(tx, transaction)
	if err != nil {
		tx.Rollback()
		return TransactionResult{Status: legacy.StatusFailed, Code: "94", Message: "failed to persist transaction"}
	}

	ledgerEntries := []model.LedgerEntry{
		{TransactionID: transactionID, AccountType: "USER", AccountID: user.ID, Direction: "DEBIT", Amount: int64(req.Amount), EntryType: string(req.Type)},
	}
	if merchant != nil {
		ledgerEntries = append(ledgerEntries, model.LedgerEntry{TransactionID: transactionID, AccountType: "MERCHANT", AccountID: merchant.ID, Direction: "CREDIT", Amount: int64(req.Amount), EntryType: string(req.Type)})
	}
	if recipient != nil {
		ledgerEntries = append(ledgerEntries, model.LedgerEntry{TransactionID: transactionID, AccountType: "USER", AccountID: recipient.ID, Direction: "CREDIT", Amount: int64(req.Amount), EntryType: string(req.Type)})
	}

	if err := s.LedgerRepo.PostEntries(tx, ledgerEntries); err != nil {
		tx.Rollback()
		return TransactionResult{Status: legacy.StatusFailed, Code: "94", Message: "failed to record ledger entries"}
	}

	if err := tx.Commit(); err != nil {
		tx.Rollback()
		return TransactionResult{Status: legacy.StatusSystemBusy, Code: "91", Message: "failed to commit transaction"}
	}

	return TransactionResult{
		Status:        result.Status,
		Code:          result.Code,
		Message:       result.Message,
		TransactionID: transactionID,
		AuditID:       auditID,
		HostReference: result.HostRef,
		NeedReversal:  result.NeedReversal,
		LegacyProfile: result.Profile,
		LegacyLatency: result.LatencyMs,
	}
}

func (s *TransactionService) GetUserTransactions(userID int) ([]map[string]interface{}, error) {
	return s.Repo.GetByUserID(userID)
}
