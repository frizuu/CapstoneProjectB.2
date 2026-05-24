package main

import (
	"baseline-system/config"
	"baseline-system/handler"
	"baseline-system/metrics"
	"baseline-system/repository"
	"baseline-system/service"
	"net/http"
)

func main() {

	// Koneksi Database
	db := config.ConnectDB()

	// Repository
	repo := &repository.TransactionRepo{DB: db}
	userRepo := &repository.UserRepo{DB: db}
	merchantRepo := &repository.MerchantRepo{DB: db}
	auditRepo := &repository.AuditRepo{DB: db}
	ledgerRepo := &repository.LedgerRepo{DB: db}
	if err := repo.EnsureSchema(); err != nil {
		panic(err)
	}

	svc := &service.TransactionService{
		DB:           db,
		Repo:         repo,
		UserRepo:     userRepo,
		MerchantRepo: merchantRepo,
		AuditRepo:    auditRepo,
		LedgerRepo:   ledgerRepo,
	}

	// Handler
	userHandler := &handler.UserHandler{Service: svc}
	merchantHandler := &handler.MerchantHandler{MerchantRepo: merchantRepo, Service: svc}
	h := &handler.Handler{Service: svc}

	mux := http.NewServeMux()

	// Routes - existing
	mux.HandleFunc("/payment", metrics.InstrumentHTTP("/payment", h.Payment))
	mux.HandleFunc("/transaction/reversal", metrics.InstrumentHTTP("/transaction/reversal", h.ReverseTransaction))
	mux.HandleFunc("/balance", metrics.InstrumentHTTP("/balance", userHandler.GetBalance))

	// Routes - QRIS
	mux.HandleFunc("/qris/inquiry", metrics.InstrumentHTTP("/qris/inquiry", merchantHandler.InquiryQRIS))
	mux.HandleFunc("/qris/payment", metrics.InstrumentHTTP("/qris/payment", h.PaymentQRIS))
	mux.HandleFunc("/merchant/balance", metrics.InstrumentHTTP("/merchant/balance", merchantHandler.GetMerchantBalance))
	mux.HandleFunc("/merchants", metrics.InstrumentHTTP("/merchants", merchantHandler.GetAllMerchants))
	mux.HandleFunc("/transactions", metrics.InstrumentHTTP("/transactions", h.GetUserTransactions))
	mux.HandleFunc("/transaction/status", metrics.InstrumentHTTP("/transaction/status", h.GetTransactionStatus))
	mux.HandleFunc("/metrics", metrics.Handler)
	println("Server running on :8080")
	http.ListenAndServe(":8080", mux)
}
