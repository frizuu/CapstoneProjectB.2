package metrics

import (
	"fmt"
	"net/http"
	"sort"
	"strings"
	"sync"
	"time"
)

var durationBuckets = []float64{0.05, 0.1, 0.25, 0.5, 1, 2, 5, 10}

type httpKey struct {
	Method     string
	Path       string
	StatusCode int
}

type durationKey struct {
	Method string
	Path   string
}

type businessKey struct {
	Operation string
	Status    string
	Code      string
}

type durationMetric struct {
	Buckets []uint64
	Count   uint64
	Sum     float64
}

type statusRecorder struct {
	http.ResponseWriter
	statusCode int
}

var (
	mu sync.Mutex

	startedAt = time.Now()

	httpRequests = map[httpKey]uint64{}
	httpDuration = map[durationKey]*durationMetric{}

	businessOperations = map[businessKey]uint64{}
	businessAmount     = map[businessKey]int64{}
)

func InstrumentHTTP(path string, next http.HandlerFunc) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		start := time.Now()
		recorder := &statusRecorder{
			ResponseWriter: w,
			statusCode:     http.StatusOK,
		}

		next(recorder, r)

		RecordHTTPRequest(r.Method, path, recorder.statusCode, time.Since(start).Seconds())
	}
}

func (r *statusRecorder) WriteHeader(statusCode int) {
	r.statusCode = statusCode
	r.ResponseWriter.WriteHeader(statusCode)
}

func RecordHTTPRequest(method string, path string, statusCode int, durationSeconds float64) {
	key := httpKey{
		Method:     method,
		Path:       path,
		StatusCode: statusCode,
	}
	durationLabel := durationKey{
		Method: method,
		Path:   path,
	}

	mu.Lock()
	defer mu.Unlock()

	httpRequests[key]++

	metric := httpDuration[durationLabel]
	if metric == nil {
		metric = &durationMetric{
			Buckets: make([]uint64, len(durationBuckets)),
		}
		httpDuration[durationLabel] = metric
	}

	for i, bucket := range durationBuckets {
		if durationSeconds <= bucket {
			metric.Buckets[i]++
		}
	}
	metric.Count++
	metric.Sum += durationSeconds
}

func RecordBusinessOperation(operation string, status string, code string, amount int) {
	if status == "" {
		status = "UNKNOWN"
	}
	if code == "" {
		code = "none"
	}

	key := businessKey{
		Operation: normalizeLabel(operation),
		Status:    normalizeLabel(status),
		Code:      normalizeLabel(code),
	}

	mu.Lock()
	defer mu.Unlock()

	businessOperations[key]++
	if amount > 0 {
		businessAmount[key] += int64(amount)
	}
}

func Handler(w http.ResponseWriter, r *http.Request) {
	mu.Lock()
	defer mu.Unlock()

	w.Header().Set("Content-Type", "text/plain; version=0.0.4; charset=utf-8")

	fmt.Fprintln(w, "# HELP baseline_app_uptime_seconds Application uptime in seconds.")
	fmt.Fprintln(w, "# TYPE baseline_app_uptime_seconds gauge")
	fmt.Fprintf(w, "baseline_app_uptime_seconds %.0f\n\n", time.Since(startedAt).Seconds())

	writeHTTPRequests(w)
	writeHTTPDuration(w)
	writeBusinessOperations(w)
}

func writeHTTPRequests(w http.ResponseWriter) {
	fmt.Fprintln(w, "# HELP baseline_http_requests_total Total HTTP requests received by the baseline server.")
	fmt.Fprintln(w, "# TYPE baseline_http_requests_total counter")

	keys := make([]httpKey, 0, len(httpRequests))
	for key := range httpRequests {
		keys = append(keys, key)
	}
	sort.Slice(keys, func(i, j int) bool {
		return fmt.Sprintf("%s%s%d", keys[i].Method, keys[i].Path, keys[i].StatusCode) <
			fmt.Sprintf("%s%s%d", keys[j].Method, keys[j].Path, keys[j].StatusCode)
	})

	for _, key := range keys {
		fmt.Fprintf(
			w,
			"baseline_http_requests_total{method=%q,path=%q,status_code=%q} %d\n",
			escapeLabelValue(key.Method),
			escapeLabelValue(key.Path),
			fmt.Sprintf("%d", key.StatusCode),
			httpRequests[key],
		)
	}
	fmt.Fprintln(w)
}

