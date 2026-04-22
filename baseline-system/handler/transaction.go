package handler

import (
	"baseline-system/service"
	"encoding/json"
	"net/http"
)

type Handler struct {
	Service *service.TransactionService
}

type Request struct {
	UserID int `json:"user_id"`
	Amount int `json:"amount"`
}

func (h *Handler) Payment(w http.ResponseWriter, r *http.Request) {
	var req Request
	json.NewDecoder(r.Body).Decode(&req)

	result := h.Service.Process(req.UserID, req.Amount)

	json.NewEncoder(w).Encode(map[string]string{
		"status": result,
	})
}