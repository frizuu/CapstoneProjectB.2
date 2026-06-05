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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.capstone_frontend.component.AppColor
import com.example.capstone_frontend.component.MetricCard
import com.example.capstone_frontend.component.formatRupiah
import com.example.capstone_frontend.data.DummyRepository
import com.example.capstone_frontend.data.MerchantDto
import com.example.capstone_frontend.data.RetrofitClient
import com.example.capstone_frontend.model.TransactionData

@Composable
fun AdminDashboardScreen(
    onLogout: () -> Unit
) {
    var merchantBalance by remember {
        mutableLongStateOf(0L)
    }

    var merchants by remember {
        mutableStateOf<List<MerchantDto>>(emptyList())
    }

    var transactions by remember {
        mutableStateOf<List<TransactionData>>(emptyList())
    }

    var isLoading by remember {
        mutableStateOf(true)
    }

    var isError by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {
        try {
            isLoading = true
            isError = false

            val balanceResponse = RetrofitClient.api.getMerchantBalance(1)
            merchantBalance = balanceResponse.balance

            val merchantResponse = RetrofitClient.api.getMerchants()
            merchants = merchantResponse.merchants
            DummyRepository.setMerchantCache(merchantResponse.merchants)

            val transactionResponse = RetrofitClient.api.getTransactions(1)
            val convertedTransactions = DummyRepository.convertBackendTransactions(transactionResponse)
            DummyRepository.setBackendTransactions(convertedTransactions)
            transactions = DummyRepository.getTransactions()
        } catch (e: Exception) {
            isError = true
            transactions = DummyRepository.getTransactions()
        } finally {
            isLoading = false
        }
    }

    val totalTransaction = transactions.size

    val successCount = transactions.count {
        it.status == "SUCCESS"
    }

    val failedCount = transactions.count {
        it.status == "FAILED"
    }

    val timeoutCount = transactions.count {
        it.status == "TIMEOUT"
    }

    val successRate = if (totalTransaction > 0) {
        (successCount * 100) / totalTransaction
    } else {
        0
    }

    val errorRate = if (totalTransaction > 0) {
        (failedCount * 100) / totalTransaction
    } else {
        0
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColor.Background)
            .padding(horizontal = 20.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(34.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(30.dp),
                colors = CardDefaults.cardColors(containerColor = AppColor.Primary),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier.padding(22.dp)
                ) {
                    Text(
                        text = "Admin / Merchant Dashboard",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Monitoring saldo penerimaan merchant, daftar merchant, transaksi, dan audit log dari baseline backend.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFFFD6D6)
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
                text = "Ringkasan Baseline",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = AppColor.TextDark
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Data diambil dari endpoint baseline: merchant balance, merchants, dan transactions.",
                style = MaterialTheme.typography.bodyMedium,
                color = AppColor.TextGray
            )

            if (isLoading) {
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Memuat data dashboard...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColor.TextGray
                )
            }

            if (isError) {
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Sebagian data belum dapat dimuat. Pastikan backend baseline sedang berjalan.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFC62828)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    title = "Saldo Penerimaan Merchant",
                    value = formatRupiah(merchantBalance.toInt()),
                    modifier = Modifier.weight(1f)
                )

                MetricCard(
                    title = "Total Transaksi",
                    value = totalTransaction.toString(),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    title = "Success Rate",
                    value = "$successRate%",
                    modifier = Modifier.weight(1f)
                )

                MetricCard(
                    title = "Error Rate",
                    value = "$errorRate%",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    title = "Berhasil",
                    value = successCount.toString(),
                    modifier = Modifier.weight(1f)
                )

                MetricCard(
                    title = "Timeout",
                    value = timeoutCount.toString(),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            BaselineContextCard()

            Spacer(modifier = Modifier.height(16.dp))

            AuditLogSummaryCard(
                transactions = transactions
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Daftar Merchant Aktif",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = AppColor.TextDark
            )

            Spacer(modifier = Modifier.height(12.dp))
        }

        if (merchants.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp)
                    ) {
                        Text(
                            text = "Data merchant belum tersedia.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColor.TextGray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        } else {
            items(merchants) { merchant ->
                MerchantCard(
                    merchant = merchant
                )

                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun BaselineContextCard() {
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
                text = "Konteks Pengujian Baseline",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AppColor.TextDark
            )

            Spacer(modifier = Modifier.height(14.dp))

            BaselineContextItem(
                label = "Mode Sistem",
                value = "Baseline"
            )

            BaselineContextItem(
                label = "Alur Transaksi",
                value = "Inquiry QRIS → Payment QRIS → Status Check"
            )

            BaselineContextItem(
                label = "Data Monitoring",
                value = "Saldo penerimaan merchant, daftar merchant, transaksi, dan audit log"
            )

            BaselineContextItem(
                label = "Metrik Evaluasi",
                value = "Success rate, error rate, timeout, dan jumlah transaksi"
            )
        }
    }
}

