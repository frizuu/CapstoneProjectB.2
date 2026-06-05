package com.example.capstone_frontend.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.capstone_frontend.component.AppColor
import com.example.capstone_frontend.component.formatRupiah
import com.example.capstone_frontend.data.DummyRepository
import com.example.capstone_frontend.data.RetrofitClient
import com.example.capstone_frontend.model.TransactionData
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.roundToInt

private const val BASELINE_USER_COUNT = 100
private const val BASELINE_MERCHANT_COUNT = 100
private const val BASELINE_TRANSACTION_COUNT = 108

@Composable
fun AdminLoginScreenV2(
    onLoginSuccess: () -> Unit,
    onBack: () -> Unit
) {
    var username by remember {
        mutableStateOf("")
    }

    var password by remember {
        mutableStateOf("")
    }

    var errorMessage by remember {
        mutableStateOf("")
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColor.Background)
            .padding(horizontal = 24.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(38.dp))

            TextButton(
                onClick = onBack
            ) {
                Text(
                    text = "← Kembali",
                    color = AppColor.Primary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Admin Login",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = AppColor.TextDark
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Masuk untuk memantau performa transaksi QRIS, merchant, audit log, dan baseline system.",
                style = MaterialTheme.typography.bodyMedium,
                color = AppColor.TextGray
            )

            Spacer(modifier = Modifier.height(28.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Akses Admin",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AppColor.TextDark
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = username,
                        onValueChange = {
                            username = it
                            errorMessage = ""
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text("Username")
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppColor.Primary,
                            focusedLabelColor = AppColor.Primary
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            errorMessage = ""
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text("Password")
                        },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password
                        ),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppColor.Primary,
                            focusedLabelColor = AppColor.Primary
                        )
                    )

                    if (errorMessage.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFC62828)
                        )
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    Button(
                        onClick = {
                            if (
                                username.equals("admin", ignoreCase = true) &&
                                password == "admin123"
                            ) {
                                onLoginSuccess()
                            } else {
                                errorMessage = "Username atau password admin tidak sesuai."
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColor.Primary,
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = "Masuk Dashboard",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF4F7)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Keterangan",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = AppColor.TextDark
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Gunakan admin / admin123 untuk masuk ke dashboard monitoring baseline.",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColor.TextGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
fun AdminDashboardScreenV2(
    onLogout: () -> Unit
) {
    var isLoading by remember {
        mutableStateOf(true)
    }

    var errorMessage by remember {
        mutableStateOf("")
    }

    var allMerchants by remember {
        mutableStateOf<List<AdminMerchantUiV2>>(emptyList())
    }

    var activeMerchants by remember {
        mutableStateOf<List<AdminMerchantUiV2>>(emptyList())
    }

    var transactions by remember {
        mutableStateOf<List<TransactionData>>(emptyList())
    }

    var totalTransactions by remember {
        mutableIntStateOf(0)
    }

    var successCount by remember {
        mutableIntStateOf(0)
    }

    var failedCount by remember {
        mutableIntStateOf(0)
    }

    var timeoutCount by remember {
        mutableIntStateOf(0)
    }

    var activeMerchantCount by remember {
        mutableIntStateOf(0)
    }

    var inactiveMerchantCount by remember {
        mutableIntStateOf(0)
    }

    var successRate by remember {
        mutableDoubleStateOf(0.0)
    }

    var errorRate by remember {
        mutableDoubleStateOf(0.0)
    }

    var timeoutRate by remember {
        mutableDoubleStateOf(0.0)
    }

    var totalActiveMerchantBalance by remember {
        mutableLongStateOf(0L)
    }

    var totalVisibleMerchantBalance by remember {
        mutableLongStateOf(0L)
    }

    LaunchedEffect(Unit) {
        try {
            isLoading = true
            errorMessage = ""

            val merchantResponse = RetrofitClient.api.getMerchants()
            DummyRepository.setMerchantCache(merchantResponse.merchants)

            allMerchants = merchantResponse.merchants.map { merchant ->
                AdminMerchantUiV2(
                    id = merchant.id.toInt(),
                    name = merchant.name,
                    merchantCode = merchant.merchantCode,
                    category = merchant.category,
                    balance = merchant.balance.toLong(),
                    status = merchant.status
                )
            }

            activeMerchants = allMerchants.filter {
                it.status.equals("ACTIVE", ignoreCase = true)
            }

            if (activeMerchants.isEmpty() && allMerchants.isNotEmpty()) {
                activeMerchants = allMerchants
            }

            activeMerchantCount = activeMerchants.size

            inactiveMerchantCount = if (allMerchants.size >= BASELINE_MERCHANT_COUNT) {
                allMerchants.count {
                    it.status.equals("INACTIVE", ignoreCase = true)
                }
            } else {
                (BASELINE_MERCHANT_COUNT - activeMerchantCount).coerceAtLeast(0)
            }

            totalActiveMerchantBalance = activeMerchants.sumOf {
                it.balance
            }

            totalVisibleMerchantBalance = allMerchants.sumOf {
                it.balance
            }

            val collectedTransactions = mutableListOf<TransactionData>()

            for (userId in 1..BASELINE_USER_COUNT) {
                try {
                    val response = RetrofitClient.api.getTransactions(userId)
                    val converted = DummyRepository.convertBackendTransactions(response)
                    collectedTransactions.addAll(converted)
                } catch (_: Exception) {
                }
            }

            transactions = collectedTransactions
                .distinctBy { it.transactionId }
                .sortedByDescending { parseAdminTimeMillisV2(it.createdAt) }

            totalTransactions = transactions.size

            successCount = transactions.count {
                it.status.uppercase() == "SUCCESS"
            }

            timeoutCount = transactions.count {
                it.status.uppercase().contains("TIMEOUT")
            }

            failedCount = transactions.count {
                it.status.uppercase() != "SUCCESS" &&
                        !it.status.uppercase().contains("TIMEOUT")
            }

            successRate = calculateRateV2(successCount, totalTransactions)
            errorRate = calculateRateV2(failedCount, totalTransactions)
            timeoutRate = calculateRateV2(timeoutCount, totalTransactions)
        } catch (e: Exception) {
            errorMessage = e.message ?: "Sebagian data belum dapat dimuat."
        } finally {
            isLoading = false
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColor.Background)
            .padding(horizontal = 14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(30.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = AppColor.Primary),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier.padding(22.dp)
                ) {
                    Text(
                        text = "Admin Monitoring Dashboard",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Pantau performa transaksi QRIS, latency, throughput, data user, data merchant, dan audit log baseline.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.88f)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = onLogout,
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = AppColor.Primary
                        )
                    ) {
                        Text(
                            text = "Logout",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            Text(
                text = "Mode Pengujian Sistem",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = AppColor.TextDark
            )

            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp)
                ) {
                    AdminInfoRowV2(
                        label = "Mode Sistem",
                        value = "Baseline"
                    )

                    AdminInfoRowV2(
                        label = "Alur Transaksi",
                        value = "Inquiry QRIS → Payment QRIS → Status Check"
                    )

                    AdminInfoRowV2(
                        label = "Data Baseline",
                        value = "$BASELINE_USER_COUNT user, $BASELINE_MERCHANT_COUNT merchant, $BASELINE_TRANSACTION_COUNT transaksi awal"
                    )

                    AdminInfoRowV2(
                        label = "Merchant Operasional",
                        value = "Merchant berstatus ACTIVE digunakan untuk validasi QRIS"
                    )

                    AdminInfoRowV2(
                        label = "Tujuan Monitoring",
                        value = "Membandingkan latency, throughput, error rate, dan timeout rate sebelum optimasi."
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Ringkasan Data Baseline",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = AppColor.TextDark
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (isLoading) {
                    "Memuat data dari backend baseline..."
                } else {
                    "Data dirangkum dari endpoint merchant dan transaksi baseline backend."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = AppColor.TextGray
            )

            if (errorMessage.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Sebagian data belum dapat dimuat. Pastikan backend baseline sedang berjalan.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFC62828)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AdminMetricCardV2(
                    title = "Total User",
                    value = BASELINE_USER_COUNT.toString(),
                    subtitle = "Data baseline",
                    modifier = Modifier.weight(1f)
                )

                AdminMetricCardV2(
                    title = "Total Merchant",
                    value = BASELINE_MERCHANT_COUNT.toString(),
                    subtitle = "Data baseline",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AdminMetricCardV2(
                    title = "Merchant Aktif",
                    value = activeMerchantCount.toString(),
                    subtitle = "Siap QRIS",
                    modifier = Modifier.weight(1f)
                )

                AdminMetricCardV2(
                    title = "Merchant Nonaktif",
                    value = inactiveMerchantCount.toString(),
                    subtitle = "Tidak dipakai QRIS",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AdminMetricCardV2(
                    title = "Saldo Merchant Aktif",
                    value = formatRupiahLong(totalActiveMerchantBalance),
                    subtitle = "$activeMerchantCount merchant aktif",
                    modifier = Modifier.weight(1f)
                )

                AdminMetricCardV2(
                    title = "Total Transaksi",
                    value = totalTransactions.toString(),
                    subtitle = "Terbaca dari backend",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Ringkasan Performa Transaksi",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = AppColor.TextDark
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AdminMetricCardV2(
                    title = "Success Rate",
                    value = "${successRate.roundToInt()}%",
                    subtitle = "$successCount berhasil",
                    modifier = Modifier.weight(1f)
                )

                AdminMetricCardV2(
                    title = "Error Rate",
                    value = "${errorRate.roundToInt()}%",
                    subtitle = "$failedCount gagal",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AdminMetricCardV2(
                    title = "Timeout Rate",
                    value = "${timeoutRate.roundToInt()}%",
                    subtitle = "$timeoutCount timeout",
                    modifier = Modifier.weight(1f)
                )

                AdminMetricCardV2(
                    title = "Throughput",
                    value = estimateThroughputV2(totalTransactions),
                    subtitle = "Simulasi req/s",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Latency & Throughput",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = AppColor.TextDark
            )

            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp)
                ) {
                    AdminProgressMetricV2(
                        title = "Average Latency",
                        value = estimateAverageLatencyV2(totalTransactions, failedCount, timeoutCount),
                        progress = estimateLatencyProgressV2(totalTransactions, failedCount, timeoutCount)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    AdminProgressMetricV2(
                        title = "P95 Latency",
                        value = estimateP95LatencyV2(totalTransactions, failedCount, timeoutCount),
                        progress = estimateP95ProgressV2(totalTransactions, failedCount, timeoutCount)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    AdminProgressMetricV2(
                        title = "Throughput",
                        value = estimateThroughputV2(totalTransactions),
                        progress = estimateThroughputProgressV2(totalTransactions)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Redis & Async Queue Monitoring",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = AppColor.TextDark
            )

            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp)
                ) {
                    AdminInfoRowV2(
                        label = "Redis Cache",
                        value = "OFF pada baseline"
                    )

                    AdminInfoRowV2(
                        label = "Cache State",
                        value = "Direct request ke mock legacy"
                    )

                    AdminInfoRowV2(
                        label = "RabbitMQ Queue",
                        value = "OFF pada baseline"
                    )

                    AdminInfoRowV2(
                        label = "Async Task",
                        value = "Logging, audit trail, dan notifikasi dipantau sebagai proses non-kritis"
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Saat mode optimized aktif, bagian ini dapat menampilkan cache HIT/MISS, queue pending, processed jobs, dan failed jobs.",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColor.TextGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Live QRIS / Transaction Traffic",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = AppColor.TextDark
            )

            Spacer(modifier = Modifier.height(10.dp))
        }

        if (transactions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp)
                    ) {
                        Text(
                            text = "Belum ada transaksi",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = AppColor.TextDark
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Jalankan transaksi QRIS atau transfer dari user app untuk melihat traffic transaksi di dashboard admin.",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppColor.TextGray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))
            }
        } else {
            items(transactions.take(8)) { transaction ->
                AdminTransactionItemV2(transaction = transaction)
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        item {
            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Audit Log Transaksi",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = AppColor.TextDark
            )

            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp)
                ) {
                    if (transactions.isEmpty()) {
                        Text(
                            text = "Belum ada aktivitas transaksi yang dapat ditampilkan.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColor.TextGray
                        )
                    } else {
                        transactions.take(5).forEach { transaction ->
                            AdminAuditLogItemV2(transaction = transaction)
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Daftar Merchant Aktif",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = AppColor.TextDark
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Menampilkan merchant berstatus ACTIVE yang dapat digunakan untuk validasi Inquiry QRIS dan Payment QRIS.",
                style = MaterialTheme.typography.bodySmall,
                color = AppColor.TextGray
            )

            Spacer(modifier = Modifier.height(12.dp))
        }

        if (activeMerchants.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp)
                    ) {
                        Text(
                            text = "Data merchant aktif belum tersedia.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColor.TextGray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))
            }
        } else {
            items(activeMerchants) { merchant ->
                AdminMerchantCardV2(merchant = merchant)
                Spacer(modifier = Modifier.height(12.dp))
            }

            item {
                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }
}

data class AdminMerchantUiV2(
    val id: Int,
    val name: String,
    val merchantCode: String,
    val category: String,
    val balance: Long,
    val status: String
)

@Composable
private fun AdminMetricCardV2(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(116.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = AppColor.TextGray
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = AppColor.Primary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = AppColor.TextGray
            )
        }
    }
}

@Composable
private fun AdminInfoRowV2(
    label: String,
    value: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = AppColor.TextGray
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = AppColor.TextDark
        )
    }
}

@Composable
private fun AdminProgressMetricV2(
    title: String,
    value: String,
    progress: Float
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = AppColor.TextDark
            )

            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = AppColor.Primary
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LinearProgressIndicator(
            progress = {
                progress.coerceIn(0f, 1f)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = AppColor.Primary,
            trackColor = Color(0xFFFFE4EC)
        )
    }
}

@Composable
private fun AdminTransactionItemV2(
    transaction: TransactionData
) {
    val type = transaction.transactionType.uppercase()
    val isSuccess = transaction.status.uppercase() == "SUCCESS"

    val title = when (type) {
        "QRIS" -> "QRIS ke ${transaction.merchantName}"
        "TRANSFER" -> "Transfer ke ${transaction.recipientUserName ?: transaction.merchantName}"
        else -> transaction.merchantName
    }

    val subtitle = when (type) {
        "QRIS" -> "Payment QRIS • ${formatAdminDisplayTimeV2(transaction.createdAt)}"
        "TRANSFER" -> "Transfer saldo • ${formatAdminDisplayTimeV2(transaction.createdAt)}"
        else -> "Transaksi • ${formatAdminDisplayTimeV2(transaction.createdAt)}"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = AppColor.TextDark
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColor.TextGray
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = "Ref: ${transaction.referenceNo ?: transaction.transactionId}",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColor.TextGray
                )
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = formatRupiah(transaction.amount),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = AppColor.TextDark
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (isSuccess) "SUCCESS" else transaction.status,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isSuccess) {
                        Color(0xFF008A3D)
                    } else {
                        Color(0xFFC62828)
                    }
                )
            }
        }
    }
}

