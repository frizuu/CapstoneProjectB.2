package service

import (
	"baseline-system/legacy"
	"baseline-system/model"
	"baseline-system/repository"
	"database/sql"
	"encoding/json"
	"fmt"
	"time"
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
	ReferenceNo   string `json:"reference_no,omitempty"`
	AuditID       int    `json:"audit_id,omitempty"`
	NeedReversal  bool   `json:"need_reversal,omitempty"`
	Idempotent    bool   `json:"idempotent,omitempty"`
	LegacyProfile string `json:"legacy_profile,omitempty"`
	LegacyLatency int64  `json:"legacy_latency_ms,omitempty"`
}

type BalanceInquiryResult struct {
	Status        string `json:"status"`
	Code          string `json:"code,omitempty"`
	Message       string `json:"message,omitempty"`
	UserID        int    `json:"user_id"`
	Balance       int    `json:"balance"`
	AuditID       int    `json:"audit_id,omitempty"`
	LegacyProfile string `json:"legacy_profile,omitempty"`
	LegacyLatency int64  `json:"legacy_latency_ms,omitempty"`
}

type MerchantBalanceInquiryResult struct {
	Status        string `json:"status"`
	Code          string `json:"code,omitempty"`
	Message       string `json:"message,omitempty"`
	MerchantID    int    `json:"merchant_id"`
	MerchantCode  string `json:"merchant_code,omitempty"`
	Balance       int64  `json:"balance"`
	AuditID       int    `json:"audit_id,omitempty"`
	LegacyProfile string `json:"legacy_profile,omitempty"`
	LegacyLatency int64  `json:"legacy_latency_ms,omitempty"`
}

type MerchantInquiryResult struct {
	Status        string `json:"status"`
	Code          string `json:"code,omitempty"`
	Message       string `json:"message,omitempty"`
	MerchantID    int    `json:"merchant_id,omitempty"`
	MerchantName  string `json:"merchant_name,omitempty"`
	MerchantCode  string `json:"merchant_code,omitempty"`
	Category      string `json:"category,omitempty"`
	AuditID       int    `json:"audit_id,omitempty"`
	LegacyProfile string `json:"legacy_profile,omitempty"`
	LegacyLatency int64  `json:"legacy_latency_ms,omitempty"`
}

type TransactionHistoryResult struct {
	Status        string                   `json:"status"`
	Code          string                   `json:"code,omitempty"`
	Message       string                   `json:"message,omitempty"`
	UserID        int                      `json:"user_id,omitempty"`
	Transactions  []map[string]interface{} `json:"transactions,omitempty"`
	AuditID       int                      `json:"audit_id,omitempty"`
	LegacyProfile string                   `json:"legacy_profile,omitempty"`
	LegacyLatency int64                    `json:"legacy_latency_ms,omitempty"`
}

type TransactionStatusResult struct {
	Status        string                 `json:"status"`
	Code          string                 `json:"code,omitempty"`
	Message       string                 `json:"message,omitempty"`
	Data          map[string]interface{} `json:"data,omitempty"`
	AuditID       int                    `json:"audit_id,omitempty"`
	LegacyProfile string                 `json:"legacy_profile,omitempty"`
	LegacyLatency int64                  `json:"legacy_latency_ms,omitempty"`
}

type balanceCacheItem struct {
	Balance int `json:"balance"`
}

// merchantBalanceCacheItem digunakan untuk menyimpan saldo merchant di cache
type merchantBalanceCacheItem struct {
	Balance int64 `json:"balance"`
}

// merchantInfoCacheItem digunakan untuk menyimpan data merchant di cache
type merchantInfoCacheItem struct {
	MerchantID   int    `json:"merchant_id"`
	MerchantName string `json:"merchant_name"`
	MerchantCode string `json:"merchant_code"`
	Category     string `json:"category"`
}

// txStatusCacheItem digunakan untuk menyimpan status transaksi di cache
type txStatusCacheItem struct {
	Data map[string]interface{} `json:"data"`
}

// TTL khusus: status transaksi lebih pendek karena bisa berubah (misal PENDING → SUCCESS)
const txStatusCacheTTL = 30 * time.Second

func (s *TransactionService) Process(userID int, amount int, referenceNo string) TransactionResult {
	req := &legacy.TransactionRequest{
		Type:        legacy.TypePayment,
		ReferenceNo: referenceNo,
		UserID:      userID,
		Amount:      amount,
	}
	return s.execute(req)
}

