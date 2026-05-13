package legacy

import (
	"fmt"
	"math/rand"
	"os"
	"strings"
	"time"

	"baseline-system/model"
)

const (
	TypePayment  = "PAYMENT"
	TypeQRIS     = "QRIS_PAYMENT"
	TypeTransfer = "USER_TRANSFER"

	StatusSuccess      = "SUCCESS"
	StatusFailed       = "FAILED"
	StatusTimeout      = "TIMEOUT"
	StatusSystemBusy   = "SYSTEM_BUSY"
	StatusInvalidInput = "INVALID_REQUEST"

	defaultChannel = "MOBILE_QRIS"
)

type TransactionRequest struct {
	Type            string
	ReferenceNo     string
	Channel         string
	UserID          int
	RecipientUserID int
	MerchantCode    string
	Amount          int
	RequestedTime   time.Time
}

type TransactionResult struct {
	Status       string
	Code         string
	Message      string
	HostRef      string
	NeedReversal bool
	LatencyMs    int64
	Profile      string
}

type networkProfile struct {
	Name                 string
	MinDelayMs           int
	MaxDelayMs           int
	JitterMs             int
	PacketLossPercent    int
	TimeoutPercent       int
	SystemBusyPercent    int
	FailedPercent        int
	AmbiguousTimeoutRate int
}

func init() {
	rand.Seed(time.Now().UnixNano())
}

func resolveNetworkProfile() networkProfile {
	switch strings.ToLower(os.Getenv("LEGACY_NETWORK_PROFILE")) {
	case "rural":
		return networkProfile{
			Name:                 "rural",
			MinDelayMs:           1200,
			MaxDelayMs:           4500,
			JitterMs:             1800,
			PacketLossPercent:    5,
			TimeoutPercent:       14,
			SystemBusyPercent:    5,
			FailedPercent:        6,
			AmbiguousTimeoutRate: 35,
		}
	case "peak":
		return networkProfile{
			Name:                 "peak",
			MinDelayMs:           900,
			MaxDelayMs:           3200,
			JitterMs:             900,
			PacketLossPercent:    2,
			TimeoutPercent:       10,
			SystemBusyPercent:    8,
			FailedPercent:        6,
			AmbiguousTimeoutRate: 25,
		}
	default:
		return networkProfile{
			Name:                 "normal",
			MinDelayMs:           350,
			MaxDelayMs:           1500,
			JitterMs:             250,
			PacketLossPercent:    1,
			TimeoutPercent:       5,
			SystemBusyPercent:    3,
			FailedPercent:        6,
			AmbiguousTimeoutRate: 10,
		}
	}
}

func generateHostRef(req *TransactionRequest) string {
	now := time.Now()
	return fmt.Sprintf("AS400-%s-%06d-%04d", now.Format("20060102150405"), req.UserID, rand.Intn(10000))
}

func prepareRequest(req *TransactionRequest) {
	if req.ReferenceNo == "" {
		req.ReferenceNo = fmt.Sprintf("REF-%d-%d-%04d", req.UserID, time.Now().UnixNano(), rand.Intn(10000))
	}
	if req.Channel == "" {
		req.Channel = defaultChannel
	}
	if req.RequestedTime.IsZero() {
		req.RequestedTime = time.Now()
	}
}

func simulateAS400Delay(profile networkProfile, stage string) time.Duration {
	spread := profile.MaxDelayMs - profile.MinDelayMs
	if spread <= 0 {
		spread = 1
	}

	delay := profile.MinDelayMs + rand.Intn(spread)
	if profile.JitterMs > 0 {
		delay += rand.Intn(profile.JitterMs)
	}

	// Different AS/400 stages are sequential and blocking, but not equally heavy.
	switch stage {
	case "VALIDATION":
		delay = delay / 5
	case "RULES":
		delay = delay / 4
	case "FRAUD":
		delay = delay / 3
	case "POSTING":
		delay = delay / 2
	}

	time.Sleep(time.Duration(delay) * time.Millisecond)
	return time.Duration(delay) * time.Millisecond
}

func simulateAS400BatchResponse(operation string, profile networkProfile) string {
	if operation == "" {
		return StatusInvalidInput
	}

	percent := rand.Intn(100)
	timeoutLimit := profile.TimeoutPercent + profile.PacketLossPercent
	busyLimit := timeoutLimit + profile.SystemBusyPercent
	failedLimit := busyLimit + profile.FailedPercent

	switch {
	case percent < timeoutLimit:
		return StatusTimeout
	case percent < busyLimit:
		return StatusSystemBusy
	case percent < failedLimit:
		return StatusFailed
	default:
		return StatusSuccess
	}
}

func processLegacyCore(operation string, profile networkProfile) (string, time.Duration) {
	latency := simulateAS400Delay(profile, "POSTING")
	return simulateAS400BatchResponse(operation, profile), latency
}

