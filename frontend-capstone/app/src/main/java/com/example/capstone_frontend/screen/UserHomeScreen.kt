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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import com.example.capstone_frontend.component.TopFloatingNotification
import com.example.capstone_frontend.component.TopNotificationType
import com.example.capstone_frontend.component.TopNotificationUiState
import com.example.capstone_frontend.component.formatRupiah
import com.example.capstone_frontend.data.DummyRepository
import com.example.capstone_frontend.data.NotificationReadStore
import com.example.capstone_frontend.data.RetrofitClient
import com.example.capstone_frontend.model.TransactionData

@Composable
fun UserHomeScreen(
    onPayQrisClick: () -> Unit,
    onRegularTransactionClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onHelpClick: () -> Unit,
    onLogout: () -> Unit
) {
    var balance by remember {
        mutableIntStateOf(0)
    }

    var isBalanceLoaded by remember {
        mutableStateOf(false)
    }

    var balanceError by remember {
        mutableStateOf("")
    }

    var isBalanceVisible by remember {
        mutableStateOf(true)
    }

    var showLogoutDialog by remember {
        mutableStateOf(false)
    }

    var showDeleteDialog by remember {
        mutableStateOf(false)
    }

    var showTopNotification by remember {
        mutableStateOf(false)
    }

    var topNotificationData by remember {
        mutableStateOf<TopNotificationUiState?>(null)
    }

    var currentTransactions by remember {
        mutableStateOf<List<TransactionData>>(emptyList())
    }

    var unreadNotificationCount by remember {
        mutableIntStateOf(0)
    }

    val currentUserName = DummyRepository.getCurrentUserFirstName()
    val currentUserId = DummyRepository.getCurrentUserId()

    LaunchedEffect(currentUserId) {
        try {
            isBalanceLoaded = false
            balanceError = ""

            val response = RetrofitClient.api.getBalance(currentUserId)

            balance = response.balance
            isBalanceLoaded = true
            balanceError = ""
        } catch (e: Exception) {
            isBalanceLoaded = false
            balanceError = e.message ?: e.toString()
        }
    }

    LaunchedEffect(currentUserId) {
        try {
            try {
                val merchantResponse = RetrofitClient.api.getMerchants()
                DummyRepository.setMerchantCache(merchantResponse.merchants)
            } catch (_: Exception) {
            }

            val transactionResponse = RetrofitClient.api.getTransactions(currentUserId)
            val convertedTransactions = DummyRepository.convertBackendTransactions(transactionResponse)

            DummyRepository.setBackendTransactions(convertedTransactions)
            currentTransactions = convertedTransactions

            unreadNotificationCount = NotificationReadStore.getUnreadCount(
                userId = currentUserId,
                transactions = convertedTransactions
            )

            val latestNotificationTransaction = convertedTransactions.firstOrNull { transaction ->
                NotificationReadStore.shouldCountAsNotification(
                    transaction = transaction,
                    currentUserId = currentUserId
                )
            }

            if (latestNotificationTransaction != null) {
                val notification = buildHomeTopNotification(
                    transaction = latestNotificationTransaction,
                    currentUserId = currentUserId
                )

                if (
                    notification != null &&
                    !NotificationReadStore.hasShownPopup(
                        userId = currentUserId,
                        transactionId = latestNotificationTransaction.transactionId
                    )
                ) {
                    topNotificationData = notification
                    showTopNotification = true

                    NotificationReadStore.markPopupShown(
                        userId = currentUserId,
                        transactionId = latestNotificationTransaction.transactionId
                    )
                }
            }
        } catch (_: Exception) {
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColor.Background)
                .padding(horizontal = 22.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(42.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Selamat siang, $currentUserName",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = AppColor.TextDark
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Siap melakukan transaksi hari ini?",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColor.TextGray
                        )
                    }

                    NotificationBellButton(
                        unreadCount = unreadNotificationCount,
                        onClick = {
                            /*
                             * Penting:
                             * Klik lonceng hanya membuka halaman notifikasi.
                             * Jangan langsung mark semua sebagai sudah dibaca.
                             * Notif baru dianggap dibaca setelah user klik item notifikasinya.
                             */
                            onNotificationClick()
                        }
                    )
                }

                Spacer(modifier = Modifier.height(22.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = AppColor.Primary),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(22.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Intip Saldo",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFFFD6D6)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            if (isBalanceLoaded) {
                                Text(
                                    text = if (isBalanceVisible) {
                                        formatRupiah(balance)
                                    } else {
                                        "Rp ••••••"
                                    },
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "User ID $currentUserId • Saldo berhasil dimuat.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFFFE2E8)
                                )
                            } else {
                                Text(
                                    text = "Saldo belum tersedia",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "User ID $currentUserId • Gagal memuat saldo.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFFFE2E8)
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .background(
                                    color = Color(0x22FFFFFF),
                                    shape = CircleShape
                                )
                                .clickable {
                                    isBalanceVisible = !isBalanceVisible
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isBalanceVisible) {
                                    Icons.Filled.Visibility
                                } else {
                                    Icons.Filled.VisibilityOff
                                },
                                contentDescription = "Tampilkan saldo",
                                tint = Color.White
                            )
                        }
                    }
                }

                if (balanceError.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp)
                        ) {
                            Text(
                                text = "Detail error saldo",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFC62828)
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = balanceError,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFC62828)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(26.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 18.dp, horizontal = 12.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HomeMenuItemV2(
                            iconText = "QR",
                            label = "QR Bayar",
                            onClick = onPayQrisClick
                        )

                        HomeMenuItemV2(
                            iconText = "TF",
                            label = "Transfer",
                            onClick = onRegularTransactionClick
                        )

                        HomeMenuItemV2(
                            iconText = "≡",
                            label = "Riwayat",
                            onClick = onHistoryClick
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp)
                    ) {
                        Text(
                            text = "Pengaturan Akun",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = AppColor.TextDark
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Kelola akses akun nasabah yang sedang digunakan.",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppColor.TextGray
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Button(
                            onClick = {
                                showLogoutDialog = true
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppColor.Primary,
                                contentColor = Color.White
                            )
                        ) {
                            Text(
                                text = "Logout",
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        TextButton(
                            onClick = {
                                showDeleteDialog = true
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Hapus Akun",
                                color = Color(0xFFD32F2F),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))
            }
        }

        TopFloatingNotification(
            visible = showTopNotification,
            notification = topNotificationData,
            modifier = Modifier.align(Alignment.TopCenter),
            onDismiss = {
                showTopNotification = false
                topNotificationData = null
            },
            onActionClick = {
                /*
                 * Klik "Lihat Notifikasi" juga hanya membuka halaman notifikasi.
                 * Tidak langsung menghapus semua badge.
                 */
                onNotificationClick()
            }
        )
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = {
                showLogoutDialog = false
            },
            title = {
                Text(
                    text = "Logout dari akun?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Anda akan keluar dari akun nasabah yang sedang digunakan."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        DummyRepository.logoutUser()
                        onLogout()
                    }
                ) {
                    Text(
                        text = "Logout",
                        color = AppColor.Primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                    }
                ) {
                    Text("Batal")
                }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
            },
            title = {
                Text(
                    text = "Hapus akun?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Akses akun nasabah yang sedang digunakan akan dihapus dari aplikasi."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        DummyRepository.deleteCurrentAccount()
                        onLogout()
                    }
                ) {
                    Text(
                        text = "Hapus Akun",
                        color = Color(0xFFD32F2F),
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                    }
                ) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
private fun NotificationBellButton(
    unreadCount: Int,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier.size(56.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    color = Color.White,
                    shape = CircleShape
                )
                .clickable {
                    onClick()
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🔔",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )
        }

        if (unreadCount > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-2).dp, y = 2.dp)
                    .size(if (unreadCount > 9) 24.dp else 20.dp)
                    .background(
                        color = Color(0xFFFF6A00),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (unreadCount > 9) "9+" else unreadCount.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun HomeMenuItemV2(
    iconText: String,
    label: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier.clickable {
            onClick()
        },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .background(
                    color = Color(0xFFFFEEF4),
                    shape = RoundedCornerShape(18.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = iconText,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = AppColor.Primary,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = AppColor.TextDark,
            textAlign = TextAlign.Center
        )
    }
}

private fun buildHomeTopNotification(
    transaction: TransactionData,
    currentUserId: Int
): TopNotificationUiState? {
    val type = transaction.transactionType.uppercase()

    val isIncomingTransfer = type == "TRANSFER" &&
            transaction.recipientUserId == currentUserId &&
            transaction.userId.toIntOrNull() != currentUserId

    val isFailed = transaction.status.uppercase() != "SUCCESS"

    return when {
        isFailed && type == "TRANSFER" -> {
            TopNotificationUiState(
                title = "Transfer Gagal",
                message = "Transfer ke ${transaction.recipientUserName ?: transaction.merchantName} gagal diproses.",
                type = TopNotificationType.ERROR,
                actionLabel = "Lihat Notifikasi"
            )
        }

        isFailed && type == "QRIS" -> {
            TopNotificationUiState(
                title = "Pembayaran QRIS Gagal",
                message = "Pembayaran QRIS ke ${transaction.merchantName} gagal diproses.",
                type = TopNotificationType.ERROR,
                actionLabel = "Lihat Notifikasi"
            )
        }

        type == "TRANSFER" && isIncomingTransfer -> {
            TopNotificationUiState(
                title = "Transfer Masuk",
                message = "🎉 Kamu menerima ${formatRupiah(transaction.amount)} dari ${transaction.senderName ?: "pengirim"}.",
                type = TopNotificationType.SUCCESS,
                actionLabel = "Lihat Notifikasi"
            )
        }

        type == "TRANSFER" -> {
            TopNotificationUiState(
                title = "Transfer Berhasil",
                message = "Transfer ${formatRupiah(transaction.amount)} ke ${transaction.recipientUserName ?: transaction.merchantName} berhasil.",
                type = TopNotificationType.SUCCESS,
                actionLabel = "Lihat Notifikasi"
            )
        }

        type == "QRIS" -> {
            TopNotificationUiState(
                title = "Pembayaran QRIS Berhasil",
                message = "Pembayaran ${formatRupiah(transaction.amount)} ke ${transaction.merchantName} berhasil.",
                type = TopNotificationType.INFO,
                actionLabel = "Lihat Notifikasi"
            )
        }

        else -> null
    }
}