func (s *TransactionService) MerchantInquiry(merchantCode string) MerchantInquiryResult {
	// --- Cache L1/Redis: cek merchant info sebelum ke DB & legacy ---
	cacheKey := fmt.Sprintf("merchant_info:%s", merchantCode)
	if cached, ok := GetFromCache(cacheKey); ok {
		var item merchantInfoCacheItem
		if err := json.Unmarshal([]byte(cached), &item); err == nil {
			return MerchantInquiryResult{
				Status:        legacy.StatusSuccess,
				Code:          "00",
				Message:       "Merchant inquiry retrieved from cache",
				MerchantID:    item.MerchantID,
				MerchantName:  item.MerchantName,
				MerchantCode:  item.MerchantCode,
				Category:      item.Category,
				LegacyProfile: "cache",
				LegacyLatency: 0,
			}
		}
	}

	start := time.Now()
	req := &legacy.TransactionRequest{
		Type:         legacy.TypeMerchant,
		MerchantCode: merchantCode,
	}
	legacy.PrepareInquiryRequest(req)
	profile, status := legacy.ExecuteInquiryHost(req.Type, "VALIDATION")

	result := MerchantInquiryResult{
		Status:        status,
		Code:          "00",
		Message:       "Merchant inquiry successful",
		LegacyProfile: profile,
		LegacyLatency: time.Since(start).Milliseconds(),
	}
	if status != legacy.StatusSuccess {
		result.Code = "96"
		result.Message = "legacy core returned " + status
		result.AuditID = s.recordInquiryAudit(req.Type, 0, result.Status, result.Message, fmt.Sprintf("merchant_code=%s ref=%s profile=%s", merchantCode, req.ReferenceNo, profile))
		return result
	}

	merchant, err := s.MerchantRepo.GetByCode(merchantCode)
	if err != nil {
		result.Status = legacy.StatusInvalidInput
		result.Code = "15"
		result.Message = "merchant not found or inactive"
		result.AuditID = s.recordInquiryAudit(req.Type, 0, result.Status, result.Message, fmt.Sprintf("merchant_code=%s ref=%s profile=%s", merchantCode, req.ReferenceNo, profile))
		return result
	}

	result.MerchantID = merchant.ID
	result.MerchantName = merchant.Name
	result.MerchantCode = merchant.MerchantCode
	result.Category = merchant.Category
	result.LegacyLatency = time.Since(start).Milliseconds()
	result.AuditID = s.recordInquiryAudit(req.Type, merchant.ID, result.Status, result.Message, fmt.Sprintf("merchant_code=%s ref=%s profile=%s", merchantCode, req.ReferenceNo, profile))

	// Simpan ke cache setelah berhasil mendapat data merchant
	cacheItem := merchantInfoCacheItem{
		MerchantID:   merchant.ID,
		MerchantName: merchant.Name,
		MerchantCode: merchant.MerchantCode,
		Category:     merchant.Category,
	}
	if data, err := json.Marshal(cacheItem); err == nil {
		SetCache(cacheKey, string(data), defaultCacheTTL)
	}

	return result
}

func (s *TransactionService) BalanceInquiry(userID int) BalanceInquiryResult {
	key := fmt.Sprintf("balance:%d", userID)
	if cached, ok := GetFromCache(key); ok {
		var item balanceCacheItem
		if err := json.Unmarshal([]byte(cached), &item); err == nil {
			return BalanceInquiryResult{
				Status:        legacy.StatusSuccess,
				Code:          "00",
				Message:       "Balance retrieved from cache",
				UserID:        userID,
				Balance:       item.Balance,
				LegacyProfile: "cache",
				LegacyLatency: 0,
			}
		}
	}

	user, err := s.UserRepo.GetByID(userID)
	if err != nil {
		return BalanceInquiryResult{Status: legacy.StatusInvalidInput, Code: "14", Message: "user not found"}
	}

	req := &legacy.TransactionRequest{
		Type:   legacy.TypeBalance,
		UserID: userID,
	}
	result := legacy.ExecuteBalanceInquiry(req, user)

	audit := &model.AuditEntry{
		EventType:    "INQUIRY",
		EventSubType: legacy.TypeBalance,
		ReferenceID:  userID,
		Status:       result.Status,
		Message:      result.Message,
		Payload:      fmt.Sprintf("user=%d ref=%s profile=%s latency_ms=%d", userID, req.ReferenceNo, result.Profile, result.LatencyMs),
	}
	auditID, _ := s.AuditRepo.RecordNoTx(audit)

	response := BalanceInquiryResult{
		Status:        result.Status,
		Code:          result.Code,
		Message:       result.Message,
		UserID:        userID,
		AuditID:       auditID,
		LegacyProfile: result.Profile,
		LegacyLatency: result.LatencyMs,
	}
	if result.Status == legacy.StatusSuccess {
		response.Balance = result.Balance
		cacheItem := balanceCacheItem{Balance: result.Balance}
		if data, err := json.Marshal(cacheItem); err == nil {
			SetCache(key, string(data), defaultCacheTTL)
		}
	}

	return response
}

