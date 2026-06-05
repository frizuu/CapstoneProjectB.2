package com.example.capstone_frontend.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.capstone_frontend.component.AppColor
import com.example.capstone_frontend.component.formatRupiah
import com.example.capstone_frontend.data.DummyRepository
import com.example.capstone_frontend.data.RetrofitClient
import com.example.capstone_frontend.model.TransactionData
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun TransferDetailScreen(
    transactionId: String,
    onBack: () -> Unit
) {
    val currentUserId = DummyRepository.getCurrentUserId()

    var transaction by remember(transactionId) {
        mutableStateOf<TransactionData?>(DummyRepository.getTransactionById(transactionId))
    }

    var isLoading by remember(transactionId) {
        mutableStateOf(transaction == null)
    }

    var errorMessage by remember(transactionId) {
        mutableStateOf("")
    }

    LaunchedEffect(transactionId, currentUserId) {
        if (transaction == null) {
            try {
                isLoading = true
                errorMessage = ""

                try {
                    val merchantResponse = RetrofitClient.api.getMerchants()
                    DummyRepository.setMerchantCache(merchantResponse.merchants)
                } catch (_: Exception) {
                }

                val response = RetrofitClient.api.getTransactions(currentUserId)
                val convertedTransactions = DummyRepository.convertBackendTransactions(response)

                DummyRepository.setBackendTransactions(convertedTransactions)

                transaction = convertedTransactions.firstOrNull {
                    it.transactionId == transactionId
                } ?: convertedTransactions.firstOrNull {
                    normalizeTransactionIdForCompare(it.transactionId) ==
                            normalizeTransactionIdForCompare(transactionId)
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: "Gagal memuat detail transaksi."
            } finally {
                isLoading = false
            }
        } else {
            isLoading = false
        }
    }

    if (isLoading) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColor.Background)
                .padding(22.dp)
        ) {
            Spacer(modifier = Modifier.height(34.dp))

            TextButton(
                onClick = onBack
            ) {
                Text(
                    text = "← Kembali",
                    color = AppColor.Primary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Text(
                    text = "Memuat detail transaksi...",
                    modifier = Modifier.padding(20.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = AppColor.TextGray
                )
            }
        }

        return
    }

    val currentTransaction = transaction

    if (currentTransaction == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColor.Background)
                .padding(22.dp)
        ) {
            Spacer(modifier = Modifier.height(34.dp))

            TextButton(
                onClick = onBack
            ) {
                Text(
                    text = "← Kembali",
                    color = AppColor.Primary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Transaksi tidak ditemukan.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = AppColor.TextDark
                    )

                    if (errorMessage.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFC62828)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Silakan kembali ke riwayat atau notifikasi, lalu buka ulang transaksi.",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColor.TextGray
                    )
                }
            }
        }

        return
    }

    val type = normalizeDetailTransactionType(currentTransaction.transactionType)

    val isIncomingTransfer = type == "TRANSFER" &&
            currentTransaction.recipientUserId == currentUserId &&
            currentTransaction.userId.toIntOrNull() != currentUserId

    val senderName = when (type) {
        "TRANSFER" -> currentTransaction.senderName
            ?.takeIf { it.isNotBlank() && it.lowercase() != "null" }
            ?: "Pengirim"

        "QRIS" -> DummyRepository.getCurrentUserName()

        else -> "User ID ${currentTransaction.userId}"
    }

    val receiverName = when (type) {
        "TRANSFER" -> currentTransaction.recipientUserName
            ?.takeIf { it.isNotBlank() && it.lowercase() != "null" }
            ?: "Penerima"

        "QRIS" -> currentTransaction.merchantName
            .takeIf { it.isNotBlank() && it.lowercase() != "null" }
            ?: "Merchant"

        else -> "Sistem Baseline"
    }

    val amountTitle = if (isIncomingTransfer) {
        "+${formatRupiah(currentTransaction.amount)}"
    } else {
        formatRupiah(currentTransaction.amount)
    }

    val description = when (type) {
        "QRIS" -> "Bayar QRIS ke $receiverName"
        "TRANSFER" -> if (isIncomingTransfer) {
            "Terima uang dari $senderName"
        } else {
            "Transfer ke $receiverName"
        }

        else -> "Transaksi baseline dari data awal sistem"
    }

    val methodText = when (type) {
        "QRIS" -> "QRIS"
        "TRANSFER" -> "Transfer Saldo"
        else -> "Baseline Transaction"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColor.Background)
            .padding(horizontal = 22.dp)
    ) {
        Spacer(modifier = Modifier.height(34.dp))

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
                text = "Rincian Transaksi",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = AppColor.TextDark
            )
        }

        Spacer(modifier = Modifier.height(42.dp))

        Text(
            text = amountTitle,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = if (isIncomingTransfer) {
                Color(0xFF008A3D)
            } else {
                AppColor.TextDark
            },
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = description,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.bodyMedium,
            color = AppColor.TextGray,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(36.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier.padding(22.dp)
            ) {
                DetailPartyRowV2(
                    label = "Dari",
                    initial = senderName.take(1).uppercase(),
                    name = senderName,
                    subtitle = if (type == "BASELINE") {
                        "Data awal baseline"
                    } else {
                        "Saldo Utama"
                    }
                )

                DetailDividerV2()

                DetailPartyRowV2(
                    label = "Ke",
                    initial = receiverName.take(1).uppercase(),
                    name = receiverName,
                    subtitle = when (type) {
                        "QRIS" -> "Merchant QRIS"
                        "TRANSFER" -> "Saldo Utama"
                        else -> "Sistem Baseline"
                    }
                )

                DetailDividerV2()

                DetailInfoRowV2(
                    label = when (type) {
                        "QRIS" -> "Jumlah Pembayaran"
                        "TRANSFER" -> "Jumlah Transfer"
                        else -> "Jumlah Transaksi"
                    },
                    value = formatRupiah(currentTransaction.amount)
                )

                DetailDividerV2()

                DetailInfoRowV2(
                    label = "No. Transaksi",
                    value = currentTransaction.transactionId
                )

                DetailDividerV2()

                DetailInfoRowV2(
                    label = "Metode Transaksi",
                    value = methodText
                )

                DetailDividerV2()

                DetailInfoRowV2(
                    label = "Keterangan",
                    value = description
                )

                DetailDividerV2()

                DetailInfoRowV2(
                    label = "Status",
                    value = formatDetailStatus(currentTransaction.status)
                )

                DetailDividerV2()

                DetailInfoRowV2(
                    label = "Waktu Transaksi",
                    value = formatDetailTimeV2(currentTransaction.createdAt)
                )

                if (!currentTransaction.referenceNo.isNullOrBlank()) {
                    DetailDividerV2()

                    DetailInfoRowV2(
                        label = "Reference No",
                        value = currentTransaction.referenceNo
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "Butuh Bantuan?",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF157EAA),
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(28.dp))
    }
}

