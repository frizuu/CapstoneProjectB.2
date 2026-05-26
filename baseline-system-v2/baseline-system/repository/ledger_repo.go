package repository

import (
	"baseline-system/model"
	"database/sql"
)

type LedgerRepo struct {
	DB *sql.DB
}

func (r *LedgerRepo) PostEntries(tx *sql.Tx, entries []model.LedgerEntry) error {
	stmt, err := tx.Prepare(
		`INSERT INTO ledger_entries(transaction_id, account_type, account_id, direction, amount, entry_type)
		 VALUES($1,$2,$3,$4,$5,$6)`,
	)
	if err != nil {
		return err
	}
	defer stmt.Close()

	for _, entry := range entries {
		if _, err := stmt.Exec(entry.TransactionID, entry.AccountType, entry.AccountID, entry.Direction, entry.Amount, entry.EntryType); err != nil {
			return err
		}
	}

	return nil
}
