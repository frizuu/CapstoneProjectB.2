package com.example.capstone_frontend.screen

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.capstone_frontend.component.AppColor
import com.example.capstone_frontend.data.DummyRepository
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onFinish: () -> Unit
) {
    LaunchedEffect(Unit) {
        delay(900)
        onFinish()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColor.Primary)
            .padding(28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(84.dp)
                .background(
                    color = Color(0x22FFFFFF),
                    shape = RoundedCornerShape(28.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "QR",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "QRIS Payment",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Digital Transaction Service",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFFFFE2E8),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun LandingScreen(
    onUserLoginClick: () -> Unit,
    onAdminLoginClick: () -> Unit,
    onQrisClick: () -> Unit,
    onStatusClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onHelpClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColor.Background)
            .padding(horizontal = 22.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(44.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(
                            color = AppColor.Primary,
                            shape = RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "QR",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Column(
                    modifier = Modifier.padding(start = 12.dp)
                ) {
                    Text(
                        text = "QRIS Payment",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AppColor.TextDark
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "Digital Transaction Service",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColor.TextGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(38.dp))

            Text(
                text = "Transaksi digital lebih mudah dan aman",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = AppColor.TextDark
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Aplikasi ini digunakan untuk simulasi pembayaran QRIS, transaksi biasa, pengecekan saldo, status transaksi, dan riwayat transaksi.",
                style = MaterialTheme.typography.bodyMedium,
                color = AppColor.TextGray
            )

            Spacer(modifier = Modifier.height(28.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(30.dp),
                colors = CardDefaults.cardColors(containerColor = AppColor.Primary),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "Fitur Utama",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ServiceInfoItem(
                            title = "QRIS",
                            description = "Bayar QR",
                            modifier = Modifier.weight(1f)
                        )

                        ServiceInfoItem(
                            title = "Transfer",
                            description = "Transaksi",
                            modifier = Modifier.weight(1f)
                        )

                        ServiceInfoItem(
                            title = "Saldo",
                            description = "Cek saldo",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Pilih Akses Masuk",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = AppColor.TextDark
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Gunakan akses sesuai peran pada simulasi sistem.",
                style = MaterialTheme.typography.bodyMedium,
                color = AppColor.TextGray
            )

            Spacer(modifier = Modifier.height(16.dp))

            AccessOptionCard(
                title = "Nasabah",
                description = "Masuk untuk melakukan pembayaran QRIS, transaksi biasa, melihat saldo, riwayat, dan status transaksi.",
                iconText = "N",
                buttonText = "Masuk sebagai Nasabah",
                isPrimary = true,
                onClick = onUserLoginClick
            )

            Spacer(modifier = Modifier.height(14.dp))

            AccessOptionCard(
                title = "Admin / Merchant",
                description = "Masuk untuk memantau saldo merchant, daftar merchant, dan transaksi dari baseline backend.",
                iconText = "M",
                buttonText = "Masuk sebagai Admin / Merchant",
                isPrimary = false,
                onClick = onAdminLoginClick
            )

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
fun ServiceInfoItem(
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(82.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0x22FFFFFF)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFFFE2E8),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun AccessOptionCard(
    title: String,
    description: String,
    iconText: String,
    buttonText: String,
    isPrimary: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = Color(0xFFFFEEF4),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = iconText,
                        color = AppColor.Primary,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 14.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AppColor.TextDark
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColor.TextGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isPrimary) {
                Button(
                    onClick = onClick,
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
                        text = buttonText,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                OutlinedButton(
                    onClick = onClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = buttonText,
                        color = AppColor.Primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun UserLoginScreen(
    onLoginSuccess: () -> Unit,
    onBack: () -> Unit
) {
    var mode by remember {
        mutableStateOf("choice")
    }

    var fullName by remember {
        mutableStateOf("")
    }

    var username by remember {
        mutableStateOf("")
    }

    var password by remember {
        mutableStateOf("")
    }

    var errorMessage by remember {
        mutableStateOf("")
    }

    val registeredUser = DummyRepository.getRegisteredUser()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColor.Background)
            .padding(22.dp)
    ) {
        item {
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

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = when (mode) {
                    "register" -> "Buat Akun Nasabah"
                    "login" -> "Masuk Nasabah"
                    else -> "Selamat Datang"
                },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = AppColor.TextDark
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = when (mode) {
                    "register" -> "Lengkapi data akun untuk menggunakan layanan."
                    "login" -> "Masukkan password untuk masuk ke akun Anda."
                    else -> "Pilih apakah Anda sudah memiliki akun atau ingin membuat akun baru."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = AppColor.TextGray
            )

            Spacer(modifier = Modifier.height(26.dp))

            when (mode) {
                "choice" -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(26.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Text(
                                text = "Sudah memiliki akun?",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = AppColor.TextDark
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Jika sudah memiliki akun, lanjutkan dengan password. Jika belum, buat akun baru terlebih dahulu.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = AppColor.TextGray
                            )

                            Spacer(modifier = Modifier.height(22.dp))

                            Button(
                                onClick = {
                                    mode = "login"
                                    username = registeredUser.username
                                    password = ""
                                    errorMessage = ""
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
                                    text = "Sudah Punya Akun",
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedButton(
                                onClick = {
                                    mode = "register"
                                    fullName = ""
                                    username = ""
                                    password = ""
                                    errorMessage = ""
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(22.dp)
                            ) {
                                Text(
                                    text = "Buat Akun Baru",
                                    color = AppColor.Primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                "login" -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(26.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Text(
                                text = registeredUser.fullName,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = AppColor.TextDark
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Username: ${registeredUser.username}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = AppColor.TextGray
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            OutlinedTextField(
                                value = password,
                                onValueChange = {
                                    password = it
                                    errorMessage = ""
                                },
                                label = {
                                    Text("Password")
                                },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                visualTransformation = PasswordVisualTransformation()
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

                            Button(
                                onClick = {
                                    val success = DummyRepository.loginUser(
                                        username = registeredUser.username,
                                        password = password
                                    )

                                    if (success) {
                                        onLoginSuccess()
                                    } else {
                                        errorMessage = "Password salah. Gunakan user123 untuk akun demo."
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
                                    text = "Masuk",
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            TextButton(
                                onClick = {
                                    mode = "register"
                                    fullName = ""
                                    username = ""
                                    password = ""
                                    errorMessage = ""
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Gunakan akun lain / buat akun baru",
                                    color = AppColor.Primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Text(
                                text = "Akun demo: user • Password: user123",
                                style = MaterialTheme.typography.bodySmall,
                                color = AppColor.TextGray
                            )
                        }
                    }
                }

                "register" -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(26.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            OutlinedTextField(
                                value = fullName,
                                onValueChange = {
                                    fullName = it
                                    errorMessage = ""
                                },
                                label = {
                                    Text("Nama Lengkap")
                                },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            OutlinedTextField(
                                value = username,
                                onValueChange = {
                                    username = it.lowercase().replace(" ", "")
                                    errorMessage = ""
                                },
                                label = {
                                    Text("Username")
                                },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            OutlinedTextField(
                                value = password,
                                onValueChange = {
                                    password = it
                                    errorMessage = ""
                                },
                                label = {
                                    Text("Password")
                                },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                visualTransformation = PasswordVisualTransformation()
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

                            Button(
                                onClick = {
                                    when {
                                        fullName.isBlank() -> {
                                            errorMessage = "Nama lengkap wajib diisi."
                                        }

                                        username.isBlank() -> {
                                            errorMessage = "Username wajib diisi."
                                        }

                                        password.length < 6 -> {
                                            errorMessage = "Password minimal 6 karakter."
                                        }

                                        else -> {
                                            DummyRepository.registerUser(
                                                fullName = fullName,
                                                username = username,
                                                password = password
                                            )
                                            onLoginSuccess()
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
                                    text = "Buat Akun",
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            TextButton(
                                onClick = {
                                    mode = "login"
                                    password = ""
                                    errorMessage = ""
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Sudah punya akun? Masuk",
                                    color = AppColor.Primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminLoginScreen(
    onLoginSuccess: () -> Unit,
    onBack: () -> Unit
) {
    var username by remember {
        mutableStateOf("")
    }

    var password by remember {
        mutableStateOf("")
    }

    var errorMessage by remember {
        mutableStateOf("")
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColor.Background)
            .padding(22.dp)
    ) {
        item {
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

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Masuk Admin / Merchant",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = AppColor.TextDark
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Akses dashboard untuk memantau saldo merchant dan transaksi dari baseline backend.",
                style = MaterialTheme.typography.bodyMedium,
                color = AppColor.TextGray
            )

            Spacer(modifier = Modifier.height(26.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    OutlinedTextField(
                        value = username,
                        onValueChange = {
                            username = it
                            errorMessage = ""
                        },
                        label = {
                            Text("Username Admin")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            errorMessage = ""
                        },
                        label = {
                            Text("Password")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation()
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

                    Button(
                        onClick = {
                            if (username.isBlank() || password.isBlank()) {
                                errorMessage = "Username admin dan password wajib diisi."
                            } else {
                                onLoginSuccess()
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
                            text = "Masuk Dashboard",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Demo: isi username admin dan password apa saja untuk masuk.",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColor.TextGray
                    )
                }
            }
        }
    }
}