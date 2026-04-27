package cache

import (
	"context"
	"fmt"
	"os"
	"time"

	"github.com/redis/go-redis/v9"
)

var ctx = context.Background()

type RedisCache struct {
	Client *redis.Client
}

func NewRedisCache() *RedisCache {
	host := os.Getenv("REDIS_HOST")
	if host == "" {
		host = "redis"
	}
	port := os.Getenv("REDIS_PORT")
	if port == "" {
		port = "6379"
	}

	client := redis.NewClient(&redis.Options{
		Addr: fmt.Sprintf("%s:%s", host, port),
	})

	return &RedisCache{Client: client}
}

// Set - simpan data ke cache dengan TTL
func (c *RedisCache) Set(key string, value string, ttl time.Duration) error {
	return c.Client.Set(ctx, key, value, ttl).Err()
}

// Get - ambil data dari cache
func (c *RedisCache) Get(key string) (string, error) {
	return c.Client.Get(ctx, key).Result()
}

// Delete - hapus cache (saat data berubah)
func (c *RedisCache) Delete(key string) error {
	return c.Client.Del(ctx, key).Err()
}

// KeyUserBalance - format key cache untuk saldo user
func KeyUserBalance(userID int) string {
	return fmt.Sprintf("balance:user:%d", userID)
}

// KeyMerchantBalance - format key cache untuk saldo merchant
func KeyMerchantBalance(merchantID int) string {
	return fmt.Sprintf("balance:merchant:%d", merchantID)
}

// KeyMerchantByCode - format key cache untuk data merchant
func KeyMerchantByCode(merchantCode string) string {
	return fmt.Sprintf("merchant:code:%s", merchantCode)
}
