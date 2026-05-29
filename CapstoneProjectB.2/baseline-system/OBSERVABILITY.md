# Dashboard Metrik Evaluasi Baseline System

Dashboard ini memakai Prometheus dan Grafana untuk memonitor metrik evaluasi server transaksi baseline.

## Menjalankan Stack

```bash
docker compose up --build
```

Layanan yang tersedia:

- Baseline API: http://localhost:8080
- Prometheus: http://localhost:9090
- Grafana: http://localhost:3000

Login Grafana:

- Username: `admin`
- Password: `admin`

Dashboard otomatis tersedia di folder `Capstone` dengan nama `Baseline Transaction Evaluation`.

## Endpoint Metrik

Backend mengekspos metrik Prometheus di:

```text
GET /metrics
```

Prometheus mengambil metrik dari endpoint tersebut setiap 5 detik.

## Metrik yang Dikumpulkan

Metrik HTTP:

- `baseline_http_requests_total`
- `baseline_http_request_duration_seconds`
- `baseline_app_uptime_seconds`

Metrik bisnis transaksi:

- `baseline_business_operations_total`
- `baseline_business_amount_total`

Label penting:

- `operation`: jenis operasi, misalnya `QRIS_PAYMENT`, `PAYMENT`, `TRANSFER`, `REVERSAL`
- `status`: hasil operasi, misalnya `SUCCESS`, `FAILED`, `TIMEOUT`, `SYSTEM_BUSY`
- `code`: kode respons legacy/baseline
- `path`: endpoint HTTP
- `status_code`: kode status HTTP

## Skenario Evaluasi LK

Gunakan Postman collection `postman-many-transactions-demo.json` untuk menjalankan banyak transaksi ke server. Setelah request dijalankan, buka dashboard Grafana untuk melihat:

- request rate per endpoint
- p95 latency per endpoint
- success rate transaksi
- error rate transaksi
- hasil transaksi QRIS
- total nominal yang diproses

Dashboard ini dapat digunakan sebagai bukti observability untuk mengevaluasi performa, reliabilitas, dan stabilitas server baseline pada alur transaksi QRIS.
