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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
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

    var selectedDateFilter by remember {
        mutableStateOf("30 Hari Terakhir")
    }

    var selectedTypeFilter by remember {
        mutableStateOf("Semua Transaksi")
    }

    var showDateSheet by remember {
        mutableStateOf(false)
    }

    var showTypeSheet by remember {
        mutableStateOf(false)
    }

    val dateSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    val typeSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    val currentUserId = DummyRepository.getCurrentUserId()

    suspend fun refreshHistory() {
        try {
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
        } catch (e: Exception) {
            errorMessage = e.message ?: "Riwayat transaksi belum dapat dimuat."
        } finally {
            isLoading = false
        }
    }

    LaunchedEffect(currentUserId) {
        isLoading = true
        refreshHistory()

        while (true) {
            delay(5000)
            refreshHistory()
        }
    }

    val filteredTransactions = transactions
        .filter {
            showTransaction
        }
        .filter { transaction ->
            filterHistoryByDateV2(
                transaction = transaction,
                selectedDateFilter = selectedDateFilter
            )
        }
        .filter { transaction ->
            filterHistoryByTypeV2(
                transaction = transaction,
                selectedTypeFilter = selectedTypeFilter,
                currentUserId = currentUserId
            )
        }

    LaunchedEffect(filteredTransactions, currentUserId) {
        totalOut = filteredTransactions
            .filter { isOutgoingForCurrentUserV2(it, currentUserId) }
            .sumOf { it.amount }

        totalIn = filteredTransactions
            .filter { isIncomingForCurrentUserV2(it, currentUserId) || isRefundTransactionV2(it) }
            .sumOf { it.amount }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
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
                        text = "$selectedDateFilter⌄",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            showDateSheet = true
                        }
                    )

                    HistoryFilterChipV2(
                        text = "$selectedTypeFilter⌄",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            showTypeSheet = true
                        }
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
                        text = "Tampilkan transaksi QRIS, transfer, dan pengembalian dana",
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
                        text = "Riwayat",
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

                errorMessage.isNotBlank() && filteredTransactions.isEmpty() -> {
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
                            text = "Belum ada transaksi sesuai filter.",
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

        if (showDateSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    showDateSheet = false
                },
                sheetState = dateSheetState,
                containerColor = Color.White,
                shape = RoundedCornerShape(
                    topStart = 18.dp,
                    topEnd = 18.dp
                )
            ) {
                HistorySelectionSheetV2(
                    title = "Pilih Tanggal",
                    description = "Silakan pilih periode riwayat transaksi yang ingin ditampilkan.",
                    options = listOf(
                        "1 Tahun Terakhir",
                        "30 Hari Terakhir",
                        "7 Hari Terakhir",
                        "Semua Waktu"
                    ),
                    selectedOption = selectedDateFilter,
                    onClose = {
                        showDateSheet = false
                    },
                    onOptionSelected = { selected ->
                        selectedDateFilter = selected
                        showDateSheet = false
                    }
                )
            }
        }

        if (showTypeSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    showTypeSheet = false
                },
                sheetState = typeSheetState,
                containerColor = Color.White,
                shape = RoundedCornerShape(
                    topStart = 18.dp,
                    topEnd = 18.dp
                )
            ) {
                HistorySelectionSheetV2(
                    title = "Pilih Jenis",
                    description = "Pilih jenis transaksi yang ingin ditampilkan di riwayat.",
                    options = listOf(
                        "Semua Transaksi",
                        "Saldo Masuk",
                        "Saldo Keluar",
                        "Pembayaran QRIS",
                        "Transfer Masuk",
                        "Transfer Keluar",
                        "Pengembalian Dana",
                        "Transaksi Gagal"
                    ),
                    selectedOption = selectedTypeFilter,
                    onClose = {
                        showTypeSheet = false
                    },
                    onOptionSelected = { selected ->
                        selectedTypeFilter = selected
                        showTypeSheet = false
                    }
                )
            }
        }
    }
}