func (s *TransactionService) MerchantBalanceInquiry(merchantID int) MerchantBalanceInquiryResult {
	// --- Cache L1/Redis: cek saldo merchant sebelum ke DB & legacy ---
	cacheKey := fmt.Sprintf("merchant_balance:%d", merchantID)
	if cached, ok := GetFromCache(cacheKey); ok {
		var item merchantBalanceCacheItem
		if err := json.Unmarshal([]byte(cached), &item); err == nil {
			return MerchantBalanceInquiryResult{
				Status:        legacy.StatusSuccess,
				Code:          "00",
				Message:       "Merchant balance retrieved from cache",
				MerchantID:    merchantID,
				Balance:       item.Balance,
				LegacyProfile: "cache",
				LegacyLatency: 0,
			}
		}
	}

	merchant, err := s.MerchantRepo.GetByID(merchantID)
	if err != nil {
		return MerchantBalanceInquiryResult{Status: legacy.StatusInvalidInput, Code: "15", Message: "merchant not found"}
	}

	req := &legacy.TransactionRequest{
		Type:         legacy.TypeMerchantBalance,
		MerchantCode: merchant.MerchantCode,
	}
	result := legacy.ExecuteMerchantBalanceInquiry(req, merchant)

	audit := &model.AuditEntry{
		EventType:    "INQUIRY",
		EventSubType: legacy.TypeMerchantBalance,
		ReferenceID:  merchant.ID,
		Status:       result.Status,
		Message:      result.Message,
		Payload:      fmt.Sprintf("merchant=%d merchant_code=%s ref=%s profile=%s latency_ms=%d", merchant.ID, merchant.MerchantCode, req.ReferenceNo, result.Profile, result.LatencyMs),
	}
	auditID, _ := s.AuditRepo.RecordNoTx(audit)

	response := MerchantBalanceInquiryResult{
		Status:        result.Status,
		Code:          result.Code,
		Message:       result.Message,
		MerchantID:    merchant.ID,
		MerchantCode:  merchant.MerchantCode,
		AuditID:       auditID,
		LegacyProfile: result.Profile,
		LegacyLatency: result.LatencyMs,
	}
	if result.Status == legacy.StatusSuccess {
		response.Balance = result.Balance
		// Simpan ke cache setelah berhasil mendapat saldo merchant
		cacheItem := merchantBalanceCacheItem{Balance: result.Balance}
		if data, err := json.Marshal(cacheItem); err == nil {
			SetCache(cacheKey, string(data), defaultCacheTTL)
		}
	}

	return response
}

func (s *TransactionService) ProcessToMerchant(userID int, merchantID int, merchantCode string, amount int, referenceNo string) TransactionResult {
	req := &legacy.TransactionRequest{
		Type:         legacy.TypePayment,
		ReferenceNo:  referenceNo,
		UserID:       userID,
		MerchantCode: merchantCode,
		Amount:       amount,
	}
	return s.executeWithMerchantID(req, merchantID)
}

func (s *TransactionService) ProcessTransfer(userID int, recipientUserID int, amount int, referenceNo string) TransactionResult {
	req := &legacy.TransactionRequest{
		Type:            legacy.TypeTransfer,
		ReferenceNo:     referenceNo,
		UserID:          userID,
		RecipientUserID: recipientUserID,
		Amount:          amount,
	}
	return s.execute(req)
}

