package model

type LedgerEntry struct {
	ID            int
	TransactionID int
	AccountType   string
	AccountID     int
	Direction     string
	Amount        int64
	EntryType     string
	CreatedAt     string
}
