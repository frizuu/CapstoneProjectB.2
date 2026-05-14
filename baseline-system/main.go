package main

import (
	"baseline-system/config"
	"baseline-system/handler"
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

	// Routes - existing
	http.HandleFunc("/payment", h.Payment)
	http.HandleFunc("/transaction/reversal", h.ReverseTransaction)
	http.HandleFunc("/balance", userHandler.GetBalance)

	// Routes - QRIS
	http.HandleFunc("/qris/inquiry", merchantHandler.InquiryQRIS)
	http.HandleFunc("/qris/payment", h.PaymentQRIS)
	http.HandleFunc("/merchant/balance", merchantHandler.GetMerchantBalance)
	http.HandleFunc("/merchants", merchantHandler.GetAllMerchants)
	http.HandleFunc("/transactions", h.GetUserTransactions)
	http.HandleFunc("/transaction/status", h.GetTransactionStatus)
	println("Server running on :8080")
	http.ListenAndServe(":8080", nil)
}