func writeHTTPDuration(w http.ResponseWriter) {
	fmt.Fprintln(w, "# HELP baseline_http_request_duration_seconds HTTP request duration in seconds.")
	fmt.Fprintln(w, "# TYPE baseline_http_request_duration_seconds histogram")

	keys := make([]durationKey, 0, len(httpDuration))
	for key := range httpDuration {
		keys = append(keys, key)
	}
	sort.Slice(keys, func(i, j int) bool {
		return fmt.Sprintf("%s%s", keys[i].Method, keys[i].Path) <
			fmt.Sprintf("%s%s", keys[j].Method, keys[j].Path)
	})

	for _, key := range keys {
		metric := httpDuration[key]
		for i, bucket := range durationBuckets {
			fmt.Fprintf(
				w,
				"baseline_http_request_duration_seconds_bucket{method=%q,path=%q,le=%q} %d\n",
				escapeLabelValue(key.Method),
				escapeLabelValue(key.Path),
				fmt.Sprintf("%.2f", bucket),
				metric.Buckets[i],
			)
		}
		fmt.Fprintf(
			w,
			"baseline_http_request_duration_seconds_bucket{method=%q,path=%q,le=%q} %d\n",
			escapeLabelValue(key.Method),
			escapeLabelValue(key.Path),
			"+Inf",
			metric.Count,
		)
		fmt.Fprintf(
			w,
			"baseline_http_request_duration_seconds_sum{method=%q,path=%q} %.6f\n",
			escapeLabelValue(key.Method),
			escapeLabelValue(key.Path),
			metric.Sum,
		)
		fmt.Fprintf(
			w,
			"baseline_http_request_duration_seconds_count{method=%q,path=%q} %d\n",
			escapeLabelValue(key.Method),
			escapeLabelValue(key.Path),
			metric.Count,
		)
	}
	fmt.Fprintln(w)
}

func writeBusinessOperations(w http.ResponseWriter) {
	fmt.Fprintln(w, "# HELP baseline_business_operations_total Total baseline business operations grouped by operation, status, and code.")
	fmt.Fprintln(w, "# TYPE baseline_business_operations_total counter")

	keys := make([]businessKey, 0, len(businessOperations))
	for key := range businessOperations {
		keys = append(keys, key)
	}
	sort.Slice(keys, func(i, j int) bool {
		return fmt.Sprintf("%s%s%s", keys[i].Operation, keys[i].Status, keys[i].Code) <
			fmt.Sprintf("%s%s%s", keys[j].Operation, keys[j].Status, keys[j].Code)
	})

	for _, key := range keys {
		fmt.Fprintf(
			w,
			"baseline_business_operations_total{operation=%q,status=%q,code=%q} %d\n",
			escapeLabelValue(key.Operation),
			escapeLabelValue(key.Status),
			escapeLabelValue(key.Code),
			businessOperations[key],
		)
	}
	fmt.Fprintln(w)

	fmt.Fprintln(w, "# HELP baseline_business_amount_total Total amount processed by baseline business operations.")
	fmt.Fprintln(w, "# TYPE baseline_business_amount_total counter")
	for _, key := range keys {
		fmt.Fprintf(
			w,
			"baseline_business_amount_total{operation=%q,status=%q,code=%q} %d\n",
			escapeLabelValue(key.Operation),
			escapeLabelValue(key.Status),
			escapeLabelValue(key.Code),
			businessAmount[key],
		)
	}
	fmt.Fprintln(w)
}

func normalizeLabel(value string) string {
	value = strings.TrimSpace(value)
	if value == "" {
		return "UNKNOWN"
	}
	return strings.ToUpper(value)
}

func escapeLabelValue(value string) string {
	value = strings.ReplaceAll(value, "\\", "\\\\")
	value = strings.ReplaceAll(value, "\n", "\\n")
	value = strings.ReplaceAll(value, "\"", "\\\"")
	return value
}
