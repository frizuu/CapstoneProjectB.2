package handler

import (
	"baseline-system/service"
	"encoding/json"
	"net/http"
	"strconv"
)

type Handler struct {
	Service *service.TransactionService
}

type Request struct {
	UserID int `json:"user_id"`
	Amount int `json:"amount"`
}

type QRISRequest struct {
	UserID       int    `json:"user_id"`
	MerchantCode string `json:"merchant_code"`
	Amount       int    `json:"amount"`
}

// Payment - POST /payment (existing)
func (h *Handler) Payment(w http.ResponseWriter, r *http.Request) {
	var req Request
	json.NewDecoder(r.Body).Decode(&req)

	result := h.Service.Process(req.UserID, req.Amount)

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(map[string]string{
		"status": result,
	})
}

// PaymentQRIS - POST /qris/payment (baru)
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

	result := h.Service.ProcessQRIS(req.UserID, req.MerchantCode, req.Amount)

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(map[string]string{
		"status": result,
	})
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

	data, err := h.Service.GetUserTransactions(userID)
	if err != nil {
		http.Error(w, "failed to fetch transactions", http.StatusInternalServerError)
		return
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(data)
}
