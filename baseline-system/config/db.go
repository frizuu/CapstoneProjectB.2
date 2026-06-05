package config

import (
	"database/sql"
	"fmt"
	"os"
	"time"

	_ "github.com/lib/pq"
)

func dbEnvOrDefault(key string, fallback string) string {
	value := os.Getenv(key)
	if value == "" {
		return fallback
	}
	return value
}

func ConnectDB() *sql.DB {
	connStr := fmt.Sprintf(
		"host=%s port=%s user=%s password=%s dbname=%s sslmode=disable",
		dbEnvOrDefault("DB_HOST", "localhost"),
		dbEnvOrDefault("DB_PORT", "5434"),
		dbEnvOrDefault("DB_USER", "postgres"),
		dbEnvOrDefault("DB_PASS", "postgres"),
		dbEnvOrDefault("DB_NAME", "bank"),
	)

	db, err := sql.Open("postgres", connStr)
	if err != nil {
		panic(err)
	}

	// Connection pool: mencegah antrian saat banyak cache miss bersamaan
	db.SetMaxOpenConns(25) // max 25 koneksi aktif ke Postgres
	db.SetMaxIdleConns(25) // keep 25 koneksi idle siap pakai
	db.SetConnMaxLifetime(5 * time.Minute)
	db.SetConnMaxIdleTime(5 * time.Minute)

	return db
}
