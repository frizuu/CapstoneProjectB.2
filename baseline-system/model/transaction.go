package model

import "time"

type Transaction struct {
	ID                      int
	UserID                  int
	MerchantID              int
	RecipientUserID         int
	Amount                  int
	Status                  string
	TransactionType         string
	ReferenceNo             string
	ReversalOfTransactionID int
	CreatedAt               time.Time
}