func validateRequest(req *TransactionRequest, user *model.User, merchant *model.Merchant) *TransactionResult {
	if req == nil || req.UserID == 0 || req.Amount <= 0 {
		return &TransactionResult{Status: StatusInvalidInput, Code: "14", Message: "invalid transaction request"}
	}

	if req.Amount < 100 {
		return &TransactionResult{Status: StatusFailed, Code: "13", Message: "amount below minimum threshold"}
	}

	if user == nil {
		return &TransactionResult{Status: StatusInvalidInput, Code: "14", Message: "user not found"}
	}

	if user.ID%97 == 0 {
		return &TransactionResult{Status: StatusFailed, Code: "76", Message: "account marked dormant by legacy host"}
	}

	if user.ID%89 == 0 {
		return &TransactionResult{Status: StatusFailed, Code: "78", Message: "account blocked by legacy host"}
	}

	if req.Type == TypeQRIS || (req.Type == TypePayment && merchant != nil) {
		if merchant == nil {
			return &TransactionResult{Status: StatusInvalidInput, Code: "15", Message: "merchant lookup failed"}
		}
		if merchant.Status != "ACTIVE" {
			return &TransactionResult{Status: StatusFailed, Code: "16", Message: "merchant is not active"}
		}
	}

	if user.Balance < req.Amount {
		return &TransactionResult{Status: StatusFailed, Code: "51", Message: "insufficient funds"}
	}

	if req.Type == TypeTransfer && req.RecipientUserID == req.UserID {
		return &TransactionResult{Status: StatusInvalidInput, Code: "12", Message: "sender and recipient cannot be the same user"}
	}

	return nil
}

// evaluateBusinessRules enforces banking rules on transaction (AS/400 rule engine)
func evaluateBusinessRules(req *TransactionRequest, user *model.User, merchant *model.Merchant) *TransactionResult {
	if req.Amount > 10000000 {
		return &TransactionResult{Status: StatusFailed, Code: "61", Message: "amount exceeds single-transaction limit"}
	}

	if req.Type == TypeQRIS && req.Amount > 7500000 {
		return &TransactionResult{Status: StatusFailed, Code: "65", Message: "amount exceeds QRIS channel limit"}
	}

	if req.Type == TypeQRIS && merchant != nil && merchant.Category == "INACTIVE" {
		return &TransactionResult{Status: StatusFailed, Code: "62", Message: "merchant is not allowed for QRIS settlement"}
	}

	if isLegacyCutoffWindow(req.RequestedTime) {
		return &TransactionResult{Status: StatusSystemBusy, Code: "90", Message: "legacy host is in end-of-day cutover window"}
	}

	return nil
}

func isLegacyCutoffWindow(t time.Time) bool {
	if t.IsZero() {
		t = time.Now()
	}

	// Short host cutover window to mimic EOD/batch behavior without blocking normal demos.
	return t.Hour() == 23 && t.Minute() >= 55
}

// fraudCheck performs batch fraud scoring (simulated AS/400 fraud engine)
func fraudCheck(req *TransactionRequest, user *model.User, merchant *model.Merchant) *TransactionResult {
	if req.Amount > 5000000 {
		percent := rand.Intn(100)
		if percent < 30 {
			return &TransactionResult{Status: StatusFailed, Code: "79", Message: "transaction flagged by fraud rules"}
		}
	}
	return nil
}

// ExecuteTransaction orchestrates the full AS/400-style transaction pipeline
// 1. Validation (input screening)
// 2. Rules evaluation (business logic)
// 3. Fraud check (risk scoring)
// 4. Legacy core processing (batch core)
// Entire flow is blocking and sequential with integrated audit trail
func ExecuteTransaction(req *TransactionRequest, user *model.User, merchant *model.Merchant) TransactionResult {
	start := time.Now()
	profile := resolveNetworkProfile()
	prepareRequest(req)

	// STAGE 1: Input Validation (Sequential)
	simulateAS400Delay(profile, "VALIDATION")
	if validation := validateRequest(req, user, merchant); validation != nil {
		validation.Profile = profile.Name
		validation.LatencyMs = time.Since(start).Milliseconds()
		return *validation
	}

	// STAGE 2: Rules Evaluation (Blocking)
	simulateAS400Delay(profile, "RULES")
	if rule := evaluateBusinessRules(req, user, merchant); rule != nil {
		rule.Profile = profile.Name
		rule.LatencyMs = time.Since(start).Milliseconds()
		return *rule
	}

	// STAGE 3: Fraud Check (Scoring Engine)
	simulateAS400Delay(profile, "FRAUD")
	if fraud := fraudCheck(req, user, merchant); fraud != nil {
		fraud.Profile = profile.Name
		fraud.LatencyMs = time.Since(start).Milliseconds()
		return *fraud
	}

	// STAGE 4: Monolithic Core Processing (AS/400 Batch)
	hostRef := generateHostRef(req)
	status, _ := processLegacyCore(req.Type, profile)
	if status != StatusSuccess {
		needReversal := status == StatusTimeout && rand.Intn(100) < profile.AmbiguousTimeoutRate
		message := "legacy core returned " + status
		if needReversal {
			message = "legacy host timed out after possible posting; reversal check required"
		}

		return TransactionResult{
			Status:       status,
			Code:         "96",
			Message:      message,
			HostRef:      hostRef,
			NeedReversal: needReversal,
			LatencyMs:    time.Since(start).Milliseconds(),
			Profile:      profile.Name,
		}
	}

	return TransactionResult{
		Status:    StatusSuccess,
		Code:      "00",
		Message:   "Transaction successful",
		HostRef:   hostRef,
		LatencyMs: time.Since(start).Milliseconds(),
		Profile:   profile.Name,
	}
}
