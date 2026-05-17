package com.example.capstone_frontend.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.capstone_frontend.component.AppColor
import com.example.capstone_frontend.component.InfoRow
import com.example.capstone_frontend.component.formatRupiah
import com.example.capstone_frontend.data.DummyRepository
import com.example.capstone_frontend.data.RetrofitClient

@Composable
fun RegularTransactionScreen(
    onBack: () -> Unit,
    onSubmitTransfer: (recipientUserId: Int, amount: Int, description: String) -> Unit
) {
    var recipientText by remember {
        mutableStateOf("")
    }

    var amountText by remember {
        mutableStateOf("")
    }

    var errorMessage by remember {
        mutableStateOf("")
    }

    var balance by remember {
        mutableIntStateOf(0)
    }

    var isLoadingBalance by remember {
        mutableStateOf(true)
    }

    val currentUserId = DummyRepository.getCurrentUserId()
    val currentUserName = DummyRepository.getCurrentUserName()

    val amount = amountText.toIntOrNull() ?: 0

    val recipientUser = remember(recipientText, currentUserId) {
        findRecipientUser(
            query = recipientText,
            currentUserId = currentUserId
        )
    }

    LaunchedEffect(currentUserId) {
        try {
            isLoadingBalance = true
            val response = RetrofitClient.api.getBalance(currentUserId)
            balance = response.balance
        } catch (_: Exception) {
            balance = 0
        } finally {
            isLoadingBalance = false
        }
    }

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
                        text = "Transfer Saldo",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = AppColor.TextDark
                    )

                    Text(
                        text = "Kirim saldo ke sesama user baseline.",
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
                        text = "Data Transfer",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AppColor.TextDark
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Masukkan nama, username tanpa spasi, atau User ID penerima.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColor.TextGray
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    OutlinedTextField(
                        value = recipientText,
                        onValueChange = {
                            recipientText = it
                            errorMessage = ""
                        },
                        label = {
                            Text("Nama / User ID Penerima")
                        },
                        placeholder = {
                            Text("Contoh: Lestari Rahayu")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    if (recipientText.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))

                        if (recipientUser != null) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF5F8)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp)
                                ) {
                                    Text(
                                        text = "Penerima ditemukan",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = AppColor.Primary
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = "${recipientUser.name} • User ID ${recipientUser.id}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = AppColor.TextDark
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = "Penerima tidak ditemukan.",
                                color = Color(0xFFC62828),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    OutlinedTextField(
                        value = amountText,
                        onValueChange = {
                            amountText = it.filter { char ->
                                char.isDigit()
                            }
                            errorMessage = ""
                        },
                        label = {
                            Text("Nominal Transfer")
                        },
                        placeholder = {
                            Text("Contoh: 10000")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        )
                    )

                    if (errorMessage.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = errorMessage,
                            color = Color(0xFFC62828),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF5F8)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Ringkasan Transfer",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = AppColor.TextDark
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            InfoRow("Dari", "$currentUserName • User ID $currentUserId")
                            InfoRow(
                                "Ke",
                                recipientUser?.let {
                                    "${it.name} • User ID ${it.id}"
                                } ?: "-"
                            )
                            InfoRow("Nominal", if (amount > 0) formatRupiah(amount) else "-")
                            InfoRow(
                                "Saldo Saat Ini",
                                if (isLoadingBalance) {
                                    "Memuat..."
                                } else {
                                    formatRupiah(balance)
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    Button(
                        onClick = {
                            when {
                                recipientText.isBlank() -> {
                                    errorMessage = "Nama atau User ID penerima wajib diisi."
                                }

                                recipientUser == null -> {
                                    errorMessage = "Penerima tidak ditemukan di data baseline."
                                }

                                recipientUser.id == currentUserId -> {
                                    errorMessage = "Penerima tidak boleh sama dengan akun pengirim."
                                }

                                amount <= 0 -> {
                                    errorMessage = "Nominal transfer wajib diisi."
                                }

                                amount < 1000 -> {
                                    errorMessage = "Nominal minimal Rp1.000."
                                }

                                balance > 0 && amount > balance -> {
                                    errorMessage = "Saldo tidak mencukupi."
                                }

                                else -> {
                                    onSubmitTransfer(
                                        recipientUser.id,
                                        amount,
                                        "Transfer ke ${recipientUser.name}"
                                    )
                                }
                            }
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
                            text = "Lanjutkan Transfer",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(
                        onClick = onBack,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Batalkan",
                            color = AppColor.Primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

private fun findRecipientUser(
    query: String,
    currentUserId: Int
): DummyRepository.BaselineUser? {
    val cleanQuery = query.trim().lowercase()

    if (cleanQuery.isBlank()) {
        return null
    }

    return DummyRepository.getBaselineUsers().find { user ->
        user.id != currentUserId &&
                (
                        user.id.toString() == cleanQuery ||
                                user.name.lowercase() == cleanQuery ||
                                user.name.lowercase().replace(" ", "") == cleanQuery.replace(" ", "")
                        )
    }
}