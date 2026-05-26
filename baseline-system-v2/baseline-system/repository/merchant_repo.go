package repository

import (
	"baseline-system/model"
	"database/sql"
)

type MerchantRepo struct {
	DB *sql.DB
}

// GetByCode - mencari merchant berdasarkan merchant_code (untuk QRIS inquiry)
func (r *MerchantRepo) GetByCode(merchantCode string) (*model.Merchant, error) {
	merchant := &model.Merchant{}
	err := r.DB.QueryRow(
		`SELECT id, name, balance, merchant_code, category, status, created_at
		 FROM public.merchants
		 WHERE merchant_code = $1 AND status = 'ACTIVE'`,
		merchantCode,
	).Scan(
		&merchant.ID,
		&merchant.Name,
		&merchant.Balance,
		&merchant.MerchantCode,
		&merchant.Category,
		&merchant.Status,
		&merchant.CreatedAt,
	)
	if err != nil {
		return nil, err
	}
	return merchant, nil
}

func (r *MerchantRepo) GetByCodeForUpdate(tx *sql.Tx, merchantCode string) (*model.Merchant, error) {
	merchant := &model.Merchant{}
	err := tx.QueryRow(
		`SELECT id, name, balance, merchant_code, category, status, created_at
		 FROM public.merchants
		 WHERE merchant_code = $1 AND status = 'ACTIVE' FOR UPDATE`,
		merchantCode,
	).Scan(
		&merchant.ID,
		&merchant.Name,
		&merchant.Balance,
		&merchant.MerchantCode,
		&merchant.Category,
		&merchant.Status,
		&merchant.CreatedAt,
	)
	if err != nil {
		return nil, err
	}
	return merchant, nil
}

func (r *MerchantRepo) GetByIDForUpdate(tx *sql.Tx, merchantID int) (*model.Merchant, error) {
	merchant := &model.Merchant{}
	err := tx.QueryRow(
		`SELECT id, name, balance, merchant_code, category, status, created_at
		 FROM public.merchants
		 WHERE id = $1 AND status = 'ACTIVE' FOR UPDATE`,
		merchantID,
	).Scan(
		&merchant.ID,
		&merchant.Name,
		&merchant.Balance,
		&merchant.MerchantCode,
		&merchant.Category,
		&merchant.Status,
		&merchant.CreatedAt,
	)
	if err != nil {
		return nil, err
	}
	return merchant, nil
}

func (r *MerchantRepo) GetByIDAnyStatusForUpdate(tx *sql.Tx, merchantID int) (*model.Merchant, error) {
	merchant := &model.Merchant{}
	err := tx.QueryRow(
		`SELECT id, name, balance, merchant_code, category, status, created_at
		 FROM public.merchants
		 WHERE id = $1 FOR UPDATE`,
		merchantID,
	).Scan(
		&merchant.ID,
		&merchant.Name,
		&merchant.Balance,
		&merchant.MerchantCode,
		&merchant.Category,
		&merchant.Status,
		&merchant.CreatedAt,
	)
	if err != nil {
		return nil, err
	}
	return merchant, nil
}

// GetByID - mencari merchant berdasarkan ID
func (r *MerchantRepo) GetByID(merchantID int) (*model.Merchant, error) {
	merchant := &model.Merchant{}
	err := r.DB.QueryRow(
		`SELECT id, name, balance, merchant_code, category, status, created_at
		 FROM public.merchants
		 WHERE id = $1`,
		merchantID,
	).Scan(
		&merchant.ID,
		&merchant.Name,
		&merchant.Balance,
		&merchant.MerchantCode,
		&merchant.Category,
		&merchant.Status,
		&merchant.CreatedAt,
	)
	if err != nil {
		return nil, err
	}
	return merchant, nil
}

// GetBalance - ambil saldo merchant
func (r *MerchantRepo) GetBalance(merchantID int) (int64, error) {
	var balance int64
	err := r.DB.QueryRow(
		`SELECT balance FROM public.merchants WHERE id = $1`,
		merchantID,
	).Scan(&balance)
	return balance, err
}

// UpdateBalanceWithTx - update saldo merchant dalam konteks transaksi
func (r *MerchantRepo) UpdateBalanceWithTx(tx *sql.Tx, merchantID int, newBalance int64) error {
	_, err := tx.Exec(
		`UPDATE public.merchants SET balance = $1 WHERE id = $2`,
		newBalance, merchantID,
	)
	return err
}

// GetAll - ambil semua merchant yang aktif
func (r *MerchantRepo) GetAll() ([]model.Merchant, error) {
	rows, err := r.DB.Query(
		`SELECT id, name, balance, merchant_code, category, status, created_at
		 FROM public.merchants
		 WHERE status = 'ACTIVE'
		 ORDER BY id ASC`,
	)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var merchants []model.Merchant
	for rows.Next() {
		var m model.Merchant
		err := rows.Scan(
			&m.ID,
			&m.Name,
			&m.Balance,
			&m.MerchantCode,
			&m.Category,
			&m.Status,
			&m.CreatedAt,
		)
		if err != nil {
			return nil, err
		}
		merchants = append(merchants, m)
	}
	return merchants, nil
}
