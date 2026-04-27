package handler

import (
	"baseline-system/repository"
	"encoding/json"
	"net/http"
	"strconv"
)

type MerchantHandler struct {
	MerchantRepo *repository.MerchantRepo
}

// InquiryQRIS - GET /qris/inquiry?merchant_code=NMID001234567890
// Mengambil informasi merchant berdasarkan merchant_code dari QR
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

// GetMerchantBalance - GET /merchant/balance?merchant_id=1
// Mengambil saldo merchant berdasarkan ID
func (h *MerchantHandler) GetMerchantBalance(w http.ResponseWriter, r *http.Request) {
	merchantIDStr := r.URL.Query().Get("merchant_id")
	merchantID, err := strconv.Atoi(merchantIDStr)
	if err != nil || merchantID == 0 {
		http.Error(w, "merchant_id is required", http.StatusBadRequest)
		return
	}

	balance, err := h.MerchantRepo.GetBalance(merchantID)
	if err != nil {
		http.Error(w, "Merchant not found", http.StatusNotFound)
		return
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(map[string]interface{}{
		"merchant_id": merchantID,
		"balance":     balance,
	})
}

// GetAllMerchants - GET /merchants
// Mengambil semua merchant yang aktif
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