@Composable
fun HistoryFilterChipV2(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(46.dp)
            .clickable {
                onClick()
            },
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
private fun HistorySelectionSheetV2(
    title: String,
    description: String,
    options: List<String>,
    selectedOption: String,
    onClose: () -> Unit,
    onOptionSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = AppColor.TextDark
            )

            Text(
                text = "×",
                modifier = Modifier.clickable {
                    onClose()
                },
                style = MaterialTheme.typography.headlineSmall,
                color = AppColor.TextGray,
                textAlign = TextAlign.Center
            )
        }

        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color(0xFFEDEDED))
        )

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = description,
            modifier = Modifier.padding(horizontal = 18.dp),
            style = MaterialTheme.typography.bodySmall,
            color = AppColor.TextGray
        )

        Spacer(modifier = Modifier.height(10.dp))

        options.forEach { option ->
            HistorySheetOptionV2(
                text = option,
                selected = option == selectedOption,
                onClick = {
                    onOptionSelected(option)
                }
            )
        }

        Spacer(modifier = Modifier.height(26.dp))
    }
}

@Composable
private fun HistorySheetOptionV2(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clickable {
                onClick()
            }
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = AppColor.TextDark
        )

        if (selected) {
            Text(
                text = "✓",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFF6A00)
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
    val type = normalizeHistoryTransactionTypeV2(transaction)
    val isIncoming = isIncomingForCurrentUserV2(transaction, currentUserId)
    val isRefund = isRefundTransactionV2(transaction)
    val isFailed = transaction.status.uppercase() != "SUCCESS"

    val title = when {
        type == "QRIS" -> {
            "Bayar QRIS ke ${safeHistoryMerchantNameV2(transaction)}"
        }

        type == "TRANSFER" && isIncoming -> {
            "Terima uang dari ${safeHistorySenderNameV2(transaction)}"
        }

        type == "TRANSFER" -> {
            "Transfer ke ${safeHistoryRecipientNameV2(transaction)}"
        }

        isRefund -> {
            "Pengembalian Dana"
        }

        else -> {
            "Transaksi"
        }
    }

    val subtitle = when {
        type == "QRIS" -> "Pembayaran QRIS"
        type == "TRANSFER" && isIncoming -> "Transfer masuk"
        type == "TRANSFER" -> "Transfer keluar"
        isRefund -> "Dana dikembalikan ke saldo"
        else -> "Aktivitas transaksi"
    }

    val amountText = if (isIncoming || isRefund) {
        "+${formatRupiah(transaction.amount)}"
    } else {
        "-${formatRupiah(transaction.amount)}"
    }

    val iconText = when {
        type == "QRIS" -> "QR"
        type == "TRANSFER" -> "TF"
        isRefund -> "RF"
        else -> "TR"
    }

    val iconBackground = when {
        isFailed -> Color(0xFFFFEBEE)
        isIncoming || isRefund -> Color(0xFFE8F5E9)
        else -> Color(0xFFFFF3E0)
    }

    val amountColor = if (isIncoming || isRefund) {
        Color(0xFF008A3D)
    } else if (isFailed) {
        Color(0xFFC62828)
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
                color = if (isFailed) Color(0xFFC62828) else AppColor.Primary,
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
                text = formatHistoryStatusTextV2(transaction.status),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = if (transaction.status.uppercase() == "SUCCESS") {
                    Color(0xFF008A3D)
                } else {
                    Color(0xFFC62828)
                }
            )
        }
    }
}

private fun filterHistoryByDateV2(
    transaction: TransactionData,
    selectedDateFilter: String
): Boolean {
    if (selectedDateFilter == "Semua Waktu") {
        return true
    }

    val transactionMillis = parseHistoryTimeMillisV2(transaction.createdAt)

    if (transactionMillis == 0L) {
        return true
    }

    val now = System.currentTimeMillis()

    val maxAgeMillis = when (selectedDateFilter) {
        "7 Hari Terakhir" -> 7L * 24L * 60L * 60L * 1000L
        "30 Hari Terakhir" -> 30L * 24L * 60L * 60L * 1000L
        "1 Tahun Terakhir" -> 365L * 24L * 60L * 60L * 1000L
        else -> 30L * 24L * 60L * 60L * 1000L
    }

    return now - transactionMillis <= maxAgeMillis
}