@Composable
fun BaselineContextItem(
    label: String,
    value: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 7.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = AppColor.TextGray
        )

        Spacer(modifier = Modifier.height(3.dp))

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = AppColor.TextDark
        )
    }
}

@Composable
fun AuditLogSummaryCard(
    transactions: List<TransactionData>
) {
    val latestLogs = transactions.take(5)

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
                text = "Audit Log Transaksi",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AppColor.TextDark
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Mencatat aktivitas transaksi berhasil, gagal, dan timeout untuk kebutuhan monitoring serta troubleshooting.",
                style = MaterialTheme.typography.bodySmall,
                color = AppColor.TextGray
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (latestLogs.isEmpty()) {
                Text(
                    text = "Belum ada aktivitas transaksi yang dapat ditampilkan.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColor.TextGray
                )
            } else {
                latestLogs.forEachIndexed { index, transaction ->
                    AuditLogItem(
                        transaction = transaction
                    )

                    if (index != latestLogs.lastIndex) {
                        Divider(
                            modifier = Modifier.padding(vertical = 10.dp),
                            color = Color(0xFFEFEFF2)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AuditLogItem(
    transaction: TransactionData
) {
    val statusColor = when (transaction.status) {
        "SUCCESS" -> Color(0xFF168A4A)
        "FAILED" -> Color(0xFFC62828)
        "TIMEOUT" -> Color(0xFFE69500)
        else -> AppColor.TextGray
    }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = transaction.transactionId,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = AppColor.TextDark
            )

            Text(
                text = auditStatusLabel(transaction.status),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = statusColor
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "${auditTypeLabel(transaction.transactionType)} • ${transaction.merchantName}",
            style = MaterialTheme.typography.bodySmall,
            color = AppColor.TextGray
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Nominal ${formatRupiah(transaction.amount)} • ${transaction.createdAt}",
            style = MaterialTheme.typography.bodySmall,
            color = AppColor.TextGray
        )
    }
}

@Composable
fun MerchantCard(
    merchant: MerchantDto
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

            MerchantInfoRow(
                label = "Merchant ID",
                value = merchant.id.toString()
            )

            MerchantInfoRow(
                label = "Kode Merchant",
                value = merchant.merchantCode
            )

            MerchantInfoRow(
                label = "Kategori",
                value = merchant.category
            )

            MerchantInfoRow(
                label = "Saldo Penerimaan",
                value = formatRupiah(merchant.balance.toInt())
            )

            MerchantInfoRow(
                label = "Status",
                value = merchant.status
            )
        }
    }
}

@Composable
fun MerchantInfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = AppColor.TextGray
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = AppColor.TextDark
        )
    }
}

fun auditStatusLabel(status: String): String {
    return when (status) {
        "SUCCESS" -> "Berhasil"
        "FAILED" -> "Gagal"
        "TIMEOUT" -> "Timeout"
        else -> "Tidak diketahui"
    }
}

fun auditTypeLabel(transactionType: String): String {
    return when (transactionType.uppercase()) {
        "QRIS" -> "QRIS Payment"
        "PAYMENT" -> "Normal Payment"
        else -> transactionType
    }
}