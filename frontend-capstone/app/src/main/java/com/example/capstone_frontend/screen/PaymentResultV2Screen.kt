package com.example.capstone_frontend.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.capstone_frontend.component.AppColor
import com.example.capstone_frontend.component.InfoRow
import com.example.capstone_frontend.component.formatRupiah
import com.example.capstone_frontend.data.DummyRepository
import com.example.capstone_frontend.model.TransactionData
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun PaymentResultV2Screen(
    transactionId: String,
    onBackHome: () -> Unit,
    onHistoryClick: () -> Unit
) {
    val transaction = DummyRepository.getTransactionById(transactionId)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColor.Background)
            .padding(20.dp),
        verticalArrangement = Arrangement.Center
    ) {
        if (transaction != null) {
            val isSuccess = transaction.status.uppercase() == "SUCCESS"
            val title = buildResultTitle(transaction)
            val description = buildResultDescription(transaction)
            val reference = transaction.referenceNo
                ?.takeIf { it.isNotBlank() && it.lowercase() != "null" }
                ?: transaction.transactionId

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isSuccess) "✓" else "!",
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(
                                if (isSuccess) {
                                    Color(0xFFE8F5E9)
                                } else {
                                    Color(0xFFFFEBEE)
                                }
                            )
                            .padding(horizontal = 22.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (isSuccess) {
                            Color(0xFF008A3D)
                        } else {
                            Color(0xFFC62828)
                        },
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = title,
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = AppColor.TextDark,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = description,
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColor.TextGray,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    InfoRow("No. Transaksi", transaction.transactionId)
                    InfoRow("Reference No", reference)
                    InfoRow("Keterangan", description)
                    InfoRow("Nominal", formatRupiah(transaction.amount))
                    InfoRow("Status", if (isSuccess) "Berhasil" else formatResultStatus(transaction.status))
                    InfoRow("Waktu", formatResultTime(transaction.createdAt))

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = onBackHome,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColor.Primary,
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = "Kembali ke Home",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    TextButton(
                        onClick = onHistoryClick
                    ) {
                        Text(
                            text = "Lihat Riwayat Transaksi",
                            color = AppColor.Primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Text(
                    text = "Transaksi tidak ditemukan.",
                    modifier = Modifier.padding(22.dp),
                    color = AppColor.TextDark
                )
            }
        }
    }
}

private fun buildResultTitle(
    transaction: TransactionData
): String {
    val type = transaction.transactionType.uppercase()
    val isSuccess = transaction.status.uppercase() == "SUCCESS"

    return when (type) {
        "QRIS" -> {
            if (isSuccess) {
                "Payment Successful"
            } else {
                "QRIS Payment Failed"
            }
        }

        "TRANSFER" -> {
            if (isSuccess) {
                "Transfer Berhasil"
            } else {
                "Transfer Failed"
            }
        }

        "REFUND" -> {
            "Refund Processed"
        }

        else -> {
            if (isSuccess) {
                "Transaksi Berhasil"
            } else {
                "Transaksi Gagal"
            }
        }
    }
}

private fun buildResultDescription(
    transaction: TransactionData
): String {
    val type = transaction.transactionType.uppercase()

    return when (type) {
        "QRIS" -> {
            "${formatRupiah(transaction.amount)} paid to ${safeResultMerchantName(transaction)}"
        }

        "TRANSFER" -> {
            "Transfer ${formatRupiah(transaction.amount)} ke ${safeResultRecipientName(transaction)}"
        }

        "REFUND" -> {
            "Refund ${formatRupiah(transaction.amount)} has been returned to your balance."
        }

        else -> {
            "Transaksi ${formatRupiah(transaction.amount)} diproses oleh baseline system."
        }
    }
}

private fun safeResultMerchantName(
    transaction: TransactionData
): String {
    return transaction.merchantName
        .takeIf { it.isNotBlank() && it.lowercase() != "null" }
        ?: "Merchant"
}

private fun safeResultRecipientName(
    transaction: TransactionData
): String {
    return transaction.recipientUserName
        ?.takeIf { it.isNotBlank() && it.lowercase() != "null" }
        ?: transaction.merchantName.takeIf { it.isNotBlank() && it.lowercase() != "null" }
        ?: "Penerima"
}

private fun formatResultStatus(
    status: String
): String {
    return when (status.uppercase()) {
        "SUCCESS" -> "Berhasil"
        "FAILED" -> "Gagal"
        "TIMEOUT" -> "Timeout"
        else -> status.ifBlank { "Unknown" }
    }
}

private fun formatResultTime(
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