package com.example.capstone_frontend.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.capstone_frontend.component.AppColor
import com.example.capstone_frontend.component.formatRupiah
import com.example.capstone_frontend.data.DummyRepository
import com.example.capstone_frontend.data.RetrofitClient
import com.example.capstone_frontend.model.TransactionData
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun TransferHistoryScreen(
    onBack: () -> Unit,
    onTransactionClick: (String) -> Unit
) {
    var transactions by remember {
        mutableStateOf<List<TransactionData>>(emptyList())
    }

    var isLoading by remember {
        mutableStateOf(true)
    }

    var errorMessage by remember {
        mutableStateOf("")
    }

    var showTransaction by remember {
        mutableStateOf(true)
    }

    var totalOut by remember {
        mutableIntStateOf(0)
    }

    var totalIn by remember {
        mutableIntStateOf(0)
    }

    val currentUserId = DummyRepository.getCurrentUserId()

    LaunchedEffect(currentUserId) {
        try {
            isLoading = true
            errorMessage = ""

            try {
                val merchantResponse = RetrofitClient.api.getMerchants()
                DummyRepository.setMerchantCache(merchantResponse.merchants)
            } catch (_: Exception) {
            }

            val response = RetrofitClient.api.getTransactions(currentUserId)
            val converted = DummyRepository.convertBackendTransactions(response)

            DummyRepository.setBackendTransactions(converted)

            transactions = converted

            totalOut = converted
                .filter { isOutgoingForCurrentUser(it, currentUserId) }
                .sumOf { it.amount }

            totalIn = converted
                .filter { isIncomingTransfer(it, currentUserId) }
                .sumOf { it.amount }
        } catch (e: Exception) {
            transactions = emptyList()
            totalOut = 0
            totalIn = 0
            errorMessage = e.message ?: "Riwayat transaksi belum dapat dimuat."
        } finally {
            isLoading = false
        }
    }

    val filteredTransactions = transactions.filter {
        showTransaction
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColor.Background)
            .padding(horizontal = 18.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onBack
                ) {
                    Text(
                        text = "←",
                        color = AppColor.Primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "Riwayat Transaksi",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = AppColor.TextDark
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HistoryFilterChipV2(
                    text = "30 Hari Terakhir⌄",
                    modifier = Modifier.weight(1f)
                )

                HistoryFilterChipV2(
                    text = "Semua Transaksi⌄",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = showTransaction,
                    onCheckedChange = {
                        showTransaction = it
                    },
                    colors = CheckboxDefaults.colors(
                        checkedColor = AppColor.Primary
                    )
                )

                Text(
                    text = "Tampilkan transaksi QRIS dan transfer",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColor.TextDark
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "Mei 2026",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = AppColor.TextDark
                )

                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "Saldo keluar: ${formatRupiah(totalOut)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColor.TextGray
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = "Saldo masuk: ${formatRupiah(totalIn)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColor.TextGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
        }

        when {
            isLoading -> {
                item {
                    Text(
                        text = "Memuat riwayat transaksi...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColor.TextGray
                    )
                }
            }

            errorMessage.isNotBlank() -> {
                item {
                    Text(
                        text = "Riwayat transaksi belum dapat dimuat.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColor.TextGray
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFC62828)
                    )
                }
            }

            filteredTransactions.isEmpty() -> {
                item {
                    Text(
                        text = "Belum ada transaksi.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColor.TextGray
                    )
                }
            }

            else -> {
                items(filteredTransactions) { transaction ->
                    HistoryItemV2(
                        transaction = transaction,
                        currentUserId = currentUserId,
                        onClick = {
                            onTransactionClick(transaction.transactionId)
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun HistoryFilterChipV2(
    text: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(46.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = AppColor.TextDark,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun HistoryItemV2(
    transaction: TransactionData,
    currentUserId: Int,
    onClick: () -> Unit
) {
    val type = transaction.transactionType.uppercase()
    val isIncoming = isIncomingTransfer(transaction, currentUserId)

    val title = when {
        type == "QRIS" -> {
            "Bayar QRIS ke ${transaction.merchantName}"
        }

        type == "TRANSFER" && isIncoming -> {
            "Terima uang dari ${transaction.senderName ?: "Pengirim"}"
        }

        type == "TRANSFER" -> {
            "Transfer ke ${transaction.recipientUserName ?: transaction.merchantName}"
        }

        else -> {
            transaction.merchantName
        }
    }

    val subtitle = when {
        type == "QRIS" -> "Pembayaran QRIS"
        type == "TRANSFER" && isIncoming -> "Transfer masuk"
        type == "TRANSFER" -> "Transfer keluar"
        else -> "Pembayaran"
    }

    val amountText = if (isIncoming) {
        "+${formatRupiah(transaction.amount)}"
    } else {
        "-${formatRupiah(transaction.amount)}"
    }

    val iconText = when (type) {
        "QRIS" -> "QR"
        "TRANSFER" -> "TF"
        else -> "TX"
    }

    val iconBackground = if (isIncoming) {
        Color(0xFFE8F5E9)
    } else {
        Color(0xFFFFF3E0)
    }

    val amountColor = if (isIncoming) {
        Color(0xFF008A3D)
    } else {
        AppColor.TextDark
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    color = iconBackground,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = iconText,
                color = AppColor.Primary,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 14.dp)
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
                color = AppColor.TextDark
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = formatHistoryTimeV2(transaction.createdAt),
                style = MaterialTheme.typography.bodySmall,
                color = AppColor.TextGray
            )
        }

        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = amountText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = amountColor
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (transaction.status == "SUCCESS") {
                    "Berhasil"
                } else {
                    transaction.status
                },
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = if (transaction.status == "SUCCESS") {
                    Color(0xFF008A3D)
                } else {
                    Color(0xFFC62828)
                }
            )
        }
    }
}

private fun isIncomingTransfer(
    transaction: TransactionData,
    currentUserId: Int
): Boolean {
    return transaction.transactionType.uppercase() == "TRANSFER" &&
            transaction.recipientUserId == currentUserId &&
            transaction.userId.toIntOrNull() != currentUserId
}

private fun isOutgoingForCurrentUser(
    transaction: TransactionData,
    currentUserId: Int
): Boolean {
    return when (transaction.transactionType.uppercase()) {
        "TRANSFER" -> transaction.userId.toIntOrNull() == currentUserId
        "QRIS" -> transaction.userId.toIntOrNull() == currentUserId
        else -> true
    }
}

private fun formatHistoryTimeV2(
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