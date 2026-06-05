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
import com.example.capstone_frontend.data.NotificationReadStore
import com.example.capstone_frontend.data.RetrofitClient
import com.example.capstone_frontend.model.NotificationData
import com.example.capstone_frontend.model.NotificationType
import com.example.capstone_frontend.model.TransactionData
import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.coroutines.delay

@Composable
fun NotificationScreen(
    onBack: () -> Unit,
    onTransactionClick: (String) -> Unit
) {
    var notifications by remember {
        mutableStateOf<List<NotificationData>>(emptyList())
    }

    var isLoading by remember {
        mutableStateOf(true)
    }

    var errorMessage by remember {
        mutableStateOf("")
    }

    var totalIn by remember {
        mutableIntStateOf(0)
    }

    var totalOut by remember {
        mutableIntStateOf(0)
    }

    val currentUserId = DummyRepository.getCurrentUserId()

    suspend fun refreshNotifications() {
        try {
            errorMessage = ""

            try {
                val merchantResponse = RetrofitClient.api.getMerchants()
                DummyRepository.setMerchantCache(merchantResponse.merchants)
            } catch (_: Exception) {
            }

            val response = RetrofitClient.api.getTransactions(currentUserId)
            val convertedTransactions = DummyRepository.convertBackendTransactions(response)

            DummyRepository.setBackendTransactions(convertedTransactions)

            val generatedNotifications = convertedTransactions
                .filter { transaction ->
                    NotificationReadStore.shouldCountAsNotification(
                        transaction = transaction,
                        currentUserId = currentUserId
                    )
                }
                .map { transaction ->
                    transaction.toNotificationData(currentUserId)
                }

            notifications = generatedNotifications

            totalIn = generatedNotifications
                .filter {
                    it.type == NotificationType.INCOMING_TRANSFER ||
                            it.type == NotificationType.REFUND
                }
                .sumOf { it.amount }

            totalOut = generatedNotifications
                .filter {
                    it.type == NotificationType.OUTGOING_TRANSFER ||
                            it.type == NotificationType.QRIS_SUCCESS ||
                            it.type == NotificationType.QRIS_FAILED ||
                            it.type == NotificationType.TRANSFER_FAILED ||
                            it.type == NotificationType.INFO
                }
                .sumOf { it.amount }
        } catch (e: Exception) {
            errorMessage = e.message ?: "Notifikasi belum dapat dimuat."
        } finally {
            isLoading = false
        }
    }

    LaunchedEffect(currentUserId) {
        isLoading = true
        refreshNotifications()

        while (true) {
            delay(5000)
            refreshNotifications()
        }
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

                Column {
                    Text(
                        text = "Notifikasi",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = AppColor.TextDark
                    )

                    Text(
                        text = "Aktivitas uang masuk, keluar, QRIS, refund, dan transaksi gagal.",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColor.TextGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                NotificationSummaryCard(
                    title = "Uang Masuk",
                    value = formatRupiah(totalIn),
                    modifier = Modifier.weight(1f),
                    isIncome = true
                )

                NotificationSummaryCard(
                    title = "Uang Keluar",
                    value = formatRupiah(totalOut),
                    modifier = Modifier.weight(1f),
                    isIncome = false
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Recent Activity",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AppColor.TextDark
            )

            Spacer(modifier = Modifier.height(12.dp))
        }

        when {
            isLoading -> {
                item {
                    Text(
                        text = "Memuat notifikasi...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColor.TextGray
                    )
                }
            }

            errorMessage.isNotBlank() && notifications.isEmpty() -> {
                item {
                    Text(
                        text = "Notifikasi belum dapat dimuat.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColor.TextGray
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFC62828)
                    )
                }
            }

            notifications.isEmpty() -> {
                item {
                    Text(
                        text = "Belum ada notifikasi transaksi.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColor.TextGray
                    )
                }
            }

            else -> {
                items(notifications) { notification ->
                    val isRead = NotificationReadStore.isNotificationRead(
                        userId = currentUserId,
                        transactionId = notification.transactionId
                    )

                    NotificationItem(
                        notification = notification,
                        isRead = isRead,
                        onClick = {
                            NotificationReadStore.markNotificationRead(
                                userId = currentUserId,
                                transactionId = notification.transactionId
                            )

                            onTransactionClick(notification.transactionId)
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
fun NotificationSummaryCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    isIncome: Boolean
) {
    Card(
        modifier = modifier.height(92.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
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
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isIncome) {
                    Color(0xFF008A3D)
                } else {
                    AppColor.Primary
                }
            )
        }
    }
}

@Composable
fun NotificationItem(
    notification: NotificationData,
    isRead: Boolean,
    onClick: () -> Unit
) {
    val colorConfig = notification.type.toNotificationColorConfig()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isRead) {
                Color.White
            } else {
                Color(0xFFFFF4F7)
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = colorConfig.background,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = colorConfig.icon,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colorConfig.foreground,
                    textAlign = TextAlign.Center
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!isRead) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    color = Color(0xFFFF6A00),
                                    shape = CircleShape
                                )
                        )

                        Spacer(modifier = Modifier.padding(start = 6.dp))
                    }

                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = AppColor.TextDark
                    )

                    if (notification.status.uppercase() != "SUCCESS") {
                        Spacer(modifier = Modifier.padding(start = 6.dp))

                        Text(
                            text = formatNotificationStatus(notification.status),
                            modifier = Modifier
                                .background(
                                    color = Color(0xFFFFEBEE),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFC62828)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColor.TextGray
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = formatNotificationTime(notification.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColor.TextGray
                )
            }

            Text(
                text = notification.amountText,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = colorConfig.amountColor
            )
        }
    }
}

