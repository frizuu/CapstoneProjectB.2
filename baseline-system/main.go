package main

import (
	"baseline-system/config"
	"baseline-system/handler"
	"baseline-system/repository"
	"baseline-system/service"
	"net/http"
)

func main() {

	db := config.ConnectDB()

	repo := &repository.TransactionRepo{DB: db}
	userRepo := &repository.UserRepo{DB: db}
	userHandler := &handler.UserHandler{UserRepo: userRepo}
	svc := &service.TransactionService{Repo: repo, UserRepo: userRepo}
	h := &handler.Handler{Service: svc}

	http.HandleFunc("/payment", h.Payment)
	http.HandleFunc("/balance", userHandler.GetBalance)

	println("Server running on :8080")
	http.ListenAndServe(":8080", nil)
}
