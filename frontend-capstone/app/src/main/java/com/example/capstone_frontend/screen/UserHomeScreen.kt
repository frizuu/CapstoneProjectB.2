package com.example.capstone_frontend.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.dp
import com.example.capstone_frontend.component.AppColor
import com.example.capstone_frontend.component.FeatureMenuItem
import com.example.capstone_frontend.component.formatRupiah
import com.example.capstone_frontend.data.DummyRepository
import com.example.capstone_frontend.data.RetrofitClient

@Composable
fun UserHomeScreen(
    onPayQrisClick: () -> Unit,
    onRegularTransactionClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onStatusClick: () -> Unit,
    onHelpClick: () -> Unit,
    onLogout: () -> Unit
) {
    var balance by remember {
        mutableIntStateOf(0)
    }

    var showBalance by remember {
        mutableStateOf(false)
    }

    var isLoadingBalance by remember {
        mutableStateOf(true)
    }

    var balanceError by remember {
        mutableStateOf(false)
    }

    var showLogoutDialog by remember {
        mutableStateOf(false)
    }

    var showDeleteAccountDialog by remember {
        mutableStateOf(false)
    }

    val userFirstName = DummyRepository.getCurrentUserFirstName()
    val userFullName = DummyRepository.getCurrentUserName()

    LaunchedEffect(Unit) {
        try {
            isLoadingBalance = true
            balanceError = false

            val response = RetrofitClient.api.getBalance(1)
            balance = response.balance
        } catch (e: Exception) {
            balanceError = true
        } finally {
            isLoadingBalance = false
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = {
                showLogoutDialog = false
            },
            title = {
                Text(
                    text = "Keluar dari Akun?",
                    fontWeight = FontWeight.Bold,
                    color = AppColor.TextDark
                )
            },
            text = {
                Text(
                    text = "Anda akan keluar dari akun $userFirstName. Pastikan semua transaksi sudah selesai sebelum keluar.",
                    color = AppColor.TextGray
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        DummyRepository.logoutUser()
                        onLogout()
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColor.Primary,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "Ya, Keluar",
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
                    Text(
                        text = "Batal",
                        color = AppColor.Primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp)
        )
    }

    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = {
                showDeleteAccountDialog = false
            },
            title = {
                Text(
                    text = "Hapus Akun?",
                    fontWeight = FontWeight.Bold,
                    color = AppColor.TextDark
                )
            },
            text = {
                Text(
                    text = "Akun $userFullName akan dihapus dari perangkat ini. Anda akan keluar dari akun, dan perlu membuat akun baru atau masuk kembali untuk menggunakan layanan.",
                    color = AppColor.TextGray
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteAccountDialog = false
                        DummyRepository.deleteCurrentAccount()
                        onLogout()
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFC62828),
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "Hapus Akun",
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteAccountDialog = false
                    }
                ) {
                    Text(
                        text = "Batal",
                        color = AppColor.Primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp)
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColor.Background)
            .padding(20.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Selamat siang, $userFirstName",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = AppColor.TextDark
            )

            Text(
                text = "Siap melakukan transaksi hari ini?",
                style = MaterialTheme.typography.bodyMedium,
                color = AppColor.TextGray
            )

            Spacer(modifier = Modifier.height(18.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = AppColor.Primary),
                elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Intip Saldo",
                            color = Color(0xFFFFD6D6),
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = when {
                                isLoadingBalance -> "Memuat saldo..."
                                balanceError -> "Saldo belum tersedia"
                                showBalance -> formatRupiah(balance)
                                else -> "Rp •••••••"
                            },
                            color = Color.White,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = if (balanceError) {
                                "Periksa koneksi dan coba kembali."
                            } else {
                                "Gunakan saldo untuk QRIS dan transfer."
                            },
                            color = Color(0xFFFFD6D6),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    IconButton(
                        onClick = {
                            showBalance = !showBalance
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = Color(0x22FFFFFF),
                                shape = RoundedCornerShape(24.dp)
                            )
                    ) {
                        Icon(
                            imageVector = if (showBalance) {
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

            Spacer(modifier = Modifier.height(28.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 18.dp, horizontal = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.clickable {
                            onPayQrisClick()
                        },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        FeatureMenuItem(
                            title = "QR Bayar",
                            iconText = "QR"
                        )
                    }

                    Column(
                        modifier = Modifier.clickable {
                            onRegularTransactionClick()
                        },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        FeatureMenuItem(
                            title = "Transfer",
                            iconText = "TF"
                        )
                    }

                    Column(
                        modifier = Modifier.clickable {
                            onHistoryClick()
                        },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        FeatureMenuItem(
                            title = "Riwayat",
                            iconText = "≡"
                        )
                    }

                    Column(
                        modifier = Modifier.clickable {
                            onStatusClick()
                        },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        FeatureMenuItem(
                            title = "Status",
                            iconText = "✓"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

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

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            showLogoutDialog = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(18.dp),
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

                    Spacer(modifier = Modifier.height(10.dp))

                    TextButton(
                        onClick = {
                            showDeleteAccountDialog = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Hapus Akun",
                            color = Color(0xFFC62828),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}