private data class NotificationColorConfig(
    val icon: String,
    val background: Color,
    val foreground: Color,
    val amountColor: Color
)

private fun NotificationType.toNotificationColorConfig(): NotificationColorConfig {
    return when (this) {
        NotificationType.INCOMING_TRANSFER -> {
            NotificationColorConfig(
                icon = "↙",
                background = Color(0xFFE8F5E9),
                foreground = Color(0xFF008A3D),
                amountColor = Color(0xFF008A3D)
            )
        }

        NotificationType.OUTGOING_TRANSFER -> {
            NotificationColorConfig(
                icon = "↗",
                background = Color(0xFFFFF3E0),
                foreground = AppColor.Primary,
                amountColor = AppColor.TextDark
            )
        }

        NotificationType.QRIS_SUCCESS -> {
            NotificationColorConfig(
                icon = "QR",
                background = Color(0xFFE3F2FD),
                foreground = Color(0xFF1565C0),
                amountColor = AppColor.TextDark
            )
        }

        NotificationType.QRIS_FAILED -> {
            NotificationColorConfig(
                icon = "!",
                background = Color(0xFFFFEBEE),
                foreground = Color(0xFFC62828),
                amountColor = Color(0xFFC62828)
            )
        }

        NotificationType.TRANSFER_FAILED -> {
            NotificationColorConfig(
                icon = "×",
                background = Color(0xFFFFEBEE),
                foreground = Color(0xFFC62828),
                amountColor = Color(0xFFC62828)
            )
        }

        NotificationType.REFUND -> {
            NotificationColorConfig(
                icon = "↺",
                background = Color(0xFFE3F2FD),
                foreground = Color(0xFF1565C0),
                amountColor = Color(0xFF008A3D)
            )
        }

        NotificationType.INFO -> {
            NotificationColorConfig(
                icon = "BL",
                background = Color(0xFFE3F2FD),
                foreground = Color(0xFF1565C0),
                amountColor = AppColor.TextDark
            )
        }
    }
}