@Composable
fun DetailPartyRowV2(
    label: String,
    initial: String,
    name: String,
    subtitle: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(0.9f),
            style = MaterialTheme.typography.bodyLarge,
            color = AppColor.TextGray
        )

        Row(
            modifier = Modifier.weight(2.2f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(
                        color = AppColor.Primary,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initial.ifBlank { "?" },
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Column(
                modifier = Modifier.padding(start = 12.dp)
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = AppColor.TextDark
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColor.TextGray
                )
            }
        }
    }
}

@Composable
fun DetailInfoRowV2(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
            color = AppColor.TextGray
        )

        Text(
            text = value,
            modifier = Modifier.weight(1.2f),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = AppColor.TextDark
        )
    }
}

@Composable
fun DetailDividerV2() {
    Spacer(modifier = Modifier.height(18.dp))

    Divider(
        color = Color(0xFFECE8EC),
        thickness = 1.dp
    )

    Spacer(modifier = Modifier.height(18.dp))
}

private fun normalizeDetailTransactionType(
    transactionType: String
): String {
    val rawType = transactionType.trim().uppercase()

    return when {
        rawType == "QRIS" -> "QRIS"
        rawType == "TRANSFER" -> "TRANSFER"
        rawType == "REFUND" -> "REFUND"
        else -> "BASELINE"
    }
}

private fun normalizeTransactionIdForCompare(
    transactionId: String
): String {
    return transactionId
        .uppercase()
        .replace("TRX-", "")
        .trimStart('0')
        .ifBlank { transactionId.uppercase() }
}

private fun formatDetailStatus(
    status: String
): String {
    return when (status.uppercase()) {
        "SUCCESS" -> "Berhasil"
        "FAILED" -> "Gagal"
        "TIMEOUT" -> "Timeout"
        else -> status.ifBlank { "Unknown" }
    }
}

private fun formatDetailTimeV2(
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
                val output = SimpleDateFormat("dd MMMM yyyy, HH.mm", Locale("id", "ID"))
                return output.format(date)
            }
        } catch (_: Exception) {
        }
    }

    return rawDate
}