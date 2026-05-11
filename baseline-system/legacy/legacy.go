package legacy

import (
	"math/rand"
	"time"

	"baseline-system/model"
)

const (
	TypePayment = "PAYMENT"
	TypeQRIS    = "QRIS_PAYMENT"

	StatusSuccess      = "SUCCESS"
	StatusFailed       = "FAILED"
	StatusTimeout      = "TIMEOUT"
	StatusSystemBusy   = "SYSTEM_BUSY"
	StatusInvalidInput = "INVALID_REQUEST"
)

type TransactionRequest struct {
	Type         string
	UserID       int
	MerchantCode string
	Amount       int
}

type TransactionResult struct {
	Status  string
	Code    string
	Message string
}

func init() {
	rand.Seed(time.Now().UnixNano())
}

func simulateAS400Delay() {
	minMs := 500
	maxMs := 2000
	delay := rand.Intn(maxMs-minMs) + minMs
	time.Sleep(time.Duration(delay) * time.Millisecond)
}

func simulateAS400BatchResponse(operation string) string {
	if operation == "" {
		return StatusInvalidInput
	}

	percent := rand.Intn(100)

	switch {
	case percent < 82:
		return StatusSuccess
	case percent < 88:
		return StatusFailed
	case percent < 96:
		return StatusTimeout
	default:
		return StatusSystemBusy
	}
}

func processLegacyCore(operation string) string {
	simulateAS400Delay()
	return simulateAS400BatchResponse(operation)
}

func validateRequest(req *TransactionRequest, user *model.User, merchant *model.Merchant) *TransactionResult {
	if req == nil || req.UserID == 0 || req.Amount <= 0 {
		return &TransactionResult{Status: StatusInvalidInput, Code: "14", Message: "invalid transaction request"}
	}

	if req.Amount < 100 {
		return &TransactionResult{Status: StatusFailed, Code: "13", Message: "amount below minimum threshold"}
	}

	if user == nil {
		return &TransactionResult{Status: StatusInvalidInput, Code: "14", Message: "user not found"}
	}

	if req.Type == TypeQRIS {
		if merchant == nil {
			return &TransactionResult{Status: StatusInvalidInput, Code: "15", Message: "merchant lookup failed"}
		}
		if merchant.Status != "ACTIVE" {
			return &TransactionResult{Status: StatusFailed, Code: "16", Message: "merchant is not active"}
		}
	}

	if user.Balance < req.Amount {
		return &TransactionResult{Status: StatusFailed, Code: "51", Message: "insufficient funds"}
	}

	return nil
}

// evaluateBusinessRules enforces banking rules on transaction (AS/400 rule engine)
func evaluateBusinessRules(req *TransactionRequest, user *model.User, merchant *model.Merchant) *TransactionResult {
	if req.Amount > 10000000 {
		return &TransactionResult{Status: StatusFailed, Code: "61", Message: "amount exceeds single-transaction limit"}
	}

	if req.Type == TypeQRIS && merchant != nil && merchant.Category == "INACTIVE" {
		return &TransactionResult{Status: StatusFailed, Code: "62", Message: "merchant is not allowed for QRIS settlement"}
	}

	return nil
}

// fraudCheck performs batch fraud scoring (simulated AS/400 fraud engine)
func fraudCheck(req *TransactionRequest, user *model.User, merchant *model.Merchant) *TransactionResult {
	if req.Amount > 5000000 {
		percent := rand.Intn(100)
		if percent < 30 {
			return &TransactionResult{Status: StatusFailed, Code: "79", Message: "transaction flagged by fraud rules"}
		}
	}
	return nil
}

// ExecuteTransaction orchestrates the full AS/400-style transaction pipeline
// 1. Validation (input screening)
// 2. Rules evaluation (business logic)
// 3. Fraud check (risk scoring)
// 4. Legacy core processing (batch core)
// Entire flow is blocking and sequential with integrated audit trail
func ExecuteTransaction(req *TransactionRequest, user *model.User, merchant *model.Merchant) TransactionResult {
	// STAGE 1: Input Validation (Sequential)
	if validation := validateRequest(req, user, merchant); validation != nil {
		return *validation
	}

	// STAGE 2: Rules Evaluation (Blocking)
	if rule := evaluateBusinessRules(req, user, merchant); rule != nil {
		return *rule
	}

	// STAGE 3: Fraud Check (Scoring Engine)
	if fraud := fraudCheck(req, user, merchant); fraud != nil {
		return *fraud
	}

	// STAGE 4: Monolithic Core Processing (AS/400 Batch)
	status := processLegacyCore(req.Type)
	if status != StatusSuccess {
		return TransactionResult{Status: status, Code: "96", Message: "legacy core returned " + status}
	}

	return TransactionResult{Status: StatusSuccess, Code: "00", Message: "transaction approved"}
}