private fun TransactionData.toNotificationData(
    currentUserId: Int
): NotificationData {
    val type = normalizeNotificationTransactionType(transactionType)
    val statusUpper = status.uppercase()

    val isIncomingTransfer = type == "TRANSFER" &&
            recipientUserId == currentUserId &&
            userId.toIntOrNull() != currentUserId

    val isFailed = statusUpper != "SUCCESS"

    val notificationType = when {
        isFailed && type == "TRANSFER" -> NotificationType.TRANSFER_FAILED
        isFailed && type == "QRIS" -> NotificationType.QRIS_FAILED
        type == "QRIS" -> NotificationType.QRIS_SUCCESS
        type == "TRANSFER" && isIncomingTransfer -> NotificationType.INCOMING_TRANSFER
        type == "TRANSFER" -> NotificationType.OUTGOING_TRANSFER
        type == "REFUND" -> NotificationType.REFUND
        else -> NotificationType.INFO
    }

    val title = when (notificationType) {
        NotificationType.INCOMING_TRANSFER -> "Transfer Masuk"
        NotificationType.OUTGOING_TRANSFER -> "Realtime Transfer"
        NotificationType.QRIS_SUCCESS -> "QRIS Payment Successful"
        NotificationType.QRIS_FAILED -> "QRIS Payment Failed"
        NotificationType.TRANSFER_FAILED -> "Transfer Failed"
        NotificationType.REFUND -> "Refund Processed"
        NotificationType.INFO -> "Transaksi Baseline"
    }

    val message = when (notificationType) {
        NotificationType.INCOMING_TRANSFER -> {
            "Kamu menerima ${formatRupiah(amount)} dari ${safeNotificationSenderName(this)}."
        }

        NotificationType.OUTGOING_TRANSFER -> {
            "Kamu baru melakukan transfer senilai ${formatRupiah(amount)} kepada ${safeNotificationRecipientName(this)}."
        }

        NotificationType.QRIS_SUCCESS -> {
            "Payment of ${formatRupiah(amount)} settled at ${safeNotificationMerchantName(this)}. Ref: ${referenceNo ?: transactionId}."
        }

        NotificationType.QRIS_FAILED -> {
            "Pembayaran QRIS ke ${safeNotificationMerchantName(this)} gagal diproses."
        }

        NotificationType.TRANSFER_FAILED -> {
            "Transfer to ${safeNotificationRecipientName(this)} could not be completed. No funds were deducted."
        }

        NotificationType.REFUND -> {
            "Refund Processed: ${formatRupiah(amount)} has been returned to your balance."
        }

        NotificationType.INFO -> {
            "Aktivitas awal dari data baseline sebesar ${formatRupiah(amount)} tercatat dengan status ${formatNotificationStatus(status)}."
        }
    }

    val amountText = when (notificationType) {
        NotificationType.INCOMING_TRANSFER,
        NotificationType.REFUND -> {
            "+${formatRupiah(amount)}"
        }

        NotificationType.TRANSFER_FAILED,
        NotificationType.QRIS_FAILED -> {
            formatRupiah(amount)
        }

        else -> {
            "-${formatRupiah(amount)}"
        }
    }

    return NotificationData(
        id = transactionId,
        title = title,
        message = message,
        amount = amount,
        amountText = amountText,
        type = notificationType,
        status = status,
        createdAt = createdAt,
        transactionId = transactionId
    )
}

private fun normalizeNotificationTransactionType(
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

private fun safeNotificationMerchantName(
    transaction: TransactionData
): String {
    return transaction.merchantName
        .takeIf { it.isNotBlank() && it.lowercase() != "null" }
        ?: "Merchant"
}

private fun safeNotificationRecipientName(
    transaction: TransactionData
): String {
    return transaction.recipientUserName
        ?.takeIf { it.isNotBlank() && it.lowercase() != "null" }
        ?: transaction.merchantName.takeIf { it.isNotBlank() && it.lowercase() != "null" }
        ?: "Penerima"
}

private fun safeNotificationSenderName(
    transaction: TransactionData
): String {
    return transaction.senderName
        ?.takeIf { it.isNotBlank() && it.lowercase() != "null" }
        ?: "pengirim"
}

private fun formatNotificationStatus(
    status: String
): String {
    return when (status.uppercase()) {
        "SUCCESS" -> "Berhasil"
        "FAILED" -> "Gagal"
        "TIMEOUT" -> "Timeout"
        else -> status.ifBlank { "Unknown" }
    }
}

private fun formatNotificationTime(
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