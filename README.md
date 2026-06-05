# CapstoneProjectB.2

Repository ini berisi sistem transaksi sederhana dengan backend Go dan frontend Android Kotlin/Jetpack Compose.

## Struktur Project

- `baseline-system/`: backend Go untuk transaksi, QRIS, balance inquiry, WebSocket notification, Redis cache, RabbitMQ audit worker, PostgreSQL, Prometheus, Grafana, dan Locust.
- `frontend-capstone/`: aplikasi Android untuk user/admin, transfer, pembayaran QRIS, riwayat transaksi, notifikasi, dan integrasi API backend.

## Fitur Utama

- Cek saldo user dan merchant.
- Pembayaran reguler, transfer antar user, dan pembayaran merchant/QRIS.
- Inquiry QRIS berdasarkan `merchant_code`.
- Riwayat transaksi user.
- Status transaksi.
- Reversal transaksi.
- WebSocket untuk notifikasi transaksi.
- Observability dengan Prometheus dan Grafana.
- Load testing dengan Locust.

## Prasyarat

Pastikan sudah terpasang:

- Docker dan Docker Compose
- Go 1.24 atau versi yang kompatibel dengan `baseline-system/go.mod`
- Android Studio
- JDK 11 atau lebih baru

## Menjalankan Backend

Masuk ke folder backend:

```powershell
cd baseline-system
docker compose up --build
```

Service yang akan berjalan:

- Backend API: `http://localhost:8080`
- PostgreSQL: `localhost:5434`
- Redis: `localhost:6380`
- RabbitMQ Management UI: `http://localhost:15674`
- Prometheus: `http://localhost:9090`
- Grafana: `http://localhost:3000`
- Locust: `http://localhost:8089`

Login Grafana default:

```text
username: admin
password: admin
```

Database akan otomatis diisi data awal dari `baseline-system/init.sql`, termasuk 100 user dan 100 merchant.

## Menjalankan Frontend Android

Buka folder `frontend-capstone/` di Android Studio, lalu jalankan aplikasi ke emulator atau device fisik.

Sebelum run, sesuaikan alamat backend di:

```text
frontend-capstone/app/src/main/java/com/example/capstone_frontend/Constants.kt
```

Contoh konfigurasi:

```kotlin
const val SERVER_IP_AND_PORT = "10.0.2.2:8080"
```

Gunakan `10.0.2.2:8080` jika menjalankan backend di laptop dan aplikasi di Android Emulator.

Jika menggunakan device fisik, gunakan IP laptop pada jaringan yang sama, misalnya:

```kotlin
const val SERVER_IP_AND_PORT = "192.168.1.10:8080"
```

Pastikan firewall mengizinkan koneksi ke port `8080`.

## Cara Testing Fungsi Berhasil atau Tidak

### 1. Health check backend

Backend belum memiliki endpoint `/health`, jadi pengecekan paling sederhana adalah memanggil endpoint data:

```powershell
curl "http://localhost:8080/balance?user_id=1"
```

Berhasil jika response berbentuk JSON dan berisi status sukses, data saldo, atau kode response transaksi.

### 2. Cek saldo user

```powershell
curl "http://localhost:8080/balance?user_id=1"
```

Berhasil jika response menampilkan saldo user `1`.

Gagal jika:

- `user_id` kosong.
- `user_id` bukan angka.
- user tidak ditemukan.

### 3. Cek daftar merchant

```powershell
curl "http://localhost:8080/merchants"
```

Berhasil jika response berisi:

```json
{
  "status": "SUCCESS",
  "merchants": []
}
```

Isi `merchants` akan berisi data merchant dari database.

### 4. Inquiry QRIS

Gunakan salah satu `merchant_code` dari seed data, misalnya `NMID877996734914`.

```powershell
curl "http://localhost:8080/qris/inquiry?merchant_code=NMID877996734914"
```

Berhasil jika response mengembalikan informasi merchant dan kode sukses.

Gagal jika `merchant_code` kosong atau tidak ditemukan.

### 5. Pembayaran merchant

```powershell
curl -X POST "http://localhost:8080/payment" `
  -H "Content-Type: application/json" `
  -d "{\"user_id\":1,\"merchant_id\":1,\"amount\":1000,\"reference_no\":\"TEST-PAYMENT-001\"}"
```

Berhasil jika response berisi status sukses dan `transaction_id`.

Catatan:

- `reference_no` bersifat unik.
- Jika request dengan `reference_no` yang sama dikirim ulang, backend dapat mengembalikan hasil idempotent.
- Jika saldo user tidak cukup, transaksi akan gagal.

