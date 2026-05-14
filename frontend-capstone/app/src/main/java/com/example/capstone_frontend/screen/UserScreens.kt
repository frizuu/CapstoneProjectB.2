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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.capstone_frontend.component.AppColor
import com.example.capstone_frontend.component.InfoRow
import com.example.capstone_frontend.component.StatusChip
import com.example.capstone_frontend.component.formatRupiah
import com.example.capstone_frontend.data.DummyRepository
import com.example.capstone_frontend.data.RetrofitClient
import com.example.capstone_frontend.model.TransactionData
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs

@Composable
fun QrisPaymentScreen(
    onBack: () -> Unit,
    onPaymentSubmit: (Int, String) -> Unit
) {
    var amountText by remember {
        mutableStateOf("1000")
    }

    val merchantName = DummyRepository.getCurrentMerchantName()
    val merchantCode = DummyRepository.getCurrentMerchantCode()
    val merchantCategory = DummyRepository.getCurrentMerchantCategory()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColor.Background)
            .padding(20.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(28.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onBack
                ) {
                    Text(
                        text = "←",
                        fontWeight = FontWeight.Bold,
                        color = AppColor.Primary
                    )
                }

                Column {
                    Text(
                        text = "QRIS Payment",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = AppColor.TextDark
                    )

                    Text(
                        text = "Konfirmasi pembayaran sebelum melanjutkan.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColor.TextGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Detail QRIS",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AppColor.TextDark
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    InfoRow("Merchant", merchantName)
                    InfoRow("Merchant Code", merchantCode)
                    InfoRow("Kategori", merchantCategory)
                    InfoRow("Sumber Dana", "Saldo Utama")

                    Spacer(modifier = Modifier.height(22.dp))

                    Text(
                        text = "Nominal Transaksi",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = AppColor.TextDark
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = amountText,
                        onValueChange = {
                            amountText = it.filter { char ->
                                char.isDigit()
                            }
                        },
                        label = {
                            Text("Masukkan nominal")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            val amount = amountText.toIntOrNull() ?: 1000
                            onPaymentSubmit(amount, "Baseline")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(22.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColor.Primary,
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = "Lanjutkan Pembayaran",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(
                        onClick = onBack,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Scan Ulang QRIS",
                            color = AppColor.Primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentResultScreen(
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
                    StatusChip(status = transaction.status)

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = when (transaction.status) {
                            "SUCCESS" -> if (isRegularTransferTransaction(transaction.merchantName)) {
                                "Transfer Berhasil"
                            } else {
                                "Pembayaran Berhasil"
                            }

                            "TIMEOUT" -> "Transaksi Sedang Diproses"
                            "FAILED" -> "Transaksi Gagal"
                            else -> "Status Transaksi"
                        },
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = AppColor.TextDark
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    InfoRow("Transaction ID", transaction.transactionId)
                    InfoRow("Penerima", getDisplayMerchantName(transaction))
                    InfoRow("Nominal", formatRupiah(transaction.amount))
                    InfoRow("Status", userFriendlyStatus(transaction.status))
                    InfoRow("Waktu", formatUserFriendlyDate(transaction.createdAt))

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = userFriendlyMessage(transaction.status),
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColor.TextGray
                    )

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
                        Text("Kembali ke Home")
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
            Text(
                text = "Transaksi tidak ditemukan.",
                color = AppColor.TextDark
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionHistoryScreen(
    onBack: () -> Unit,
    onTransactionClick: (String) -> Unit
) {
    var transactions by remember {
        mutableStateOf<List<TransactionData>>(emptyList())
    }

    var isLoading by remember {
        mutableStateOf(true)
    }

    var isError by remember {
        mutableStateOf(false)
    }

    var selectedDateFilter by remember {
        mutableStateOf("30 Hari Terakhir")
    }

    var selectedTypeFilter by remember {
        mutableStateOf("Semua Transaksi")
    }

    var showQrisTransaction by remember {
        mutableStateOf(true)
    }

    var activeBottomSheet by remember {
        mutableStateOf<String?>(null)
    }

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    LaunchedEffect(Unit) {
        try {
            isLoading = true
            isError = false

            val response = RetrofitClient.api.getTransactions(1)
            val convertedTransactions = DummyRepository.convertBackendTransactions(response)

            DummyRepository.setBackendTransactions(convertedTransactions)
            transactions = DummyRepository.getTransactions()
        } catch (e: Exception) {
            isError = true
            transactions = DummyRepository.getTransactions()
        } finally {
            isLoading = false
        }
    }

    val filteredTransactions = transactions
        .filter {
            showQrisTransaction
        }
        .filter {
            filterByDate(it, selectedDateFilter)
        }
        .filter {
            filterByTransactionType(it, selectedTypeFilter)
        }

    val groupedTransactions = filteredTransactions.groupBy {
        formatHistoryMonth(it.createdAt)
    }

    if (activeBottomSheet == "date") {
        ModalBottomSheet(
            onDismissRequest = {
                activeBottomSheet = null
            },
            sheetState = sheetState,
            containerColor = Color.White
        ) {
            DateFilterBottomSheet(
                selectedDateFilter = selectedDateFilter,
                onSelect = { selected ->
                    selectedDateFilter = selected
                    activeBottomSheet = null
                },
                onClose = {
                    activeBottomSheet = null
                }
            )
        }
    }

    if (activeBottomSheet == "type") {
        ModalBottomSheet(
            onDismissRequest = {
                activeBottomSheet = null
            },
            sheetState = sheetState,
            containerColor = Color.White
        ) {
            TypeFilterBottomSheet(
                selectedTypeFilter = selectedTypeFilter,
                onSelect = { selected ->
                    selectedTypeFilter = selected
                    activeBottomSheet = null
                },
                onClose = {
                    activeBottomSheet = null
                }
            )
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColor.Background)
            .padding(horizontal = 20.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(28.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onBack
                ) {
                    Text(
                        text = "←",
                        fontWeight = FontWeight.Bold,
                        color = AppColor.Primary
                    )
                }

                Text(
                    text = "Riwayat Transaksi",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = AppColor.TextDark
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HistoryFilterChip(
                    text = selectedDateFilter,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        activeBottomSheet = "date"
                    }
                )

                HistoryFilterChip(
                    text = selectedTypeFilter,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        activeBottomSheet = "type"
                    }
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        showQrisTransaction = !showQrisTransaction
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .background(
                            color = if (showQrisTransaction) {
                                AppColor.Primary
                            } else {
                                Color.Transparent
                            },
                            shape = RoundedCornerShape(4.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (showQrisTransaction) {
                        Text(
                            text = "✓",
                            color = Color.White,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = "Tampilkan transaksi QRIS dan transfer",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColor.TextDark
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            if (isLoading) {
                Text(
                    text = "Memuat riwayat transaksi...",
                    color = AppColor.TextGray
                )

                Spacer(modifier = Modifier.height(12.dp))
            }

            if (isError) {
                Text(
                    text = "Riwayat transaksi belum dapat dimuat. Silakan coba kembali beberapa saat lagi.",
                    color = AppColor.TextGray
                )

                Spacer(modifier = Modifier.height(12.dp))
            }

            if (!isLoading && filteredTransactions.isEmpty()) {
                Text(
                    text = if (!showQrisTransaction) {
                        "Transaksi sedang disembunyikan."
                    } else {
                        "Belum ada transaksi sesuai filter."
                    },
                    color = AppColor.TextGray
                )

                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        groupedTransactions.forEach { (month, transactionGroup) ->
            item {
                HistoryMonthHeader(
                    month = month,
                    transactions = transactionGroup
                )
            }

            items(transactionGroup) { transaction ->
                BankStyleTransactionItem(
                    transaction = transaction,
                    onClick = {
                        onTransactionClick(transaction.transactionId)
                    }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun DateFilterBottomSheet(
    selectedDateFilter: String,
    onSelect: (String) -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 26.dp)
    ) {
        BottomSheetHeader(
            title = "Pilih Tanggal",
            onClose = onClose
        )

        Divider(color = Color(0xFFEFEFF2))

        Spacer(modifier = Modifier.height(22.dp))

        Text(
            text = "Silakan cek e-Statement untuk transaksi lebih dari 12 bulan.",
            modifier = Modifier.padding(horizontal = 24.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = AppColor.TextGray
        )

        Spacer(modifier = Modifier.height(20.dp))

        BottomSheetOptionRow(
            title = "1 Tahun Terakhir",
            selected = selectedDateFilter == "1 Tahun Terakhir",
            onClick = {
                onSelect("1 Tahun Terakhir")
            }
        )

        BottomSheetOptionRow(
            title = "7 Hari Terakhir",
            selected = selectedDateFilter == "7 Hari Terakhir",
            onClick = {
                onSelect("7 Hari Terakhir")
            }
        )

        BottomSheetOptionRow(
            title = "30 Hari Terakhir",
            selected = selectedDateFilter == "30 Hari Terakhir",
            onClick = {
                onSelect("30 Hari Terakhir")
            }
        )
    }
}

@Composable
fun TypeFilterBottomSheet(
    selectedTypeFilter: String,
    onSelect: (String) -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 26.dp)
    ) {
        BottomSheetHeader(
            title = "Pilih Jenis",
            onClose = onClose
        )

        Divider(color = Color(0xFFEFEFF2))

        Spacer(modifier = Modifier.height(22.dp))

        Text(
            text = "Silakan cek e-Statement untuk transaksi lebih dari 12 bulan.",
            modifier = Modifier.padding(horizontal = 24.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = AppColor.TextGray
        )

        Spacer(modifier = Modifier.height(20.dp))

        BottomSheetOptionRow(
            title = "Semua Transaksi",
            selected = selectedTypeFilter == "Semua Transaksi",
            onClick = {
                onSelect("Semua Transaksi")
            }
        )

        BottomSheetOptionRow(
            title = "Saldo Masuk",
            selected = selectedTypeFilter == "Saldo Masuk",
            onClick = {
                onSelect("Saldo Masuk")
            }
        )

        BottomSheetOptionRow(
            title = "Saldo Keluar",
            selected = selectedTypeFilter == "Saldo Keluar",
            onClick = {
                onSelect("Saldo Keluar")
            }
        )
    }
}

@Composable
fun BottomSheetHeader(
    title: String,
    onClose: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = AppColor.TextDark
        )

        IconButton(
            onClick = onClose
        ) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Tutup",
                tint = AppColor.TextGray
            )
        }
    }
}

@Composable
fun BottomSheetOptionRow(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    showArrow: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            }
            .padding(horizontal = 24.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = if (selected) {
                AppColor.TextDark
            } else {
                AppColor.TextGray
            },
            fontWeight = if (selected) {
                FontWeight.SemiBold
            } else {
                FontWeight.Normal
            }
        )

        if (selected) {
            Text(
                text = "✓",
                style = MaterialTheme.typography.headlineSmall,
                color = AppColor.Primary,
                fontWeight = FontWeight.Bold
            )
        } else if (showArrow) {
            Text(
                text = "›",
                style = MaterialTheme.typography.headlineSmall,
                color = AppColor.TextGray,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun TransactionDetailScreen(
    transactionId: String,
    onBack: () -> Unit
) {
    val transaction = DummyRepository.getTransactionById(transactionId)

    if (transaction == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColor.Background)
                .padding(24.dp)
        ) {
            Spacer(modifier = Modifier.height(28.dp))

            TextButton(
                onClick = onBack
            ) {
                Text(
                    text = "← Kembali",
                    color = AppColor.Primary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Rincian transaksi tidak ditemukan.",
                style = MaterialTheme.typography.titleMedium,
                color = AppColor.TextDark
            )
        }

        return
    }

    val isTransfer = isRegularTransferTransaction(transaction.merchantName)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F7F9))
    ) {
        item {
            Spacer(modifier = Modifier.height(28.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onBack
                    ) {
                        Text(
                            text = "←",
                            color = AppColor.TextDark,
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

                Icon(
                    imageVector = Icons.Filled.Share,
                    contentDescription = "Bagikan",
                    tint = AppColor.TextDark
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "Rp",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppColor.TextDark
                )

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = formatRupiah(transaction.amount).replace("Rp", "").trim(),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = AppColor.TextDark
                )
            }

            Spacer(modifier = Modifier.height(58.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 22.dp, vertical = 18.dp)
                ) {
                    DetailPersonRow(
                        label = "Dari",
                        name = DummyRepository.getCurrentUserName(),
                        subtitle = "Saldo Utama"
                    )

                    Divider(
                        modifier = Modifier.padding(vertical = 18.dp),
                        color = Color(0xFFEFEFF2)
                    )

                    DetailPersonRow(
                        label = "Ke",
                        name = getDisplayMerchantName(transaction),
                        subtitle = if (isTransfer) {
                            "Transfer"
                        } else {
                            "Pembayaran"
                        }
                    )

                    Divider(
                        modifier = Modifier.padding(vertical = 18.dp),
                        color = Color(0xFFEFEFF2)
                    )

                    DetailInfoRow(
                        label = if (isTransfer) {
                            "Jumlah Transfer"
                        } else {
                            "Jumlah Pembayaran"
                        },
                        value = formatRupiahWithSpace(transaction.amount)
                    )

                    Divider(
                        modifier = Modifier.padding(vertical = 18.dp),
                        color = Color(0xFFEFEFF2)
                    )

                    DetailInfoRow(
                        label = "No. Transaksi",
                        value = transaction.transactionId
                    )

                    Divider(
                        modifier = Modifier.padding(vertical = 18.dp),
                        color = Color(0xFFEFEFF2)
                    )

                    DetailInfoRow(
                        label = "Metode Transaksi",
                        value = if (isTransfer) {
                            "Transfer"
                        } else {
                            "QRIS"
                        }
                    )

                    Divider(
                        modifier = Modifier.padding(vertical = 18.dp),
                        color = Color(0xFFEFEFF2)
                    )

                    DetailInfoRow(
                        label = "Status",
                        value = userFriendlyStatus(transaction.status)
                    )

                    Divider(
                        modifier = Modifier.padding(vertical = 18.dp),
                        color = Color(0xFFEFEFF2)
                    )

                    DetailInfoRow(
                        label = "Waktu Transaksi",
                        value = formatDetailTransactionDate(transaction.createdAt)
                    )
                }
            }

            Spacer(modifier = Modifier.height(34.dp))

            Text(
                text = "Butuh Bantuan?",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF2D7FA3)
            )

            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}

@Composable
fun DetailPersonRow(
    label: String,
    name: String,
    subtitle: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.width(78.dp),
            style = MaterialTheme.typography.titleMedium,
            color = AppColor.TextGray
        )

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
                text = name.take(1).uppercase(),
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AppColor.TextDark
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = AppColor.TextGray
            )
        }
    }
}

@Composable
fun DetailInfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleMedium,
            color = AppColor.TextGray
        )

        Text(
            text = value,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleMedium,
            color = AppColor.TextDark,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun TransactionStatusScreen(
    onBack: () -> Unit
) {
    var transactions by remember {
        mutableStateOf<List<TransactionData>>(emptyList())
    }

    var transactionIdInput by remember {
        mutableStateOf("")
    }

    var checkedTransaction by remember {
        mutableStateOf<TransactionData?>(null)
    }

    var hasChecked by remember {
        mutableStateOf(false)
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

            val response = RetrofitClient.api.getTransactions(1)
            val convertedTransactions = DummyRepository.convertBackendTransactions(response)

            DummyRepository.setBackendTransactions(convertedTransactions)
            transactions = DummyRepository.getTransactions()
        } catch (e: Exception) {
            isError = true
            transactions = DummyRepository.getTransactions()
        } finally {
            isLoading = false
        }
    }

    val latestTransaction = transactions.firstOrNull()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColor.Background)
            .padding(20.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(28.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onBack
                ) {
                    Text(
                        text = "←",
                        fontWeight = FontWeight.Bold,
                        color = AppColor.Primary
                    )
                }

                Column {
                    Text(
                        text = "Status Transaksi",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = AppColor.TextDark
                    )

                    Text(
                        text = "Cek status pembayaran berdasarkan ID transaksi.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColor.TextGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Cari Status Transaksi",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AppColor.TextDark
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Masukkan ID transaksi untuk memastikan transaksi berhasil, gagal, masih diproses, atau mengalami kendala.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColor.TextGray
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = transactionIdInput,
                        onValueChange = {
                            transactionIdInput = it.uppercase()
                        },
                        label = {
                            Text("Contoh: TRX-151")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            hasChecked = true

                            checkedTransaction = if (transactionIdInput.isBlank()) {
                                latestTransaction
                            } else {
                                findTransactionByInput(
                                    transactions = transactions,
                                    input = transactionIdInput
                                )
                            }

                            if (transactionIdInput.isBlank() && latestTransaction != null) {
                                transactionIdInput = latestTransaction.transactionId
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
                            text = "Cek Status",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (latestTransaction != null) {
                        Spacer(modifier = Modifier.height(12.dp))

                        TextButton(
                            onClick = {
                                transactionIdInput = latestTransaction.transactionId
                                hasChecked = true
                                checkedTransaction = latestTransaction
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Gunakan transaksi terakhir: ${latestTransaction.transactionId}",
                                color = AppColor.Primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            if (isLoading) {
                StatusInfoCard(
                    title = "Memuat data transaksi",
                    description = "Sistem sedang mengambil data transaksi."
                )
            }

            if (isError) {
                StatusInfoCard(
                    title = "Data status terbatas",
                    description = "Data transaksi belum dapat dimuat. Sistem menampilkan data lokal jika tersedia."
                )
            }

            if (hasChecked && checkedTransaction == null) {
                StatusInfoCard(
                    title = "Transaksi tidak ditemukan",
                    description = "Pastikan ID transaksi sudah benar. Anda juga dapat mengecek daftar transaksi melalui menu Riwayat."
                )
            }

            if (checkedTransaction != null) {
                StatusCheckResultCard(
                    transaction = checkedTransaction!!
                )
            }

            if (!hasChecked && !isLoading) {
                StatusInfoCard(
                    title = "Cek Status Transaksi",
                    description = "Fitur ini digunakan untuk memastikan status satu transaksi tertentu berdasarkan transaction ID."
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun StatusInfoCard(
    title: String,
    description: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AppColor.TextDark
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = AppColor.TextGray
            )
        }
    }
}

@Composable
fun StatusCheckResultCard(
    transaction: TransactionData
) {
    val isTransfer = isRegularTransferTransaction(transaction.merchantName)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Hasil Pengecekan",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AppColor.TextDark
            )

            Spacer(modifier = Modifier.height(12.dp))

            StatusChip(status = transaction.status)

            Spacer(modifier = Modifier.height(18.dp))

            InfoRow("ID Transaksi", transaction.transactionId)
            InfoRow("Penerima", getDisplayMerchantName(transaction))
            InfoRow("Jenis", if (isTransfer) "Transfer" else "Pembayaran")
            InfoRow("Nominal", formatRupiah(transaction.amount))
            InfoRow("Status", userFriendlyStatus(transaction.status))
            InfoRow("Waktu", formatUserFriendlyDate(transaction.createdAt))

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = statusCheckExplanation(transaction.status),
                style = MaterialTheme.typography.bodyMedium,
                color = AppColor.TextGray
            )
        }
    }
}

@Composable
fun HelpScreen(
    onBack: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColor.Background)
            .padding(20.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(28.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onBack
                ) {
                    Text(
                        text = "←",
                        fontWeight = FontWeight.Bold,
                        color = AppColor.Primary
                    )
                }

                Column {
                    Text(
                        text = "Bantuan",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = AppColor.TextDark
                    )

                    Text(
                        text = "Panduan menggunakan layanan QRIS.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColor.TextGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            HelpCard(
                title = "Bagaimana cara membayar dengan QRIS?",
                description = "Tekan menu QR Bayar, lalu arahkan kamera ke QR Code merchant."
            )

            HelpCard(
                title = "Bagaimana cara transfer saldo?",
                description = "Tekan menu Transfer, pilih bank tujuan, masukkan nomor rekening, lalu isi nominal transfer."
            )

            HelpCard(
                title = "Bagaimana cara memastikan pembayaran benar?",
                description = "Periksa nama penerima dan nominal transaksi sebelum melanjutkan pembayaran."
            )

            HelpCard(
                title = "Bagaimana cara melihat riwayat transaksi?",
                description = "Tekan menu Riwayat untuk melihat daftar transaksi yang pernah dilakukan."
            )

            HelpCard(
                title = "Apa yang harus dilakukan jika transaksi gagal?",
                description = "Periksa koneksi internet, pastikan data transaksi benar, lalu coba ulangi beberapa saat lagi."
            )
        }
    }
}

@Composable
fun UserTransactionCard(
    transaction: TransactionData
) {
    BankStyleTransactionItem(
        transaction = transaction,
        onClick = {}
    )
}

@Composable
fun HistoryFilterChip(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clickable {
                onClick()
            },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = AppColor.TextDark,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = "⌄",
                style = MaterialTheme.typography.bodyMedium,
                color = AppColor.TextDark,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun HistoryMonthHeader(
    month: String,
    transactions: List<TransactionData>
) {
    val totalOut = transactions
        .filter {
            it.status == "SUCCESS"
        }
        .sumOf {
            it.amount
        }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = month,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = AppColor.TextDark
        )

        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = "Saldo keluar: ${formatRupiahWithSpace(totalOut)}",
                style = MaterialTheme.typography.bodySmall,
                color = AppColor.TextGray
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Saldo masuk: Rp 0",
                style = MaterialTheme.typography.bodySmall,
                color = AppColor.TextGray
            )
        }
    }
}

@Composable
fun BankStyleTransactionItem(
    transaction: TransactionData,
    onClick: () -> Unit
) {
    val displayName = getDisplayMerchantName(transaction)
    val transactionDate = formatShortTransactionDate(transaction.createdAt)
    val isTransfer = isRegularTransferTransaction(transaction.merchantName)

    val transactionDescription = if (isTransfer) {
        "Transfer"
    } else {
        "Pembayaran"
    }

    val iconText = if (isTransfer) {
        "TF"
    } else {
        "QR"
    }

    val amountText = if (transaction.status == "SUCCESS") {
        "-${formatRupiahWithSpace(transaction.amount)}"
    } else {
        formatRupiahWithSpace(transaction.amount)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            }
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    color = Color(0xFFFFF3E6),
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

        Spacer(modifier = Modifier.width(14.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = displayName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AppColor.TextDark
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = transactionDescription,
                style = MaterialTheme.typography.bodyMedium,
                color = AppColor.TextDark
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = transactionDate,
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
                color = if (transaction.status == "SUCCESS") {
                    AppColor.TextDark
                } else {
                    AppColor.TextGray
                }
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = userFriendlyStatus(transaction.status),
                style = MaterialTheme.typography.bodySmall,
                color = when (transaction.status) {
                    "SUCCESS" -> Color(0xFF168A4A)
                    "FAILED" -> Color(0xFFC62828)
                    else -> Color(0xFFE69500)
                },
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun HelpCard(
    title: String,
    description: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 14.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AppColor.TextDark
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = AppColor.TextGray
            )
        }
    }
}

fun userFriendlyStatus(status: String): String {
    return when (status) {
        "SUCCESS" -> "Berhasil"
        "TIMEOUT" -> "Sedang Diproses"
        "FAILED" -> "Gagal"
        else -> "Tidak Diketahui"
    }
}

fun userFriendlyMessage(status: String): String {
    return when (status) {
        "SUCCESS" -> "Transaksi berhasil. Simpan bukti transaksi ini jika diperlukan."
        "TIMEOUT" -> "Transaksi sedang diproses. Silakan cek kembali status transaksi secara berkala."
        "FAILED" -> "Transaksi gagal diproses. Silakan periksa kembali detail transaksi atau coba beberapa saat lagi."
        else -> "Status transaksi belum dapat dipastikan. Silakan cek kembali melalui menu Riwayat."
    }
}

fun statusCheckExplanation(status: String): String {
    return when (status) {
        "SUCCESS" -> "Transaksi berhasil diproses oleh sistem. Simpan bukti transaksi jika diperlukan."
        "TIMEOUT" -> "Transaksi masih dalam proses pengecekan. Silakan coba cek kembali beberapa saat lagi."
        "FAILED" -> "Transaksi gagal diproses. Silakan periksa kembali detail transaksi atau lakukan transaksi ulang."
        else -> "Status transaksi belum dapat dipastikan. Silakan cek kembali secara berkala."
    }
}

fun getDisplayMerchantName(transaction: TransactionData): String {
    val rawName = transaction.merchantName

    if (isRegularTransferTransaction(rawName)) {
        return rawName
    }

    if (isGenericMerchantName(rawName)) {
        return getRandomQrisMerchantName(transaction.transactionId)
    }

    return rawName
}

fun getUserFriendlyMerchantName(merchantName: String): String {
    return when {
        merchantName.isBlank() -> "Merchant QRIS"
        merchantName.equals("Transaksi Baseline", ignoreCase = true) -> "Merchant QRIS"
        merchantName.equals("Baseline", ignoreCase = true) -> "Merchant QRIS"
        merchantName.equals("Penerima QRIS", ignoreCase = true) -> "Merchant QRIS"
        else -> merchantName
    }
}

fun isGenericMerchantName(merchantName: String): Boolean {
    val lowerName = merchantName.lowercase().trim()

    return lowerName.isBlank() ||
            lowerName == "merchant qris" ||
            lowerName == "transaksi baseline" ||
            lowerName == "baseline" ||
            lowerName == "penerima qris" ||
            lowerName == "scanned merchant"
}

fun getRandomQrisMerchantName(transactionId: String): String {
    val qrisMerchantNames = listOf(
        "QRIS Kopi Nusantara",
        "QRIS Warung Berkah",
        "QRIS Toko Sinar Jaya",
        "QRIS Resto Selera",
        "QRIS Kedai Harmoni",
        "QRIS Mart Sejahtera",
        "QRIS Bakso Merdeka",
        "QRIS Depot Kencana"
    )

    val index = abs(transactionId.hashCode()) % qrisMerchantNames.size

    return qrisMerchantNames[index]
}

fun isRegularTransferTransaction(merchantName: String): Boolean {
    val lowerName = merchantName.lowercase()

    return lowerName.contains("seabank") ||
            lowerName.contains("bank mandiri") ||
            lowerName.contains("bank rakyat indonesia") ||
            lowerName.contains("transfer")
}

fun formatRupiahWithSpace(amount: Int): String {
    return formatRupiah(amount).replace("Rp", "Rp ")
}

fun formatHistoryMonth(rawDate: String): String {
    val locale = Locale("id", "ID")
    val date = parseBackendDate(rawDate)

    return if (date != null) {
        SimpleDateFormat("MMMM yyyy", locale).format(date)
    } else {
        "Riwayat Transaksi"
    }
}

fun formatShortTransactionDate(rawDate: String): String {
    val locale = Locale("id", "ID")
    val date = parseBackendDate(rawDate)

    return if (date != null) {
        SimpleDateFormat("dd MMM yyyy, HH.mm", locale).format(date)
    } else {
        rawDate
    }
}

fun formatUserFriendlyDate(rawDate: String): String {
    val locale = Locale("id", "ID")
    val date = parseBackendDate(rawDate)

    return if (date != null) {
        SimpleDateFormat("dd MMMM yyyy, HH.mm 'WIB'", locale).format(date)
    } else {
        rawDate
    }
}

fun formatDetailTransactionDate(rawDate: String): String {
    val locale = Locale("id", "ID")
    val date = parseBackendDate(rawDate)

    return if (date != null) {
        SimpleDateFormat("dd MMMM yyyy, HH.mm", locale).format(date)
    } else {
        rawDate
    }
}

fun parseBackendDate(rawDate: String): java.util.Date? {
    val possibleFormats = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSSSSSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
        "yyyy-MM-dd HH:mm",
        "yyyy-MM-dd HH:mm:ss"
    )

    for (format in possibleFormats) {
        try {
            val parser = SimpleDateFormat(format, Locale.US)
            return parser.parse(rawDate)
        } catch (_: Exception) {
        }
    }

    return null
}

fun filterByDate(
    transaction: TransactionData,
    selectedDateFilter: String
): Boolean {
    val transactionDate = parseBackendDate(transaction.createdAt) ?: return true

    val calendar = Calendar.getInstance()

    when (selectedDateFilter) {
        "7 Hari Terakhir" -> {
            calendar.add(Calendar.DAY_OF_YEAR, -7)
        }

        "30 Hari Terakhir" -> {
            calendar.add(Calendar.DAY_OF_YEAR, -30)
        }

        "1 Tahun Terakhir" -> {
            calendar.add(Calendar.YEAR, -1)
        }

        else -> {
            calendar.add(Calendar.DAY_OF_YEAR, -30)
        }
    }

    return transactionDate.after(calendar.time)
}

fun filterByTransactionType(
    transaction: TransactionData,
    selectedTypeFilter: String
): Boolean {
    val isTransfer = isRegularTransferTransaction(transaction.merchantName)

    return when (selectedTypeFilter) {
        "Semua Transaksi" -> true
        "Saldo Keluar" -> true
        "Saldo Masuk" -> false
        else -> true
    }
}

fun findTransactionByInput(
    transactions: List<TransactionData>,
    input: String
): TransactionData? {
    val cleanInput = input
        .trim()
        .uppercase()
        .replace(" ", "")

    if (cleanInput.isBlank()) {
        return null
    }

    return transactions.find { transaction ->
        val cleanTransactionId = transaction.transactionId
            .trim()
            .uppercase()
            .replace(" ", "")

        cleanTransactionId == cleanInput ||
                cleanTransactionId.endsWith("-$cleanInput") ||
                cleanTransactionId.replace("TRX-", "") == cleanInput
    }
}