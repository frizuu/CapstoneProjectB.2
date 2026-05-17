package main

import (
	"baseline-system/cache"
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
	redisAddr := os.Getenv("REDIS_URL")
	if redisAddr == "" {
		// Default to localhost if not running in docker, or "redis:6379" if using docker-compose networks
		redisAddr = "localhost:6379"
	}
	redisCache := cache.NewRedisCache(redisAddr, "", 0)

	// Koneksi RabbitMQ
	amqpURL := os.Getenv("RABBITMQ_URL")
	if amqpURL == "" {
		amqpURL = "amqp://guest:guest@localhost:5672/"
	}
	rabbitConn, rabbitCh := messaging.ConnectRabbitMQ(amqpURL)
	if rabbitConn != nil {
		defer rabbitConn.Close()
	}
	if rabbitCh != nil {
		defer rabbitCh.Close()
	}

	// Repository
	repo := &repository.TransactionRepo{DB: db}
	userRepo := &repository.UserRepo{DB: db}
	merchantRepo := &repository.MerchantRepo{DB: db}
	auditRepo := &repository.AuditRepo{DB: db}
	ledgerRepo := &repository.LedgerRepo{DB: db}

	if err := repo.EnsureSchema(); err != nil {
		panic(err)
	}

	// Service (sekarang menerima DB, Cache, dan RabbitMQ)
	svc := &service.TransactionService{
		DB:           db,
		Repo:         repo,
		UserRepo:     userRepo,
		MerchantRepo: merchantRepo,
		AuditRepo:    auditRepo,
		LedgerRepo:   ledgerRepo,
		Cache:        redisCache, // Passed the cache properly!
		RabbitMQ:     rabbitCh,
	}

	// Handler (Removed the broken early declarations)
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
