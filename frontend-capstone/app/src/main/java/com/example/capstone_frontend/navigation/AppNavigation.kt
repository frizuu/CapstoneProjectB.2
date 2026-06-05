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
import com.example.capstone_frontend.screen.AdminDashboardScreenV2
import com.example.capstone_frontend.screen.AdminLoginScreenV2
import com.example.capstone_frontend.screen.HelpScreen
import com.example.capstone_frontend.screen.LandingScreen
import com.example.capstone_frontend.screen.NotificationScreen
import com.example.capstone_frontend.screen.PaymentResultV2Screen
import com.example.capstone_frontend.screen.QrScannerScreen
import com.example.capstone_frontend.screen.QrisPaymentScreen
import com.example.capstone_frontend.screen.RegularTransactionScreen
import com.example.capstone_frontend.screen.SplashScreen
import com.example.capstone_frontend.screen.TransactionStatusScreen
import com.example.capstone_frontend.screen.TransferDetailScreen
import com.example.capstone_frontend.screen.TransferHistoryScreen
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
                onNotificationClick = {
                    navController.navigate("notifications")
                },
                onHelpClick = {
                    navController.navigate("status")
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
                            val currentUserId = DummyRepository.getCurrentUserId()
                            val merchantCode = DummyRepository.getCurrentMerchantCode()
                            val referenceNo = DummyRepository.buildIdempotencyKey()

                            val paymentResponse = RetrofitClient.api.qrisPayment(
                                idempotencyKey = referenceNo,
                                request = QrisPaymentRequest(
                                    userId = currentUserId,
                                    merchantCode = merchantCode,
                                    amount = amount,
                                    referenceNo = referenceNo
                                )
                            )

                            when (paymentResponse.status) {
                                "SUCCESS" -> {
                                    try {
                                        val merchantResponse = RetrofitClient.api.getMerchants()
                                        DummyRepository.setMerchantCache(merchantResponse.merchants)
                                    } catch (_: Exception) {
                                    }

                                    var transactionIdForResult =
                                        paymentResponse.transactionId?.let {
                                            "TRX-${it.toString().padStart(3, '0')}"
                                        } ?: "TRX-TERBARU"

                                    try {
                                        val latestTransactions =
                                            RetrofitClient.api.getTransactions(currentUserId)

                                        val convertedTransactions =
                                            DummyRepository.convertBackendTransactions(latestTransactions)

                                        DummyRepository.setBackendTransactions(convertedTransactions)

                                        val latestTransaction = DummyRepository.getTransactions()
                                            .firstOrNull {
                                                it.amount == amount &&
                                                        it.status == "SUCCESS" &&
                                                        it.transactionType.uppercase() == "QRIS"
                                            }
                                            ?: DummyRepository.getTransactions().firstOrNull()

                                        if (latestTransaction != null) {
                                            transactionIdForResult = latestTransaction.transactionId
                                        }
                                    } catch (_: Exception) {
                                    }

                                    showToast("Pembayaran QRIS berhasil")

                                    navController.navigate(
                                        "payment_result/$transactionIdForResult"
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
                                    showToast("Pembayaran QRIS gagal: ${paymentResponse.message ?: paymentResponse.status}")
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
                onSubmitTransfer = { recipientUserId, amount, _ ->
                    scope.launch {
                        try {
                            val currentUserId = DummyRepository.getCurrentUserId()
                            val referenceNo = "TRF-${System.currentTimeMillis()}"

                            val paymentResponse = RetrofitClient.api.createPayment(
                                request = PaymentRequest(
                                    userId = currentUserId,
                                    recipientUserId = recipientUserId,
                                    amount = amount,
                                    referenceNo = referenceNo
                                )
                            )

                            when (paymentResponse.status) {
                                "SUCCESS" -> {
                                    var transactionIdForResult =
                                        paymentResponse.transactionId?.let {
                                            "TRX-${it.toString().padStart(3, '0')}"
                                        } ?: "TRX-TERBARU"

                                    try {
                                        val latestTransactions =
                                            RetrofitClient.api.getTransactions(currentUserId)

                                        val convertedTransactions =
                                            DummyRepository.convertBackendTransactions(latestTransactions)

                                        DummyRepository.setBackendTransactions(convertedTransactions)

                                        val latestTransaction = DummyRepository.getTransactions()
                                            .firstOrNull {
                                                it.amount == amount &&
                                                        it.status == "SUCCESS" &&
                                                        it.recipientUserId == recipientUserId
                                            }
                                            ?: DummyRepository.getTransactions().firstOrNull()

                                        if (latestTransaction != null) {
                                            transactionIdForResult = latestTransaction.transactionId
                                        }
                                    } catch (_: Exception) {
                                    }

                                    showToast("Transfer berhasil")

                                    navController.navigate(
                                        "payment_result/$transactionIdForResult"
                                    ) {
                                        popUpTo("regular_transaction") {
                                            inclusive = true
                                        }
                                    }
                                }

                                "INSUFFICIENT_BALANCE" -> {
                                    showToast("Saldo tidak mencukupi.")
                                }

                                "USER_NOT_FOUND", "INVALID_INPUT" -> {
                                    showToast(paymentResponse.message ?: "User penerima tidak ditemukan.")
                                }

                                "TIMEOUT", "SYSTEM_BUSY" -> {
                                    showToast(paymentResponse.message ?: "Sistem sedang sibuk. Silakan coba lagi.")
                                }

                                else -> {
                                    showToast("Transfer gagal: ${paymentResponse.message ?: paymentResponse.status}")
                                }
                            }
                        } catch (e: Exception) {
                            showToast("Transfer gagal: ${e.message ?: "koneksi backend bermasalah"}")
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

            PaymentResultV2Screen(
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
            TransferHistoryScreen(
                onBack = {
                    navController.popBackStack()
                },
                onTransactionClick = { transactionId ->
                    navController.navigate("transaction_detail/$transactionId")
                }
            )
        }

        composable("notifications") {
            NotificationScreen(
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

            TransferDetailScreen(
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
            AdminLoginScreenV2(
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
            AdminDashboardScreenV2(
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