package handler

import (
	"baseline-system/metrics"
	"baseline-system/service"
	"encoding/json"
	"net/http"
	"strconv"
)

type Handler struct {
	Service *service.TransactionService
}

type Request struct {
	UserID          int    `json:"user_id"`
	RecipientUserID int    `json:"recipient_user_id,omitempty"`
	MerchantID      int    `json:"merchant_id,omitempty"`
	MerchantCode    string `json:"merchant_code,omitempty"`
	Amount          int    `json:"amount"`
	ReferenceNo     string `json:"reference_no,omitempty"`
}

type QRISRequest struct {
	UserID       int    `json:"user_id"`
	MerchantCode string `json:"merchant_code"`
	Amount       int    `json:"amount"`
	ReferenceNo  string `json:"reference_no,omitempty"`
}

type ReversalRequest struct {
	TransactionID int    `json:"transaction_id"`
	ReferenceNo   string `json:"reference_no,omitempty"`
}

type TransactionResponse struct {
	Status        string `json:"status"`
	Code          string `json:"code,omitempty"`
	Message       string `json:"message,omitempty"`
	TransactionID int    `json:"transaction_id,omitempty"`
	AuditID       int    `json:"audit_id,omitempty"`
}

// Payment - POST /payment
func (h *Handler) Payment(w http.ResponseWriter, r *http.Request) {
	var req Request
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		http.Error(w, "Invalid request body", http.StatusBadRequest)
		return
	}

	if req.UserID == 0 || req.Amount <= 0 {
		http.Error(w, "user_id and amount are required", http.StatusBadRequest)
		return
	}

	targetCount := 0
	if req.RecipientUserID != 0 {
		targetCount++
	}
	if req.MerchantID != 0 {
		targetCount++
	}
	if req.MerchantCode != "" {
		targetCount++
	}
	if targetCount > 1 {
		http.Error(w, "use only one target: recipient_user_id, merchant_id, or merchant_code", http.StatusBadRequest)
		return
	}
	if req.RecipientUserID == req.UserID {
		http.Error(w, "recipient_user_id cannot be the same as user_id", http.StatusBadRequest)
		return
	}

	var result service.TransactionResult
	switch {
	case req.RecipientUserID != 0:
		result = h.Service.ProcessTransfer(req.UserID, req.RecipientUserID, req.Amount, req.ReferenceNo)
		metrics.RecordBusinessOperation("TRANSFER", result.Status, result.Code, req.Amount)
	case req.MerchantID != 0 || req.MerchantCode != "":
		result = h.Service.ProcessToMerchant(req.UserID, req.MerchantID, req.MerchantCode, req.Amount, req.ReferenceNo)
		metrics.RecordBusinessOperation("MERCHANT_PAYMENT", result.Status, result.Code, req.Amount)
	default:
		result = h.Service.Process(req.UserID, req.Amount, req.ReferenceNo)
		metrics.RecordBusinessOperation("PAYMENT", result.Status, result.Code, req.Amount)
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(result)
}

// PaymentQRIS - POST /qris/payment
func (h *Handler) PaymentQRIS(w http.ResponseWriter, r *http.Request) {
	var req QRISRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		http.Error(w, "Invalid request body", http.StatusBadRequest)
		return
	}

	if req.UserID == 0 || req.MerchantCode == "" || req.Amount <= 0 {
		http.Error(w, "user_id, merchant_code, and amount are required", http.StatusBadRequest)
		return
	}

	result := h.Service.ProcessQRIS(req.UserID, req.MerchantCode, req.Amount, req.ReferenceNo)
	metrics.RecordBusinessOperation("QRIS_PAYMENT", result.Status, result.Code, req.Amount)

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(result)
}

func (h *Handler) ReverseTransaction(w http.ResponseWriter, r *http.Request) {
	var req ReversalRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		http.Error(w, "Invalid request body", http.StatusBadRequest)
		return
	}

	if req.TransactionID <= 0 {
		http.Error(w, "transaction_id is required", http.StatusBadRequest)
		return
	}

	result := h.Service.ReverseTransaction(req.TransactionID, req.ReferenceNo)
	metrics.RecordBusinessOperation("REVERSAL", result.Status, result.Code, 0)

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(result)
}

func (h *Handler) GetUserTransactions(w http.ResponseWriter, r *http.Request) {
	userIDStr := r.URL.Query().Get("user_id")
	if userIDStr == "" {
		http.Error(w, "user_id is required", http.StatusBadRequest)
		return
	}

	userID, err := strconv.Atoi(userIDStr)
	if err != nil {
		http.Error(w, "invalid user_id", http.StatusBadRequest)
		return
	}

	result := h.Service.GetUserTransactionHistory(userID)
	metrics.RecordBusinessOperation("TRANSACTION_HISTORY", result.Status, result.Code, 0)

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(result)
}

func (h *Handler) GetTransactionStatus(w http.ResponseWriter, r *http.Request) {
	transactionIDStr := r.URL.Query().Get("transaction_id")
	if transactionIDStr == "" {
		http.Error(w, "transaction_id is required", http.StatusBadRequest)
		return
	}

	transactionID, err := strconv.Atoi(transactionIDStr)
	if err != nil {
		http.Error(w, "invalid transaction_id", http.StatusBadRequest)
		return
	}

	if transactionID <= 0 {
		http.Error(w, "transaction_id must be greater than 0", http.StatusBadRequest)
		return
	}

	result := h.Service.GetTransactionStatus(transactionID)
	metrics.RecordBusinessOperation("TRANSACTION_STATUS", result.Status, result.Code, 0)

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(result)
}