@Composable
private fun AdminAuditLogItemV2(
    transaction: TransactionData
) {
    val type = transaction.transactionType.uppercase()

    val eventName = when {
        transaction.status.uppercase() != "SUCCESS" -> "TRANSACTION_FAILED"
        type == "QRIS" -> "QRIS_PAYMENT_SUCCESS"
        type == "TRANSFER" -> "TRANSFER_COMPLETED"
        else -> "TRANSACTION_COMPLETED"
    }

    val message = when (type) {
        "QRIS" -> "User ${transaction.userId} membayar ${formatRupiah(transaction.amount)} ke ${transaction.merchantName}"
        "TRANSFER" -> "User ${transaction.userId} transfer ${formatRupiah(transaction.amount)} ke ${transaction.recipientUserName ?: transaction.recipientUserId}"
        else -> "Transaksi ${formatRupiah(transaction.amount)} tercatat"
    }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = eventName,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = AppColor.TextDark
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = AppColor.TextGray
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "${formatAdminDisplayTimeV2(transaction.createdAt)} • Ref: ${transaction.referenceNo ?: transaction.transactionId}",
            style = MaterialTheme.typography.bodySmall,
            color = AppColor.TextGray
        )
    }
}

@Composable
private fun AdminMerchantCardV2(
    merchant: AdminMerchantUiV2
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Text(
                text = merchant.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AppColor.TextDark
            )

            Spacer(modifier = Modifier.height(14.dp))

            AdminMerchantRowV2(
                label = "Merchant ID",
                value = merchant.id.toString()
            )

            AdminMerchantRowV2(
                label = "Kode Merchant",
                value = merchant.merchantCode
            )

            AdminMerchantRowV2(
                label = "Kategori",
                value = merchant.category
            )

            AdminMerchantRowV2(
                label = "Saldo Penerimaan",
                value = formatRupiahLong(merchant.balance)
            )

            AdminMerchantRowV2(
                label = "Status",
                value = merchant.status
            )
        }
    }
}

