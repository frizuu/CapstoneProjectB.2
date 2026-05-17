package main

import (
	"baseline-system/config"
	"baseline-system/handler"
	"baseline-system/messaging"
	"baseline-system/repository"
	"baseline-system/service"
	"net/http"
	"os"
)

func main() {

	// Koneksi Database
	db := config.ConnectDB()

	// Koneksi Redis Cache
	redisCache := cache.NewRedisCache()

	// Koneksi RabbitMQ
	amqpURL := os.Getenv("RABBITMQ_URL")
	if amqpURL == "" {
		amqpURL = "amqp://guest:guest@localhost:5672/"
	}
	rabbitConn, rabbitCh := messaging.ConnectRabbitMQ(amqpURL)
	defer rabbitConn.Close()
	defer rabbitCh.Close()

	// Repository
	repo := &repository.TransactionRepo{DB: db}
	userRepo := &repository.UserRepo{DB: db}
	merchantRepo := &repository.MerchantRepo{DB: db}
	auditRepo := &repository.AuditRepo{DB: db}
	ledgerRepo := &repository.LedgerRepo{DB: db}
	if err := repo.EnsureSchema(); err != nil {
		panic(err)
	}


	// Handler
	userHandler := &handler.UserHandler{UserRepo: userRepo}
	merchantHandler := &handler.MerchantHandler{MerchantRepo: merchantRepo}

	// Service (sekarang menerima DB, Cache, dan RabbitMQ)
	svc := &service.TransactionService{
		DB:           db,
		Repo:         repo,
		UserRepo:     userRepo,
		MerchantRepo: merchantRepo,
		AuditRepo:    auditRepo,
		LedgerRepo:   ledgerRepo,
		RabbitMQ:     rabbitCh,
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