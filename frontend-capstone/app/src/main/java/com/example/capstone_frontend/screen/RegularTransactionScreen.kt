package com.example.capstone_frontend.screen

import androidx.compose.foundation.background
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
import com.example.capstone_frontend.data.RetrofitClient

@Composable
fun RegularTransactionScreen(
    onBack: () -> Unit,
    onSubmitTransaction: (Int, String) -> Unit
) {
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

    val amount = amountText.toIntOrNull() ?: 0

    LaunchedEffect(Unit) {
        try {
            isLoadingBalance = true
            val response = RetrofitClient.api.getBalance(1)
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
                        text = "Transaksi Biasa",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = AppColor.TextDark
                    )

                    Text(
                        text = "Masukkan nominal transaksi sesuai baseline system.",
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
                        text = "Nominal Transaksi",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AppColor.TextDark
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Data yang dikirim ke backend hanya user_id dan amount, sesuai endpoint /payment pada baseline system.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColor.TextGray
                    )

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
                            Text("Nominal")
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
                                text = "Ringkasan",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = AppColor.TextDark
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            InfoRow("Jenis Transaksi", "Transaksi Biasa")
                            InfoRow("User ID", "1")
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
                                amount <= 0 -> {
                                    errorMessage = "Nominal transaksi wajib diisi."
                                }

                                amount < 1000 -> {
                                    errorMessage = "Nominal minimal Rp1.000."
                                }

                                balance > 0 && amount > balance -> {
                                    errorMessage = "Saldo tidak mencukupi."
                                }

                                else -> {
                                    onSubmitTransaction(amount, "Transaksi Biasa")
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
                            text = "Proses Transaksi",
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