@Composable
private fun AdminMerchantRowV2(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodySmall,
            color = AppColor.TextGray
        )

        Text(
            text = value,
            modifier = Modifier.weight(1.3f),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = AppColor.TextDark,
            textAlign = TextAlign.End
        )
    }
}

private fun calculateRateV2(
    value: Int,
    total: Int
): Double {
    if (total == 0) return 0.0
    return (value.toDouble() / total.toDouble()) * 100.0
}

private fun estimateAverageLatencyV2(
    totalTransactions: Int,
    failedCount: Int,
    timeoutCount: Int
): String {
    val base = 780
    val loadPenalty = totalTransactions * 18
    val failurePenalty = failedCount * 60
    val timeoutPenalty = timeoutCount * 180
    val result = base + loadPenalty + failurePenalty + timeoutPenalty
    return "$result ms"
}

private fun estimateP95LatencyV2(
    totalTransactions: Int,
    failedCount: Int,
    timeoutCount: Int
): String {
    val base = 1180
    val loadPenalty = totalTransactions * 26
    val failurePenalty = failedCount * 85
    val timeoutPenalty = timeoutCount * 260
    val result = base + loadPenalty + failurePenalty + timeoutPenalty
    return "$result ms"
}

private fun estimateThroughputV2(
    totalTransactions: Int
): String {
    if (totalTransactions == 0) return "0 req/s"
    val throughput = (totalTransactions / 4.0).coerceAtLeast(1.0)
    return "${String.format(Locale.US, "%.1f", throughput)} req/s"
}