### 6. Transfer antar user

```powershell
curl -X POST "http://localhost:8080/payment" `
  -H "Content-Type: application/json" `
  -d "{\"user_id\":1,\"recipient_user_id\":2,\"amount\":1000,\"reference_no\":\"TEST-TRANSFER-001\"}"
```

Berhasil jika saldo user pengirim berkurang, saldo penerima bertambah, dan response berisi transaksi sukses.

### 7. Pembayaran QRIS

```powershell
curl -X POST "http://localhost:8080/qris/payment" `
  -H "Content-Type: application/json" `
  -H "Idempotency-Key: TEST-QRIS-001" `
  -d "{\"user_id\":1,\"merchant_code\":\"NMID877996734914\",\"amount\":1000,\"reference_no\":\"TEST-QRIS-001\"}"
```

Berhasil jika response berisi status sukses dan `transaction_id`.

### 8. Cek riwayat transaksi

```powershell
curl "http://localhost:8080/transactions?user_id=1"
```

Berhasil jika response berisi daftar transaksi user.

### 9. Cek status transaksi

Ganti `1` dengan `transaction_id` yang ingin dicek.

```powershell
curl "http://localhost:8080/transaction/status?transaction_id=1"
```

Berhasil jika response menampilkan status transaksi.

### 10. Reversal transaksi

Ganti `1` dengan transaksi yang statusnya bisa direversal.

```powershell
curl -X POST "http://localhost:8080/transaction/reversal" `
  -H "Content-Type: application/json" `
  -d "{\"transaction_id\":1,\"reference_no\":\"TEST-REVERSAL-001\"}"
```

Berhasil jika response menunjukkan transaksi reversal berhasil.

## Testing Otomatis Backend

Masuk ke folder backend:

```powershell
cd baseline-system
go test ./...
```

Test dianggap berhasil jika output berakhir tanpa `FAIL`.

## Testing Frontend Android

Masuk ke folder frontend:

```powershell
cd frontend-capstone
.\gradlew.bat test
```

Untuk instrumented test, jalankan emulator/device terlebih dahulu, lalu:

```powershell
.\gradlew.bat connectedAndroidTest
```

Test dianggap berhasil jika Gradle menampilkan `BUILD SUCCESSFUL`.

## Testing Manual di Aplikasi Android

1. Jalankan backend dengan `docker compose up --build`.
2. Pastikan `Constants.SERVER_IP_AND_PORT` sudah sesuai.
3. Run aplikasi dari Android Studio.
4. Coba fitur utama:
   - login/pilih user sesuai flow aplikasi,
   - cek saldo,
   - lakukan transfer/pembayaran,
   - scan atau input QRIS,
   - buka riwayat transaksi,
   - cek notifikasi.
5. Bandingkan hasil di aplikasi dengan response API menggunakan `curl`.

Fungsi dianggap berhasil jika:

- aplikasi tidak crash,
- response API sukses,
- saldo berubah sesuai nominal transaksi,
- transaksi muncul di riwayat,
- status transaksi sesuai dengan hasil pembayaran,
- notifikasi muncul ketika event transaksi diterima.

## Load Testing dengan Locust

Backend menyediakan Locust melalui Docker Compose.

1. Jalankan semua service:

```powershell
cd baseline-system
docker compose up --build
```

2. Buka:

```text
http://localhost:8089
```

3. Masukkan konfigurasi jumlah user dan spawn rate.
4. Jalankan test dan lihat statistik request.

Locust akan menguji endpoint seperti `/balance`, `/transactions`, `/qris/inquiry`, `/merchant/balance`, `/transaction/status`, dan `/payment`.

## Observability

Metrics tersedia di:

```text
http://localhost:8080/metrics
```

Prometheus:

```text
http://localhost:9090
```

Grafana dashboard:

```text
http://localhost:3000
```

Detail tambahan ada di:

```text
baseline-system/OBSERVABILITY.md
```

## Troubleshooting

- Jika Android Emulator tidak bisa terhubung ke backend, gunakan `10.0.2.2:8080`.
- Jika device fisik tidak bisa terhubung, pastikan device dan laptop berada di Wi-Fi yang sama.
- Jika database tidak terisi, hapus volume Docker lalu jalankan ulang compose:

```powershell
cd baseline-system
docker compose down -v
docker compose up --build
```

- Jika port bentrok, hentikan service lain yang memakai port `8080`, `5434`, `6380`, `5672`, `15674`, `9090`, `3000`, atau `8089`.
- Jika transaksi gagal, cek saldo user dan pastikan `reference_no` belum pernah dipakai.
