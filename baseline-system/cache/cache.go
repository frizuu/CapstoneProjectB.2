package cache

import (
	"context"
	"fmt"
	"time"

	"github.com/redis/go-redis/v9"
)

// Use a background context for standard cache operations
var ctx = context.Background()

// RedisCache acts as a wrapper around the redis client
type RedisCache struct {
	client *redis.Client
}

// NewRedisCache initializes a new Redis connection
func NewRedisCache(addr string, password string, db int) *RedisCache {
	client := redis.NewClient(&redis.Options{
		Addr:     addr,     // e.g., "localhost:6379"
		Password: password, // no password set by default
		DB:       db,       // use default DB (usually 0)
	})

	return &RedisCache{
		client: client,
	}
}

// Set stores a key-value pair in Redis with an optional expiration time
func (c *RedisCache) Set(key string, value interface{}, expiration time.Duration) error {
	return c.client.Set(ctx, key, value, expiration).Err()
}

// Get retrieves a string value from Redis by its key
func (c *RedisCache) Get(key string) (string, error) {
	val, err := c.client.Get(ctx, key).Result()
	if err != nil {
		return "", err
	}
	return val, nil
}

// Delete removes a key from Redis
func (c *RedisCache) Delete(key string) error {
	return c.client.Del(ctx, key).Err()
}

// --- Key Generation Helpers ---
// These match the calls made in your transaction_service.go

// KeyUserBalance generates a standard Redis key for a user's balance cache
func KeyUserBalance(userID int) string {
	return fmt.Sprintf("user_balance:%d", userID)
}

// KeyMerchantBalance generates a standard Redis key for a merchant's balance cache
func KeyMerchantBalance(merchantID int) string {
	return fmt.Sprintf("merchant_balance:%d", merchantID)
}
