package legacy

import (
	"math/rand"
	"time"
)

func ProcessTransaction() string {
	delay := rand.Intn(500) + 300
	time.Sleep(time.Duration(delay) * time.Millisecond)

	return "SUCCESS"
}