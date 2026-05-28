package handler

import (
	"baseline-system/metrics"
	"baseline-system/service"
	"encoding/json"
	"net/http"
	"strconv"
)

type UserHandler struct {
	Service *service.TransactionService
}

func (h *UserHandler) GetBalance(w http.ResponseWriter, r *http.Request) {
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

	if userID <= 0 {
		http.Error(w, "user_id must be greater than 0", http.StatusBadRequest)
		return
	}

	result := h.Service.BalanceInquiry(userID)
	metrics.RecordBusinessOperation("BALANCE_INQUIRY", result.Status, result.Code, 0)

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(result)
}
