package main

import (
	"baseline-system/cache"
	"baseline-system/config"
	"baseline-system/handler"
	"baseline-system/messaging"
	"baseline-system/metrics"
	"baseline-system/repository"
	"baseline-system/service"
	"baseline-system/worker"
	"fmt"
	"log"
	"net/http"
	"os"
)

func envOrDefault(key string, fallback string) string {
	value := os.Getenv(key)
	if value == "" {
		return fallback
	}
	return value
}

func main() {
	redisHost := envOrDefault("REDIS_HOST", "localhost")
	redisPort := envOrDefault("REDIS_PORT", "6380")
	redisAddr := envOrDefault("REDIS_URL", fmt.Sprintf("%s:%s", redisHost, redisPort))
	cacheMode := envOrDefault("CACHE_MODE", "full")

	if err := service.InitCache(redisAddr, cacheMode); err != nil {
		log.Printf("cache initialization warning: %v", err)
	}

	log.Printf("cache initialized: requested_mode=%s active_mode=%s redis=%s", cacheMode, service.CacheMode(), redisAddr)

	db := config.ConnectDB()

	// Koneksi Redis Cache
	redisCache := cache.NewRedisCache(redisAddr, "", 0)

	// Koneksi RabbitMQ
	amqpURL := envOrDefault("RABBITMQ_URL", "amqp://guest:guest@localhost:5672/")
	rabbitConn, rabbitCh := messaging.ConnectRabbitMQ(amqpURL)
	if rabbitConn != nil {
		defer rabbitConn.Close()
	}
	if rabbitCh != nil {
		defer rabbitCh.Close()
	}

	repo := &repository.TransactionRepo{DB: db}
	userRepo := &repository.UserRepo{DB: db}
	merchantRepo := &repository.MerchantRepo{DB: db}
	auditRepo := &repository.AuditRepo{DB: db}
	ledgerRepo := &repository.LedgerRepo{DB: db}
	if err := repo.EnsureSchema(); err != nil {
		panic(err)
	}

	// ---> NEW: Initialize WebSocket Manager before the worker <---
	wsManager := handler.NewWSManager()

	if rabbitCh != nil {
		// ---> MODIFIED: Pass the wsManager to the worker <---
		worker.StartAuditWorker(rabbitCh, auditRepo, wsManager)
	}

	// Service (sekarang menerima DB, Cache, dan RabbitMQ)
	svc := &service.TransactionService{
		DB:           db,
		Repo:         repo,
		UserRepo:     userRepo,
		MerchantRepo: merchantRepo,
		AuditRepo:    auditRepo,
		LedgerRepo:   ledgerRepo,
		Cache:        redisCache,
		RabbitMQ:     rabbitCh,
	}

	userHandler := &handler.UserHandler{Service: svc}
	merchantHandler := &handler.MerchantHandler{
		MerchantRepo: merchantRepo,
		Service:      svc,
	}

	h := &handler.Handler{Service: svc}

	mux := http.NewServeMux()

	mux.HandleFunc("/payment", metrics.InstrumentHTTP("/payment", h.Payment))
	mux.HandleFunc("/transaction/reversal", metrics.InstrumentHTTP("/transaction/reversal", h.ReverseTransaction))
	mux.HandleFunc("/balance", metrics.InstrumentHTTP("/balance", userHandler.GetBalance))

	mux.HandleFunc("/qris/inquiry", metrics.InstrumentHTTP("/qris/inquiry", merchantHandler.InquiryQRIS))
	mux.HandleFunc("/qris/payment", metrics.InstrumentHTTP("/qris/payment", h.PaymentQRIS))
	mux.HandleFunc("/merchant/balance", metrics.InstrumentHTTP("/merchant/balance", merchantHandler.GetMerchantBalance))
	mux.HandleFunc("/merchants", metrics.InstrumentHTTP("/merchants", merchantHandler.GetAllMerchants))
	mux.HandleFunc("/transactions", metrics.InstrumentHTTP("/transactions", h.GetUserTransactions))
	mux.HandleFunc("/transaction/status", metrics.InstrumentHTTP("/transaction/status", h.GetTransactionStatus))

	mux.HandleFunc("/metrics", metrics.Handler)

	// ---> NEW: Expose the WebSocket endpoint <---
	mux.HandleFunc("/ws", wsManager.HandleWebSocket)

	log.Println("Server running on :8080")
	log.Fatal(http.ListenAndServe(":8080", mux))
}