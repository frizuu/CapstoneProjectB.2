package service

import (
	"baseline-system/legacy"
	"baseline-system/model"
	"testing"
)

func TestTransactionToResultMarksIdempotentReference(t *testing.T) {
	svc := &TransactionService{}
	result := svc.transactionToResult(&model.Transaction{
		ID:              7,
		Status:          legacy.StatusSuccess,
		ReferenceNo:     "REF-IDEMPOTENT-1",
		TransactionType: legacy.TypePayment,
	}, true)

	if !result.Idempotent {
		t.Fatal("expected idempotent result")
	}
	if result.TransactionID != 7 {
		t.Fatalf("expected transaction id 7, got %d", result.TransactionID)
	}
	if result.ReferenceNo != "REF-IDEMPOTENT-1" {
		t.Fatalf("expected reference no to be preserved, got %q", result.ReferenceNo)
	}
	if result.Code != "00" {
		t.Fatalf("expected success code 00, got %q", result.Code)
	}
}

func TestCanReverseTransactionStatus(t *testing.T) {
	reversible := []string{legacy.StatusSuccess, "PENDING_REVERSAL"}
	for _, status := range reversible {
		if !canReverseTransactionStatus(status) {
			t.Fatalf("expected status %s to be reversible", status)
		}
	}

	notReversible := []string{legacy.StatusFailed, legacy.StatusTimeout, "REVERSED"}
	for _, status := range notReversible {
		if canReverseTransactionStatus(status) {
			t.Fatalf("expected status %s not to be reversible", status)
		}
	}
}
