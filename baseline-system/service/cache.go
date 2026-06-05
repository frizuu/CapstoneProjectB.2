package service

import (
	"context"
	"fmt"
	"time"

	gocache "github.com/patrickmn/go-cache"
	"github.com/redis/go-redis/v9"
)

const defaultCacheTTL = 5 * time.Minute

var (
	l1Cache   *gocache.Cache
	rdb       *redis.Client
	cacheCtx  = context.Background()
	cacheMode = "none"
)

func CacheMode() string {
	return cacheMode
}

func InitCache(redisAddr string, mode string) error {
	cacheMode = "none"
	l1Cache = nil
	rdb = nil

	if mode == "none" {
		return nil
	}

	if mode != "l1" && mode != "redis" && mode != "full" {
		return fmt.Errorf("unknown cache mode %q; supported modes are none, l1, redis, full", mode)
	}

	if mode == "l1" || mode == "full" {
		l1Cache = gocache.New(5*time.Minute, 10*time.Minute)
		cacheMode = "l1"
	}

	if mode == "redis" || mode == "full" {
		rdb = redis.NewClient(&redis.Options{
			Addr:        redisAddr,
			MaxRetries:  1,
			DialTimeout: 2 * time.Second,
		})
		if err := rdb.Ping(cacheCtx).Err(); err != nil {
			_ = rdb.Close()
			rdb = nil
			if mode == "full" {
				return fmt.Errorf("redis unavailable at %s; using l1 cache only: %w", redisAddr, err)
			}
			return fmt.Errorf("redis unavailable at %s; cache disabled: %w", redisAddr, err)
		}
		cacheMode = mode
	}

	return nil
}

func GetFromL1(key string) (string, bool) {
	if l1Cache == nil {
		return "", false
	}
	value, ok := l1Cache.Get(key)
	if !ok {
		return "", false
	}
	s, ok := value.(string)
	return s, ok
}

func SetL1(key string, value string, ttl time.Duration) {
	if l1Cache == nil {
		return
	}
	l1Cache.Set(key, value, ttl)
}

func GetFromRedis(key string) (string, bool) {
	if rdb == nil {
		return "", false
	}
	value, err := rdb.Get(cacheCtx, key).Result()
	if err != nil {
		return "", false
	}
	return value, true
}

func SetRedis(key string, value string, ttl time.Duration) {
	if rdb == nil {
		return
	}
	rdb.Set(cacheCtx, key, value, ttl)
}

func GetFromCache(key string) (string, bool) {
	if cacheMode == "none" {
		return "", false
	}

	if cacheMode == "l1" || cacheMode == "full" {
		if value, ok := GetFromL1(key); ok {
			return value, true
		}
	}

	if cacheMode == "redis" || cacheMode == "full" {
		if value, ok := GetFromRedis(key); ok {
			if cacheMode == "full" {
				SetL1(key, value, defaultCacheTTL)
			}
			return value, true
		}
	}

	return "", false
}

func SetCache(key string, value string, ttl time.Duration) {
	if cacheMode == "none" {
		return
	}

	if cacheMode == "l1" || cacheMode == "full" {
		SetL1(key, value, ttl)
	}

	if cacheMode == "redis" || cacheMode == "full" {
		SetRedis(key, value, ttl)
	}
}

func DeleteFromCache(key string) {
	if cacheMode == "none" {
		return
	}

	if cacheMode == "l1" || cacheMode == "full" {
		if l1Cache != nil {
			l1Cache.Delete(key)
		}
	}

	if cacheMode == "redis" || cacheMode == "full" {
		if rdb != nil {
			rdb.Del(cacheCtx, key)
		}
	}
}

func InvalidateBalanceCache(userID int) {
	key := fmt.Sprintf("balance:%d", userID)
	DeleteFromCache(key)
}

func InvalidateMerchantBalanceCache(merchantID int) {
	key := fmt.Sprintf("merchant_balance:%d", merchantID)
	DeleteFromCache(key)
}

func InvalidateTransactionCache(userID int) {
	key := fmt.Sprintf("transactions:%d", userID)
	DeleteFromCache(key)
}
