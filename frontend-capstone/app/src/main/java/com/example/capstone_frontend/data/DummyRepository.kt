package com.example.capstone_frontend.data

import com.example.capstone_frontend.model.MetricData
import com.example.capstone_frontend.model.TransactionData
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

object DummyRepository {

    data class UserAccount(
        val fullName: String,
        val username: String,
        val password: String
    )

    private val defaultUser = UserAccount(
        fullName = "Fariz Ubaidillah",
        username = "user",
        password = "user123"
    )

    private var registeredUser = defaultUser
    private var currentUser = registeredUser

    private var lastScannedQrString: String = "NMID877996734914"

    private var currentMerchantId: Int = 1
    private var currentMerchantName: String = "Rumah Makan Bu Putri"
    private var currentMerchantCode: String = "NMID877996734914"
    private var currentMerchantCategory: String = "UMKM"

    private val backendTransactionList = mutableListOf<TransactionData>()

    private val merchantNameById = mutableMapOf<Int, String>()
    private val merchantCodeById = mutableMapOf<Int, String>()
    private val merchantCategoryById = mutableMapOf<Int, String>()

    fun getRegisteredUser(): UserAccount {
        return registeredUser
    }

    fun getCurrentUserName(): String {
        return currentUser.fullName
    }

    fun getCurrentUserFirstName(): String {
        return currentUser.fullName
            .trim()
            .split(" ")
            .firstOrNull()
            ?.lowercase()
            ?: "nasabah"
    }

    fun registerUser(
        fullName: String,
        username: String,
        password: String
    ) {
        registeredUser = UserAccount(
            fullName = fullName.trim(),
            username = username.trim().lowercase(),
            password = password
        )

        currentUser = registeredUser
    }

    fun loginUser(
        username: String,
        password: String
    ): Boolean {
        val isValid = username.trim().lowercase() == registeredUser.username &&
                password == registeredUser.password

        if (isValid) {
            currentUser = registeredUser
        }

        return isValid
    }

    fun logoutUser() {
        currentUser = registeredUser
    }

    fun deleteCurrentAccount() {
        registeredUser = defaultUser
        currentUser = registeredUser
    }

    fun setScannedQrString(value: String) {
        lastScannedQrString = value
            .trim()
            .replace("\"", "")
            .replace(" ", "")

        currentMerchantCode = lastScannedQrString
    }

    fun getScannedQrString(): String {
        return lastScannedQrString
    }

    fun setCurrentMerchant(
        merchantId: Int,
        merchantName: String,
        merchantCode: String,
        category: String
    ) {
        currentMerchantId = merchantId
        currentMerchantName = merchantName.ifBlank {
            "Merchant ID $merchantId"
        }
        currentMerchantCode = merchantCode.ifBlank {
            lastScannedQrString
        }
        currentMerchantCategory = category.ifBlank {
            "REGULAR"
        }

        merchantNameById[merchantId] = currentMerchantName
        merchantCodeById[merchantId] = currentMerchantCode
        merchantCategoryById[merchantId] = currentMerchantCategory

        lastScannedQrString = currentMerchantCode
    }

    fun setMerchantCache(merchants: List<MerchantDto>) {
        merchants.forEach { merchant ->
            merchantNameById[merchant.id] = merchant.name
            merchantCodeById[merchant.id] = merchant.merchantCode
            merchantCategoryById[merchant.id] = merchant.category
        }
    }

    fun getCurrentMerchantId(): Int {
        return currentMerchantId
    }

    fun getCurrentMerchantName(): String {
        return currentMerchantName
    }

    fun getCurrentMerchantCode(): String {
        return currentMerchantCode
    }

    fun getCurrentMerchantCategory(): String {
        return currentMerchantCategory
    }

    fun buildIdempotencyKey(): String {
        return "QRIS-${System.currentTimeMillis()}"
    }

    fun setBackendTransactions(transactions: List<TransactionData>) {
        backendTransactionList.clear()
        backendTransactionList.addAll(transactions)
    }

    fun setTransactions(transactions: List<TransactionData>) {
        setBackendTransactions(transactions)
    }

    fun getTransactions(): List<TransactionData> {
        return backendTransactionList.sortedByDescending {
            parseDateMillis(it.createdAt)
        }
    }

    fun getTransactionById(transactionId: String): TransactionData? {
        return getTransactions().find {
            it.transactionId == transactionId
        }
    }

    fun convertBackendTransactions(
        backendTransactions: List<BackendTransactionResponse>
    ): List<TransactionData> {
        return backendTransactions.map { item ->
            convertBackendTransaction(item)
        }
    }

