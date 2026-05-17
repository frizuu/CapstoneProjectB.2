package com.example.capstone_frontend.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.capstone_frontend.model.TransactionData

object AppColor {
    val Primary = Color(0xFF8A1538)
    val PrimaryDark = Color(0xFF5F0B24)
    val Background = Color(0xFFF8F3F4)
    val TextDark = Color(0xFF222222)
    val TextGray = Color(0xFF777777)
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(108.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
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
        }
    }
}

@Composable
fun FeatureMenuItem(
    title: String,
    iconText: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .background(Color(0xFFFFF1F3), RoundedCornerShape(22.dp))
                .padding(horizontal = 18.dp, vertical = 14.dp)
        ) {
            Text(
                text = iconText,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = AppColor.Primary
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = AppColor.TextDark
        )
    }
}

@Composable
fun TransactionCard(
    transaction: TransactionData,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = transaction.transactionId,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = AppColor.TextDark
                )

                StatusChip(status = transaction.status)
            }

            Spacer(modifier = Modifier.height(12.dp))

            InfoRow("Penerima", transaction.merchantName)
            InfoRow("Nominal", formatRupiah(transaction.amount))
            InfoRow("Status", transaction.status)
            InfoRow("Jenis", transaction.transactionType)
        }
    }
}

@Composable
fun StatusChip(status: String) {
    val chipColor = when (status) {
        "SUCCESS" -> Color(0xFFE6F6EC)
        "FAILED" -> Color(0xFFFFE5E5)
        "TIMEOUT" -> Color(0xFFFFF3D6)
        else -> Color(0xFFEFEFEF)
    }

    val textColor = when (status) {
        "SUCCESS" -> Color(0xFF1B8F4A)
        "FAILED" -> Color(0xFFD32F2F)
        "TIMEOUT" -> Color(0xFFE59A00)
        else -> Color.Gray
    }

    Box(
        modifier = Modifier
            .background(chipColor, RoundedCornerShape(50.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = status,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

@Composable
fun InfoRow(
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

fun formatRupiah(amount: Int): String {
    return "Rp%,d".format(amount).replace(",", ".")
}