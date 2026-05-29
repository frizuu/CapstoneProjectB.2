package main

import (
	"baseline-system/config"
	"baseline-system/handler"
	"baseline-system/metrics"
	"baseline-system/repository"
	"baseline-system/service"
	"fmt"
	"log"
	"net/http"
	"os"
)

func main() {
	redisHost := os.Getenv("REDIS_HOST")
	if redisHost == "" {
		redisHost = "localhost"
	}

	redisPort := os.Getenv("REDIS_PORT")
	if redisPort == "" {
		redisPort = "6379"
	}

	cacheMode := os.Getenv("CACHE_MODE")
	if cacheMode == "" {
		cacheMode = "full"
	}

	if err := service.InitCache(fmt.Sprintf("%s:%s", redisHost, redisPort), cacheMode); err != nil {
		log.Fatalf("failed to initialize cache: %v", err)
	}

	log.Printf("cache initialized: mode=%s redis=%s:%s", cacheMode, redisHost, redisPort)

	db := config.ConnectDB()

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

	log.Println("Server running on :8080")
	log.Fatal(http.ListenAndServe(":8080", mux))
}