    fun convertBackendTransaction(
        item: BackendTransactionResponse
    ): TransactionData {
        val normalizedType = normalizeTransactionType(
            transactionType = item.transactionType,
            merchantId = item.merchantId
        )

        val displayName = when (normalizedType) {
            "QRIS" -> {
                item.merchantName
                    ?.takeIf { it.isNotBlank() }
                    ?: item.merchantId?.let { merchantId ->
                        merchantNameById[merchantId] ?: "Merchant ID $merchantId"
                    }
                    ?: "Merchant QRIS"
            }

            "PAYMENT" -> {
                "Transaksi Biasa"
            }

            else -> {
                "Transaksi"
            }
        }

        return TransactionData(
            transactionId = formatTransactionId(item.id),
            userId = item.userId.toString(),
            merchantId = item.merchantId,
            merchantName = displayName,
            amount = item.amount,
            status = normalizeBackendStatus(item.status),
            transactionType = normalizedType,
            createdAt = convertBackendTimeToWib(item.createdAt),
            latencyMs = 0,
            cacheState = "Database Baseline",
            networkProfile = "Normal",
            idempotencyKey = item.idempotencyKey ?: "-"
        )
    }

    fun findTransactionFromBackendList(
        backendTransactions: List<BackendTransactionResponse>,
        transactionId: Int
    ): TransactionData? {
        val converted = convertBackendTransactions(backendTransactions)
        setBackendTransactions(converted)

        return converted.find {
            it.transactionId == formatTransactionId(transactionId)
        }
    }

    fun getMetrics(): MetricData {
        val transactions = getTransactions()

        val total = transactions.size

        val success = transactions.count {
            it.status == "SUCCESS"
        }

        val timeout = transactions.count {
            it.status == "TIMEOUT"
        }

        return MetricData(
            totalTransaction = total,
            successRate = if (total > 0) {
                (success * 100) / total
            } else {
                0
            },
            averageLatency = 0,
            timeoutCount = timeout,
            cacheHitRate = 0
        )
    }

    private fun normalizeTransactionType(
        transactionType: String?,
        merchantId: Int?
    ): String {
        val type = transactionType
            ?.trim()
            ?.uppercase()
            .orEmpty()

        return when {
            type == "QRIS" -> "QRIS"
            type == "PAYMENT" -> "PAYMENT"
            merchantId != null -> "QRIS"
            else -> "PAYMENT"
        }
    }

    private fun normalizeBackendStatus(status: String): String {
        return when (status.uppercase()) {
            "SUCCESS" -> "SUCCESS"
            "FAILED" -> "FAILED"
            "TIMEOUT" -> "TIMEOUT"
            "SYSTEM_BUSY" -> "TIMEOUT"
            "INVALID_INPUT" -> "FAILED"
            "INSUFFICIENT_BALANCE" -> "FAILED"
            "USER_NOT_FOUND" -> "FAILED"
            "MERCHANT_NOT_FOUND" -> "FAILED"
            "FAILED_START_TRANSACTION" -> "FAILED"
            "FAILED_DEBIT_USER" -> "FAILED"
            "FAILED_CREDIT_MERCHANT" -> "FAILED"
            "FAILED_SAVE_TRANSACTION" -> "FAILED"
            "FAILED_COMMIT" -> "FAILED"
            else -> "FAILED"
        }
    }

    private fun formatTransactionId(id: Int): String {
        return "TRX-${id.toString().padStart(3, '0')}"
    }

    private fun convertBackendTimeToWib(rawDate: String): String {
        val date = parseBackendDateAsUtc(rawDate)

        if (date == null) {
            return rawDate
        }

        val formatter = SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss",
            Locale.US
        )

        formatter.timeZone = TimeZone.getTimeZone("Asia/Jakarta")

        return formatter.format(date)
    }

    private fun parseBackendDateAsUtc(rawDate: String): java.util.Date? {
        val cleanDate = rawDate.trim()

        val possibleFormats = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSSSSSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd HH:mm"
        )

        for (format in possibleFormats) {
            try {
                val parser = SimpleDateFormat(format, Locale.US)

                parser.timeZone = TimeZone.getTimeZone("UTC")

                val date = parser.parse(cleanDate)

                if (date != null) {
                    return date
                }
            } catch (_: Exception) {
            }
        }

        return null
    }

    private fun parseDateMillis(rawDate: String): Long {
        val possibleFormats = listOf(
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd HH:mm",
            "yyyy-MM-dd'T'HH:mm:ss.SSSSSSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss'Z'"
        )

        for (format in possibleFormats) {
            try {
                val parser = SimpleDateFormat(format, Locale.US)
                parser.timeZone = TimeZone.getTimeZone("Asia/Jakarta")

                val date = parser.parse(rawDate)

                if (date != null) {
                    return date.time
                }
            } catch (_: Exception) {
            }
        }

        return 0L
    }
}