package com.example.capstone_frontend.navigation

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.capstone_frontend.data.DummyRepository
import com.example.capstone_frontend.data.PaymentRequest
import com.example.capstone_frontend.data.QrisPaymentRequest
import com.example.capstone_frontend.data.RetrofitClient
import com.example.capstone_frontend.screen.AdminDashboardScreen
import com.example.capstone_frontend.screen.AdminLoginScreen
import com.example.capstone_frontend.screen.HelpScreen
import com.example.capstone_frontend.screen.LandingScreen
import com.example.capstone_frontend.screen.PaymentResultScreen
import com.example.capstone_frontend.screen.QrScannerScreen
import com.example.capstone_frontend.screen.QrisPaymentScreen
import com.example.capstone_frontend.screen.RegularTransactionScreen
import com.example.capstone_frontend.screen.SplashScreen
import com.example.capstone_frontend.screen.TransactionDetailScreen
import com.example.capstone_frontend.screen.TransactionHistoryScreen
import com.example.capstone_frontend.screen.TransactionStatusScreen
import com.example.capstone_frontend.screen.UserHomeScreen
import com.example.capstone_frontend.screen.UserLoginScreen
import kotlinx.coroutines.launch

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    fun showToast(message: String) {
        Toast.makeText(
            context,
            message,
            Toast.LENGTH_LONG
        ).show()
    }

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        composable("splash") {
            SplashScreen(
                onFinish = {
                    navController.navigate("landing") {
                        popUpTo("splash") {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable("landing") {
            LandingScreen(
                onUserLoginClick = {
                    navController.navigate("user_login")
                },
                onAdminLoginClick = {
                    navController.navigate("admin_login")
                },
                onQrisClick = {
                    navController.navigate("user_login")
                },
                onStatusClick = {
                    navController.navigate("user_login")
                },
                onHistoryClick = {
                    navController.navigate("user_login")
                },
                onHelpClick = {
                    navController.navigate("help")
                }
            )
        }

        composable("user_login") {
            UserLoginScreen(
                onLoginSuccess = {
                    navController.navigate("user_home") {
                        popUpTo("landing") {
                            inclusive = false
                        }
                    }
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("user_home") {
            UserHomeScreen(
                onPayQrisClick = {
                    navController.navigate("qr_scanner")
                },
                onRegularTransactionClick = {
                    navController.navigate("regular_transaction")
                },
                onHistoryClick = {
                    navController.navigate("history")
                },
                onStatusClick = {
                    navController.navigate("status")
                },
                onHelpClick = {
                    navController.navigate("help")
                },
                onLogout = {
                    navController.navigate("landing") {
                        popUpTo("user_home") {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable("qr_scanner") {
            QrScannerScreen(
                onBack = {
                    navController.popBackStack()
                },
                onQrDetected = { qrString ->
                    scope.launch {
                        try {
                            DummyRepository.setScannedQrString(qrString)

                            val merchantCode = DummyRepository.getScannedQrString()

                            val inquiryResponse = RetrofitClient.api.inquiryQris(
                                merchantCode = merchantCode
                            )

                            DummyRepository.setCurrentMerchant(
                                merchantId = inquiryResponse.merchantId,
                                merchantName = inquiryResponse.merchantName,
                                merchantCode = inquiryResponse.merchantCode,
                                category = inquiryResponse.category
                            )

                            showToast("Merchant ditemukan: ${inquiryResponse.merchantName}")

                            navController.navigate("qris_payment") {
                                popUpTo("qr_scanner") {
                                    inclusive = true
                                }
                            }
                        } catch (e: Exception) {
                            showToast("QR tidak valid atau merchant tidak ditemukan di baseline.")
                        }
                    }
                }
            )
        }

        composable("qris_payment") {
            QrisPaymentScreen(
                onBack = {
                    navController.navigate("qr_scanner") {
                        popUpTo("qris_payment") {
                            inclusive = true
                        }
                    }
                },
                onPaymentSubmit = { amount, _ ->
                    scope.launch {
                        try {
                            val merchantCode = DummyRepository.getCurrentMerchantCode()

                            val paymentResponse = RetrofitClient.api.qrisPayment(
                                idempotencyKey = DummyRepository.buildIdempotencyKey(),
                                request = QrisPaymentRequest(
                                    userId = 1,
                                    merchantCode = merchantCode,
                                    amount = amount
                                )
                            )

                            when (paymentResponse.status) {
                                "SUCCESS" -> {
                                    try {
                                        val merchantResponse = RetrofitClient.api.getMerchants()
                                        DummyRepository.setMerchantCache(merchantResponse.merchants)
                                    } catch (_: Exception) {
                                    }

                                    val latestTransactions = RetrofitClient.api.getTransactions(1)
                                    val convertedTransactions =
                                        DummyRepository.convertBackendTransactions(latestTransactions)

                                    DummyRepository.setBackendTransactions(convertedTransactions)

                                    val latestTransaction = DummyRepository.getTransactions()
                                        .firstOrNull {
                                            it.amount == amount && it.status == "SUCCESS"
                                        }
                                        ?: DummyRepository.getTransactions().firstOrNull()

                                    showToast("Pembayaran QRIS berhasil")

                                    navController.navigate(
                                        "payment_result/${latestTransaction?.transactionId ?: "TRX-TERBARU"}"
                                    ) {
                                        popUpTo("qris_payment") {
                                            inclusive = true
                                        }
                                    }
                                }

                                "INSUFFICIENT_BALANCE" -> {
                                    showToast("Saldo tidak mencukupi.")
                                }

                                "MERCHANT_NOT_FOUND" -> {
                                    showToast("Merchant tidak ditemukan.")
                                }

                                "TIMEOUT", "SYSTEM_BUSY" -> {
                                    showToast("Sistem sedang sibuk. Silakan coba lagi.")
                                }

                                else -> {
                                    showToast("Pembayaran QRIS gagal: ${paymentResponse.status}")
                                }
                            }
                        } catch (e: Exception) {
                            showToast("Pembayaran gagal. Pastikan backend baseline berjalan.")
                        }
                    }
                }
            )
        }

        composable("regular_transaction") {
            RegularTransactionScreen(
                onBack = {
                    navController.popBackStack()
                },
                onSubmitTransaction = { amount, _ ->
                    scope.launch {
                        try {
                            val paymentResponse = RetrofitClient.api.createPayment(
                                request = PaymentRequest(
                                    userId = 1,
                                    amount = amount
                                )
                            )

                            when (paymentResponse.status) {
                                "SUCCESS" -> {
                                    val latestTransactions = RetrofitClient.api.getTransactions(1)
                                    val convertedTransactions =
                                        DummyRepository.convertBackendTransactions(latestTransactions)

                                    DummyRepository.setBackendTransactions(convertedTransactions)

                                    val latestTransaction = DummyRepository.getTransactions()
                                        .firstOrNull {
                                            it.amount == amount && it.status == "SUCCESS"
                                        }
                                        ?: DummyRepository.getTransactions().firstOrNull()

                                    showToast("Transaksi berhasil")

                                    navController.navigate(
                                        "payment_result/${latestTransaction?.transactionId ?: "TRX-TERBARU"}"
                                    ) {
                                        popUpTo("regular_transaction") {
                                            inclusive = true
                                        }
                                    }
                                }

                                "INSUFFICIENT_BALANCE" -> {
                                    showToast("Saldo tidak mencukupi.")
                                }

                                "USER_NOT_FOUND" -> {
                                    showToast("User tidak ditemukan.")
                                }

                                "TIMEOUT", "SYSTEM_BUSY" -> {
                                    showToast("Sistem sedang sibuk. Silakan coba lagi.")
                                }

                                else -> {
                                    showToast("Transaksi gagal: ${paymentResponse.status}")
                                }
                            }
                        } catch (e: Exception) {
                            showToast("Transaksi gagal. Pastikan backend baseline berjalan.")
                        }
                    }
                }
            )
        }

        composable(
            route = "payment_result/{transactionId}",
            arguments = listOf(
                navArgument("transactionId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val transactionId = backStackEntry.arguments
                ?.getString("transactionId")
                .orEmpty()

            PaymentResultScreen(
                transactionId = transactionId,
                onBackHome = {
                    navController.navigate("user_home") {
                        popUpTo("user_home") {
                            inclusive = true
                        }
                    }
                },
                onHistoryClick = {
                    navController.navigate("history")
                }
            )
        }

        composable("history") {
            TransactionHistoryScreen(
                onBack = {
                    navController.popBackStack()
                },
                onTransactionClick = { transactionId ->
                    navController.navigate("transaction_detail/$transactionId")
                }
            )
        }

        composable(
            route = "transaction_detail/{transactionId}",
            arguments = listOf(
                navArgument("transactionId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val transactionId = backStackEntry.arguments
                ?.getString("transactionId")
                .orEmpty()

            TransactionDetailScreen(
                transactionId = transactionId,
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("status") {
            TransactionStatusScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("help") {
            HelpScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("admin_login") {
            AdminLoginScreen(
                onLoginSuccess = {
                    navController.navigate("admin_dashboard") {
                        popUpTo("landing") {
                            inclusive = false
                        }
                    }
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("admin_dashboard") {
            AdminDashboardScreen(
                onLogout = {
                    navController.navigate("landing") {
                        popUpTo("admin_dashboard") {
                            inclusive = true
                        }
                    }
                }
            )
        }
    }
}