private fun filterHistoryByTypeV2(
    transaction: TransactionData,
    selectedTypeFilter: String,
    currentUserId: Int
): Boolean {
    val type = normalizeHistoryTransactionTypeV2(transaction)
    val isIncoming = isIncomingForCurrentUserV2(transaction, currentUserId)
    val isOutgoing = isOutgoingForCurrentUserV2(transaction, currentUserId)
    val isRefund = isRefundTransactionV2(transaction)
    val isFailed = transaction.status.uppercase() != "SUCCESS"

    return when (selectedTypeFilter) {
        "Semua Transaksi" -> true
        "Saldo Masuk" -> isIncoming || isRefund
        "Saldo Keluar" -> isOutgoing
        "Pembayaran QRIS" -> type == "QRIS"
        "Transfer Masuk" -> type == "TRANSFER" && isIncoming
        "Transfer Keluar" -> type == "TRANSFER" && isOutgoing
        "Pengembalian Dana" -> isRefund
        "Transaksi Gagal" -> isFailed
        else -> true
    }
}

private fun isIncomingForCurrentUserV2(
    transaction: TransactionData,
    currentUserId: Int
): Boolean {
    return normalizeHistoryTransactionTypeV2(transaction) == "TRANSFER" &&
            transaction.recipientUserId == currentUserId &&
            transaction.userId.toIntOrNull() != currentUserId
}

private fun isOutgoingForCurrentUserV2(
    transaction: TransactionData,
    currentUserId: Int
): Boolean {
    val type = normalizeHistoryTransactionTypeV2(transaction)

    return when {
        isRefundTransactionV2(transaction) -> false
        type == "TRANSFER" -> transaction.userId.toIntOrNull() == currentUserId
        type == "QRIS" -> transaction.userId.toIntOrNull() == currentUserId
        else -> transaction.userId.toIntOrNull() == currentUserId
    }
}

private fun isRefundTransactionV2(
    transaction: TransactionData
): Boolean {
    val type = transaction.transactionType.trim().uppercase()
    val direction = transaction.direction.trim().uppercase()

    return type == "REFUND" ||
            direction == "REFUND" ||
            transaction.merchantName.contains("refund", ignoreCase = true)
}

private fun normalizeHistoryTransactionTypeV2(
    transaction: TransactionData
): String {
    val rawType = transaction.transactionType.trim().uppercase()

    return when {
        rawType == "QRIS" -> "QRIS"
        rawType == "TRANSFER" -> "TRANSFER"
        rawType == "REFUND" -> "REFUND"
        else -> "TRANSACTION"
    }
}

private fun safeHistoryMerchantNameV2(
    transaction: TransactionData
): String {
    return transaction.merchantName
        .takeIf { it.isNotBlank() && it.lowercase() != "null" }
        ?: "Merchant"
}

private fun safeHistoryRecipientNameV2(
    transaction: TransactionData
): String {
    return transaction.recipientUserName
        ?.takeIf { it.isNotBlank() && it.lowercase() != "null" }
        ?: transaction.merchantName.takeIf { it.isNotBlank() && it.lowercase() != "null" }
        ?: "Penerima"
}

private fun safeHistorySenderNameV2(
    transaction: TransactionData
): String {
    return transaction.senderName
        ?.takeIf { it.isNotBlank() && it.lowercase() != "null" }
        ?: "Pengirim"
}

private fun formatHistoryStatusTextV2(
    status: String
): String {
    return when (status.uppercase()) {
        "SUCCESS" -> "Berhasil"
        "FAILED" -> "Gagal"
        "TIMEOUT" -> "Timeout"
        else -> status.ifBlank { "Unknown" }
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

private fun parseHistoryTimeMillisV2(
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

            if (date != null) {
                return date.time
            }
        } catch (_: Exception) {
        }
    }

    return 0L
}
