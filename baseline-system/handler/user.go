package handler

import (
	"baseline-system/repository"
	"encoding/json"
	"net/http"
	"strconv"
)

type UserHandler struct {
	UserRepo *repository.UserRepo
}

func (h *UserHandler) GetBalance(w http.ResponseWriter, r *http.Request) {
	userIDStr := r.URL.Query().Get("user_id")
	userID, _ := strconv.Atoi(userIDStr)

	balance, err := h.UserRepo.GetBalance(userID)
	if err != nil {
		http.Error(w, "User not found", 404)
		return
	}

	json.NewEncoder(w).Encode(map[string]int{
		"balance": balance,
	})
}