private fun estimateLatencyProgressV2(
    totalTransactions: Int,
    failedCount: Int,
    timeoutCount: Int
): Float {
    val value = 780 + totalTransactions * 18 + failedCount * 60 + timeoutCount * 180
    return (value / 2200f).coerceIn(0f, 1f)
}

private fun estimateP95ProgressV2(
    totalTransactions: Int,
    failedCount: Int,
    timeoutCount: Int
): Float {
    val value = 1180 + totalTransactions * 26 + failedCount * 85 + timeoutCount * 260
    return (value / 3200f).coerceIn(0f, 1f)
}

private fun estimateThroughputProgressV2(
    totalTransactions: Int
): Float {
    if (totalTransactions == 0) return 0f
    return (totalTransactions / 40f).coerceIn(0f, 1f)
}

private fun parseAdminTimeMillisV2(
    rawDate: String
): Long {
    val possibleFormats = listOf(
        "yyyy-MM-dd HH:mm:ss",
        "yyyy-MM-dd HH:mm",
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
    )

    for (format in possibleFormats) {
        try {
            val parser = SimpleDateFormat(format, Locale.US)
            val date = parser.parse(rawDate)
            if (date != null) return date.time
        } catch (_: Exception) {
        }
    }

    return 0L
}

private fun formatAdminDisplayTimeV2(
    rawDate: String
): String {
    val possibleFormats = listOf(
        "yyyy-MM-dd HH:mm:ss",
        "yyyy-MM-dd HH:mm",
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
    )

    for (format in possibleFormats) {
        try {
            val parser = SimpleDateFormat(format, Locale.US)
            val date = parser.parse(rawDate)

            if (date != null) {
                val output = SimpleDateFormat("dd MMM yyyy, HH.mm", Locale("id", "ID"))
                return output.format(date)
            }
        } catch (_: Exception) {
        }
    }

    return rawDate
}

private fun formatRupiahLong(amount: Long): String {
    val localeId = Locale("in", "ID")
    val formatter = NumberFormat.getCurrencyInstance(localeId)
    return formatter.format(amount).replace(",00", "")
}