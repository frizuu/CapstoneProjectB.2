package legacy

import (
	"baseline-system/model"
	"testing"
	"time"
)

func TestExecuteTransactionRejectsInsufficientFundsBeforePosting(t *testing.T) {
	req := &TransactionRequest{
		Type:          TypePayment,
		UserID:        1,
		Amount:        50000,
		RequestedTime: time.Date(2026, 5, 14, 10, 0, 0, 0, time.UTC),
	}
	user := &model.User{
		ID:      1,
		Name:    "Test User",
		Balance: 10000,
	}

	result := ExecuteTransaction(req, user, nil)

	if result.Status != StatusFailed {
		t.Fatalf("expected failed status, got %s", result.Status)
	}
	if result.Code != "51" {
		t.Fatalf("expected insufficient funds code 51, got %s", result.Code)
	}
	if result.Message != "insufficient funds" {
		t.Fatalf("expected insufficient funds message, got %q", result.Message)
	}
}

func TestExecuteMerchantBalanceInquiryRejectsInactiveMerchant(t *testing.T) {
	req := &TransactionRequest{
		Type:         TypeMerchantBalance,
		MerchantCode: "MRC-INACTIVE",
	}
	merchant := &model.Merchant{
		ID:           10,
		Name:         "Inactive Merchant",
		Balance:      250000,
		MerchantCode: "MRC-INACTIVE",
		Status:       "INACTIVE",
	}

	result := ExecuteMerchantBalanceInquiry(req, merchant)

	if result.Status != StatusFailed {
		t.Fatalf("expected failed status, got %s", result.Status)
	}
	if result.Code != "16" {
		t.Fatalf("expected inactive merchant code 16, got %s", result.Code)
	}
	if result.Balance != 0 {
		t.Fatalf("expected balance to be omitted on failed inquiry, got %d", result.Balance)
	}
}
