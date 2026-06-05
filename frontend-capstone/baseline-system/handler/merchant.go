package handler

import (
	"baseline-system/metrics"
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

// InquiryQRIS sekarang memanggil service.MerchantInquiry agar cache L1/Redis terpakai
func (h *MerchantHandler) InquiryQRIS(w http.ResponseWriter, r *http.Request) {
	merchantCode := r.URL.Query().Get("merchant_code")
	if merchantCode == "" {
		http.Error(w, "merchant_code is required", http.StatusBadRequest)
		return
	}

	result := h.Service.MerchantInquiry(merchantCode)

	metrics.RecordBusinessOperation("QRIS_INQUIRY", "SUCCESS", "00", 0)
	w.Header().Set("Content-Type", "application/json")
	if result.Code != "00" {
		w.WriteHeader(http.StatusNotFound)
	}
	json.NewEncoder(w).Encode(result)
}

func (h *MerchantHandler) GetMerchantBalance(w http.ResponseWriter, r *http.Request) {
	merchantIDStr := r.URL.Query().Get("merchant_id")
	merchantID, err := strconv.Atoi(merchantIDStr)
	if err != nil || merchantID == 0 {
		http.Error(w, "merchant_id is required", http.StatusBadRequest)
		return
	}

	result := h.Service.MerchantBalanceInquiry(merchantID)
	metrics.RecordBusinessOperation("MERCHANT_BALANCE_INQUIRY", result.Status, result.Code, 0)

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(result)
}

func (h *MerchantHandler) GetAllMerchants(w http.ResponseWriter, r *http.Request) {
	merchants, err := h.MerchantRepo.GetAll()
	if err != nil {
		metrics.RecordBusinessOperation("MERCHANT_LIST", "FAILED", "96", 0)
		http.Error(w, "Failed to fetch merchants", http.StatusInternalServerError)
		return
	}

	metrics.RecordBusinessOperation("MERCHANT_LIST", "SUCCESS", "00", 0)
	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(map[string]interface{}{
		"status":    "SUCCESS",
		"merchants": merchants,
	})
}