func (s *TransactionService) ProcessQRIS(userID int, merchantCode string, amount int, referenceNo string) TransactionResult {
	req := &legacy.TransactionRequest{
		Type:         legacy.TypeQRIS,
		ReferenceNo:  referenceNo,
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
	legacy.PrepareInquiryRequest(req)

	// 1. Ambil data user secara read-only (tanpa lock) untuk keperluan inquiry/validasi legacy
	user, err := s.UserRepo.GetByID(req.UserID)
	if err != nil {
		return TransactionResult{Status: legacy.StatusInvalidInput, Code: "14", Message: "user not found"}
	}

	// 2. Ambil data merchant secara read-only (tanpa lock)
	var merchant *model.Merchant
	if req.Type == legacy.TypeQRIS || req.MerchantCode != "" {
		merchant, err = s.MerchantRepo.GetByCode(req.MerchantCode)
		if err != nil {
			return TransactionResult{Status: legacy.StatusInvalidInput, Code: "15", Message: "merchant not found or inactive"}
		}
	} else if merchantIDInput > 0 {
		merchant, err = s.MerchantRepo.GetByID(merchantIDInput)
		if err != nil {
			return TransactionResult{Status: legacy.StatusInvalidInput, Code: "15", Message: "merchant not found or inactive"}
		}
		req.MerchantCode = merchant.MerchantCode
	}

	// 3. Ambil data recipient secara read-only (tanpa lock) jika tipe transfer
	var recipient *model.User
	if req.Type == legacy.TypeTransfer {
		if req.RecipientUserID == 0 {
			return TransactionResult{Status: legacy.StatusInvalidInput, Code: "14", Message: "recipient_user_id is required"}
		}
		if req.RecipientUserID == req.UserID {
			return TransactionResult{Status: legacy.StatusInvalidInput, Code: "12", Message: "sender and recipient cannot be the same user"}
		}
		recipient, err = s.UserRepo.GetByID(req.RecipientUserID)
		if err != nil {
			return TransactionResult{Status: legacy.StatusInvalidInput, Code: "14", Message: "recipient user not found"}
		}
	}

	// 4. Panggil Legacy Monolithic Host (yang lambat & blocking, tidur 1.2-1.5 detik)
	//    Pemanggilan dilakukan di LUAR transaksi database agar tidak menahan lock baris!
	result := legacy.ExecuteTransaction(req, user, merchant)

	// 5. Setelah respon dari host legacy didapat, buka transaksi database yang super singkat
	tx, err := s.DB.Begin()
	if err != nil {
		return TransactionResult{Status: legacy.StatusSystemBusy, Code: "91", Message: "unable to start database transaction"}
	}
	defer func() {
		if err != nil {
			tx.Rollback()
		}
	}()

	// Cek duplikasi/idempotency di dalam transaksi DB
	if existing, err := s.Repo.GetByReferenceNoWithTx(tx, req.ReferenceNo); err == nil {
		if commitErr := tx.Commit(); commitErr != nil {
			tx.Rollback()
			return TransactionResult{Status: legacy.StatusSystemBusy, Code: "91", Message: "failed to complete idempotent transaction path"}
		}
		return s.transactionToResult(existing, true)
	} else if err != sql.ErrNoRows {
		tx.Rollback()
		return TransactionResult{Status: legacy.StatusSystemBusy, Code: "96", Message: "failed to check transaction reference"}
	}

	// Catat audit entry
	audit := &model.AuditEntry{
		EventType:    "TRANSACTION",
		EventSubType: string(req.Type),
		ReferenceID:  req.UserID,
		Status:       result.Status,
		Message:      result.Message,
		Payload:      fmt.Sprintf("user=%d recipient_user=%d amount=%d merchant=%s ref=%s profile=%s latency_ms=%d need_reversal=%t", req.UserID, req.RecipientUserID, req.Amount, req.MerchantCode, req.ReferenceNo, result.Profile, result.LatencyMs, result.NeedReversal),
	}
	auditID, auditErr := s.AuditRepo.Record(tx, audit)
	if auditErr != nil {
		tx.Rollback()
		return TransactionResult{Status: legacy.StatusSystemBusy, Code: "96", Message: "failed to log audit"}
	}

	if result.Status != legacy.StatusSuccess {
		var transactionID int
		if result.NeedReversal {
			transaction := &model.Transaction{
				UserID:          user.ID,
				MerchantID:      merchantIDFromModel(merchant),
				RecipientUserID: recipientIDFromModel(recipient),
				Amount:          req.Amount,
				Status:          "PENDING_REVERSAL",
				TransactionType: string(req.Type),
				ReferenceNo:     req.ReferenceNo,
			}
			transactionID, err = s.Repo.SaveWithTx(tx, transaction)
			if err != nil {
				tx.Rollback()
				return TransactionResult{Status: legacy.StatusFailed, Code: "94", Message: "failed to persist pending reversal transaction"}
			}
		}

		if commitErr := tx.Commit(); commitErr != nil {
			tx.Rollback()
			return TransactionResult{Status: legacy.StatusSystemBusy, Code: "91", Message: "failed to complete failed transaction path"}
		}
		return TransactionResult{
			Status:        result.Status,
			Code:          result.Code,
			Message:       result.Message,
			TransactionID: transactionID,
			ReferenceNo:   req.ReferenceNo,
			AuditID:       auditID,
			NeedReversal:  result.NeedReversal,
			LegacyProfile: result.Profile,
			LegacyLatency: result.LatencyMs,
		}
	}

	// 6. Kunci baris user sekarang karena kita siap melakukan pendebitan saldo
	userForUpdate, err := s.UserRepo.GetForUpdate(tx, user.ID)
	if err != nil {
		tx.Rollback()
		return TransactionResult{Status: legacy.StatusInvalidInput, Code: "14", Message: "user not found"}
	}

	// Verifikasi saldo sekali lagi (apakah berubah saat kita menunggu panggilan legacy selesai?)
	if userForUpdate.Balance < req.Amount {
		tx.Rollback()
		return TransactionResult{Status: legacy.StatusFailed, Code: "51", Message: "insufficient funds", ReferenceNo: req.ReferenceNo}
	}

	debited, err := s.UserRepo.DebitBalanceWithTx(tx, user.ID, req.Amount)
	if err != nil {
		tx.Rollback()
		return TransactionResult{Status: legacy.StatusFailed, Code: "94", Message: "failed to debit user account"}
	}
	if !debited {
		tx.Rollback()
		return TransactionResult{Status: legacy.StatusFailed, Code: "51", Message: "insufficient funds", ReferenceNo: req.ReferenceNo}
	}

	var merchantID int
	if merchant != nil {
		// Kunci baris merchant untuk mengupdate saldo
		merchantForUpdate, err := s.MerchantRepo.GetByIDForUpdate(tx, merchant.ID)
		if err != nil {
			tx.Rollback()
			return TransactionResult{Status: legacy.StatusFailed, Code: "95", Message: "failed to lock merchant for balance update"}
		}
		newMerchantBalance := merchantForUpdate.Balance + int64(req.Amount)
		if err := s.MerchantRepo.UpdateBalanceWithTx(tx, merchant.ID, newMerchantBalance); err != nil {
			tx.Rollback()
			return TransactionResult{Status: legacy.StatusFailed, Code: "95", Message: "failed to credit merchant account"}
		}
		merchantID = merchant.ID
	}

	if recipient != nil {
		// Kunci baris recipient untuk mengupdate saldo
		_, err := s.UserRepo.GetForUpdate(tx, recipient.ID)
		if err != nil {
			tx.Rollback()
			return TransactionResult{Status: legacy.StatusFailed, Code: "95", Message: "failed to lock recipient user for balance update"}
		}
		if err := s.UserRepo.CreditBalanceWithTx(tx, recipient.ID, req.Amount); err != nil {
			tx.Rollback()
			return TransactionResult{Status: legacy.StatusFailed, Code: "95", Message: "failed to credit recipient user account"}
		}
	}

	transaction := &model.Transaction{
		UserID:          user.ID,
		MerchantID:      merchantID,
		RecipientUserID: recipientIDFromModel(recipient),
		Amount:          req.Amount,
		Status:          result.Status,
		TransactionType: string(req.Type),
		ReferenceNo:     req.ReferenceNo,
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

	InvalidateBalanceCache(user.ID)
	if recipient != nil {
		InvalidateBalanceCache(recipient.ID)
	}
	if merchant != nil {
		InvalidateMerchantBalanceCache(merchant.ID)
		// Invalidate merchant info cache juga karena data merchant mungkin berubah
		DeleteFromCache(fmt.Sprintf("merchant_info:%s", merchant.MerchantCode))
	}
	InvalidateTransactionCache(user.ID)
	if recipient != nil {
		InvalidateTransactionCache(recipient.ID)
	}

	return TransactionResult{
		Status:        result.Status,
		Code:          result.Code,
		Message:       result.Message,
		TransactionID: transactionID,
		ReferenceNo:   req.ReferenceNo,
		AuditID:       auditID,
		NeedReversal:  result.NeedReversal,
		LegacyProfile: result.Profile,
		LegacyLatency: result.LatencyMs,
	}
}

func (s *TransactionService) GetUserTransactions(userID int) ([]map[string]interface{}, error) {
	key := fmt.Sprintf("transactions:%d", userID)

	if cached, ok := GetFromCache(key); ok {
		var transactions []map[string]interface{}
		if err := json.Unmarshal([]byte(cached), &transactions); err == nil {
			return transactions, nil
		}
	}

	transactions, err := s.Repo.GetByUserID(userID)
	if err != nil {
		return nil, err
	}

	if data, err := json.Marshal(transactions); err == nil {
		SetCache(key, string(data), defaultCacheTTL)
	}

	return transactions, nil
}

func (s *TransactionService) ReverseTransaction(transactionID int, referenceNo string) TransactionResult {
	req := &legacy.TransactionRequest{
		Type:        legacy.TypeReversal,
		ReferenceNo: referenceNo,
		UserID:      transactionID,
	}
	legacy.PrepareInquiryRequest(req)

	tx, err := s.DB.Begin()
	if err != nil {
		return TransactionResult{Status: legacy.StatusSystemBusy, Code: "91", Message: "unable to start reversal"}
	}
	defer func() {
		if err != nil {
			tx.Rollback()
		}
	}()

	if existing, err := s.Repo.GetByReferenceNoWithTx(tx, req.ReferenceNo); err == nil {
		if commitErr := tx.Commit(); commitErr != nil {
			tx.Rollback()
			return TransactionResult{Status: legacy.StatusSystemBusy, Code: "91", Message: "failed to complete idempotent reversal path"}
		}
		return s.transactionToResult(existing, true)
	} else if err != sql.ErrNoRows {
		tx.Rollback()
		return TransactionResult{Status: legacy.StatusSystemBusy, Code: "96", Message: "failed to check reversal reference"}
	}

	original, err := s.Repo.GetByIDForUpdate(tx, transactionID)
	if err != nil {
		tx.Rollback()
		if err == sql.ErrNoRows {
			return TransactionResult{Status: legacy.StatusInvalidInput, Code: "14", Message: "transaction not found", ReferenceNo: req.ReferenceNo}
		}
		return TransactionResult{Status: legacy.StatusSystemBusy, Code: "96", Message: "failed to fetch transaction", ReferenceNo: req.ReferenceNo}
	}

	if original.Status == "REVERSED" {
		tx.Rollback()
		return TransactionResult{Status: legacy.StatusFailed, Code: "94", Message: "transaction already reversed", TransactionID: original.ID, ReferenceNo: req.ReferenceNo}
	}
	if !canReverseTransactionStatus(original.Status) {
		tx.Rollback()
		return TransactionResult{Status: legacy.StatusFailed, Code: "94", Message: "only successful or pending reversal transactions can be reversed", TransactionID: original.ID, ReferenceNo: req.ReferenceNo}
	}

	user, err := s.UserRepo.GetForUpdate(tx, original.UserID)
	if err != nil {
		tx.Rollback()
		return TransactionResult{Status: legacy.StatusInvalidInput, Code: "14", Message: "user not found", TransactionID: original.ID, ReferenceNo: req.ReferenceNo}
	}

	shouldMoveFunds := original.Status == legacy.StatusSuccess
	var ledgerEntries []model.LedgerEntry
	if shouldMoveFunds {
		if err := s.UserRepo.UpdateBalanceWithTx(tx, user.ID, user.Balance+original.Amount); err != nil {
			tx.Rollback()
			return TransactionResult{Status: legacy.StatusFailed, Code: "94", Message: "failed to credit user account", TransactionID: original.ID, ReferenceNo: req.ReferenceNo}
		}
		ledgerEntries = append(ledgerEntries, model.LedgerEntry{AccountType: "USER", AccountID: user.ID, Direction: "CREDIT", Amount: int64(original.Amount), EntryType: legacy.TypeReversal})

		if original.MerchantID > 0 {
			merchant, err := s.MerchantRepo.GetByIDAnyStatusForUpdate(tx, original.MerchantID)
			if err != nil {
				tx.Rollback()
				return TransactionResult{Status: legacy.StatusInvalidInput, Code: "15", Message: "merchant not found", TransactionID: original.ID, ReferenceNo: req.ReferenceNo}
			}
			if err := s.MerchantRepo.UpdateBalanceWithTx(tx, merchant.ID, merchant.Balance-int64(original.Amount)); err != nil {
				tx.Rollback()
				return TransactionResult{Status: legacy.StatusFailed, Code: "95", Message: "failed to debit merchant account", TransactionID: original.ID, ReferenceNo: req.ReferenceNo}
			}
			ledgerEntries = append(ledgerEntries, model.LedgerEntry{AccountType: "MERCHANT", AccountID: merchant.ID, Direction: "DEBIT", Amount: int64(original.Amount), EntryType: legacy.TypeReversal})
		}

		if original.RecipientUserID > 0 {
			recipient, err := s.UserRepo.GetForUpdate(tx, original.RecipientUserID)
			if err != nil {
				tx.Rollback()
				return TransactionResult{Status: legacy.StatusInvalidInput, Code: "14", Message: "recipient user not found", TransactionID: original.ID, ReferenceNo: req.ReferenceNo}
			}
			if recipient.Balance < original.Amount {
				tx.Rollback()
				return TransactionResult{Status: legacy.StatusFailed, Code: "51", Message: "recipient has insufficient funds for reversal", TransactionID: original.ID, ReferenceNo: req.ReferenceNo}
			}
			if err := s.UserRepo.UpdateBalanceWithTx(tx, recipient.ID, recipient.Balance-original.Amount); err != nil {
				tx.Rollback()
				return TransactionResult{Status: legacy.StatusFailed, Code: "95", Message: "failed to debit recipient user account", TransactionID: original.ID, ReferenceNo: req.ReferenceNo}
			}
			ledgerEntries = append(ledgerEntries, model.LedgerEntry{AccountType: "USER", AccountID: recipient.ID, Direction: "DEBIT", Amount: int64(original.Amount), EntryType: legacy.TypeReversal})
		}
	}

	reversal := &model.Transaction{
		UserID:                  original.UserID,
		MerchantID:              original.MerchantID,
		RecipientUserID:         original.RecipientUserID,
		Amount:                  original.Amount,
		Status:                  legacy.StatusSuccess,
		TransactionType:         legacy.TypeReversal,
		ReferenceNo:             req.ReferenceNo,
		ReversalOfTransactionID: original.ID,
	}
	reversalID, err := s.Repo.SaveWithTx(tx, reversal)
	if err != nil {
		tx.Rollback()
		return TransactionResult{Status: legacy.StatusFailed, Code: "94", Message: "failed to persist reversal", TransactionID: original.ID, ReferenceNo: req.ReferenceNo}
	}

	for i := range ledgerEntries {
		ledgerEntries[i].TransactionID = reversalID
	}
	if len(ledgerEntries) > 0 {
		if err := s.LedgerRepo.PostEntries(tx, ledgerEntries); err != nil {
			tx.Rollback()
			return TransactionResult{Status: legacy.StatusFailed, Code: "94", Message: "failed to record reversal ledger", TransactionID: original.ID, ReferenceNo: req.ReferenceNo}
		}
	}

	if err := s.Repo.UpdateStatusWithTx(tx, original.ID, "REVERSED"); err != nil {
		tx.Rollback()
		return TransactionResult{Status: legacy.StatusFailed, Code: "94", Message: "failed to update original transaction", TransactionID: original.ID, ReferenceNo: req.ReferenceNo}
	}

	auditID := s.recordInquiryAudit(legacy.TypeReversal, original.ID, legacy.StatusSuccess, "Transaction reversed", fmt.Sprintf("transaction_id=%d reversal_transaction_id=%d ref=%s moved_funds=%t", original.ID, reversalID, req.ReferenceNo, shouldMoveFunds))

	if err := tx.Commit(); err != nil {
		tx.Rollback()
		return TransactionResult{Status: legacy.StatusSystemBusy, Code: "91", Message: "failed to commit reversal", TransactionID: original.ID, ReferenceNo: req.ReferenceNo}
	}

	// Invalidate cache status transaksi yang di-reverse
	DeleteFromCache(fmt.Sprintf("tx_status:%d", original.ID))

	return TransactionResult{
		Status:        legacy.StatusSuccess,
		Code:          "00",
		Message:       "Transaction reversed",
		TransactionID: reversalID,
		ReferenceNo:   req.ReferenceNo,
		AuditID:       auditID,
	}
}

func (s *TransactionService) GetUserTransactionHistory(userID int) TransactionHistoryResult {
	start := time.Now()
	req := &legacy.TransactionRequest{
		Type:   legacy.TypeHistory,
		UserID: userID,
	}
	legacy.PrepareInquiryRequest(req)
	profile, status := legacy.ExecuteInquiryHost(req.Type, "VALIDATION")

	result := TransactionHistoryResult{
		Status:        status,
		Code:          "00",
		Message:       "Transaction history inquiry successful",
		UserID:        userID,
		LegacyProfile: profile,
		LegacyLatency: time.Since(start).Milliseconds(),
	}
	if status != legacy.StatusSuccess {
		result.Code = "96"
		result.Message = "legacy core returned " + status
		result.AuditID = s.recordInquiryAudit(req.Type, userID, result.Status, result.Message, fmt.Sprintf("user=%d ref=%s profile=%s", userID, req.ReferenceNo, profile))
		return result
	}

	if _, err := s.UserRepo.GetByID(userID); err != nil {
		result.Status = legacy.StatusInvalidInput
		result.Code = "14"
		result.Message = "user not found"
		result.AuditID = s.recordInquiryAudit(req.Type, userID, result.Status, result.Message, fmt.Sprintf("user=%d ref=%s profile=%s", userID, req.ReferenceNo, profile))
		return result
	}

	transactions, err := s.Repo.GetByUserID(userID)
	if err != nil {
		result.Status = legacy.StatusSystemBusy
		result.Code = "96"
		result.Message = "failed to fetch transaction history"
		result.AuditID = s.recordInquiryAudit(req.Type, userID, result.Status, result.Message, fmt.Sprintf("user=%d ref=%s profile=%s", userID, req.ReferenceNo, profile))
		return result
	}

	result.Transactions = transactions
	result.LegacyLatency = time.Since(start).Milliseconds()
	result.AuditID = s.recordInquiryAudit(req.Type, userID, result.Status, result.Message, fmt.Sprintf("user=%d ref=%s profile=%s count=%d", userID, req.ReferenceNo, profile, len(transactions)))
	return result
}

func (s *TransactionService) GetTransactionStatus(transactionID int) TransactionStatusResult {
	// --- Cache L1/Redis: cek status transaksi sebelum ke DB & legacy ---
	// TTL pendek (30 detik) karena status bisa berubah (PENDING → SUCCESS)
	cacheKey := fmt.Sprintf("tx_status:%d", transactionID)
	if cached, ok := GetFromCache(cacheKey); ok {
		var item txStatusCacheItem
		if err := json.Unmarshal([]byte(cached), &item); err == nil {
			return TransactionStatusResult{
				Status:        legacy.StatusSuccess,
				Code:          "00",
				Message:       "Transaction status retrieved from cache",
				Data:          item.Data,
				LegacyProfile: "cache",
				LegacyLatency: 0,
			}
		}
	}

	start := time.Now()
	req := &legacy.TransactionRequest{
		Type:   legacy.TypeStatus,
		UserID: transactionID,
	}
	legacy.PrepareInquiryRequest(req)
	profile, status := legacy.ExecuteInquiryHost(req.Type, "VALIDATION")

	result := TransactionStatusResult{
		Status:        status,
		Code:          "00",
		Message:       "Transaction status inquiry successful",
		LegacyProfile: profile,
		LegacyLatency: time.Since(start).Milliseconds(),
	}
	if status != legacy.StatusSuccess {
		result.Code = "96"
		result.Message = "legacy core returned " + status
		result.AuditID = s.recordInquiryAudit(req.Type, transactionID, status, result.Message, fmt.Sprintf("transaction_id=%d ref=%s profile=%s", transactionID, req.ReferenceNo, profile))
		return result
	}

	data, err := s.Repo.GetStatusByID(transactionID)
	if err != nil {
		if err == sql.ErrNoRows {
			result.Status = legacy.StatusInvalidInput
			result.Code = "14"
			result.Message = "transaction not found"
		} else {
			result.Status = legacy.StatusSystemBusy
			result.Code = "96"
			result.Message = "failed to fetch transaction status"
		}
		result.LegacyLatency = time.Since(start).Milliseconds()
		result.AuditID = s.recordInquiryAudit(req.Type, transactionID, result.Status, result.Message, fmt.Sprintf("transaction_id=%d ref=%s profile=%s", transactionID, req.ReferenceNo, profile))
		return result
	}

	result.Data = data
	result.LegacyLatency = time.Since(start).Milliseconds()
	result.AuditID = s.recordInquiryAudit(req.Type, transactionID, result.Status, result.Message, fmt.Sprintf("transaction_id=%d ref=%s profile=%s", transactionID, req.ReferenceNo, profile))

	// Simpan ke cache dengan TTL pendek setelah berhasil mendapat status transaksi
	cacheItem := txStatusCacheItem{Data: data}
	if cacheData, err := json.Marshal(cacheItem); err == nil {
		SetCache(cacheKey, string(cacheData), txStatusCacheTTL)
	}

	return result
}

func (s *TransactionService) recordInquiryAudit(eventSubType string, referenceID int, status string, message string, payload string) int {
	audit := &model.AuditEntry{
		EventType:    "INQUIRY",
		EventSubType: eventSubType,
		ReferenceID:  referenceID,
		Status:       status,
		Message:      message,
		Payload:      payload,
	}
	auditID, _ := s.AuditRepo.RecordNoTx(audit)
	return auditID
}

func (s *TransactionService) transactionToResult(t *model.Transaction, idempotent bool) TransactionResult {
	code := "00"
	message := "Transaction already processed"
	if t.Status != legacy.StatusSuccess {
		code = "96"
		message = "Transaction already recorded with status " + t.Status
	}
	if t.Status == "REVERSED" {
		code = "94"
		message = "Transaction already reversed"
	}
	if t.TransactionType == legacy.TypeReversal {
		message = "Reversal already processed"
	}

	return TransactionResult{
		Status:        t.Status,
		Code:          code,
		Message:       message,
		TransactionID: t.ID,
		ReferenceNo:   t.ReferenceNo,
		Idempotent:    idempotent,
	}
}

func merchantIDFromModel(merchant *model.Merchant) int {
	if merchant == nil {
		return 0
	}
	return merchant.ID
}

func recipientIDFromModel(recipient *model.User) int {
	if recipient == nil {
		return 0
	}
	return recipient.ID
}

func canReverseTransactionStatus(status string) bool {
	return status == legacy.StatusSuccess || status == "PENDING_REVERSAL"
}