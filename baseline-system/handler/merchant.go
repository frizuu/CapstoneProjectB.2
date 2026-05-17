package handler

import (
	"baseline-system/repository"
	"baseline-system/service"
	"encoding/json"
	"net/http"
	"strconv"
)

type MerchantHandler struct {
	MerchantRepo *repository.MerchantRepo
	Service      *service.TransactionService
}

func (h *MerchantHandler) InquiryQRIS(w http.ResponseWriter, r *http.Request) {
	merchantCode := r.URL.Query().Get("merchant_code")
	if merchantCode == "" {
		http.Error(w, "merchant_code is required", http.StatusBadRequest)
		return
	}

	merchant, err := h.MerchantRepo.GetByCode(merchantCode)
	if err != nil {
		http.Error(w, "Merchant not found", http.StatusNotFound)
		return
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(map[string]interface{}{
		"status":        "SUCCESS",
		"merchant_id":   merchant.ID,
		"merchant_name": merchant.Name,
		"merchant_code": merchant.MerchantCode,
		"category":      merchant.Category,
	})
}

func (h *MerchantHandler) GetMerchantBalance(w http.ResponseWriter, r *http.Request) {
	merchantIDStr := r.URL.Query().Get("merchant_id")
	merchantID, err := strconv.Atoi(merchantIDStr)
	if err != nil || merchantID == 0 {
		http.Error(w, "merchant_id is required", http.StatusBadRequest)
		return
	}

	result := h.Service.MerchantBalanceInquiry(merchantID)

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(result)
}

func (h *MerchantHandler) GetAllMerchants(w http.ResponseWriter, r *http.Request) {
	merchants, err := h.MerchantRepo.GetAll()
	if err != nil {
		http.Error(w, "Failed to fetch merchants", http.StatusInternalServerError)
		return
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(map[string]interface{}{
		"status":    "SUCCESS",
		"merchants": merchants,
	})
}
