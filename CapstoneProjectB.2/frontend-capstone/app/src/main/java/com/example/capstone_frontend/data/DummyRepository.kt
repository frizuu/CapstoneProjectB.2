package com.example.capstone_frontend.data

import com.example.capstone_frontend.model.MetricData
import com.example.capstone_frontend.model.TransactionData
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

object DummyRepository {

    data class BaselineUser(
        val id: Int,
        val name: String,
        val initialBalance: Int
    )

    data class UserAccount(
        val fullName: String,
        val username: String,
        val password: String
    )

    private val baselineUsers = listOf(
        BaselineUser(1, "Fariz", 40000),
        BaselineUser(2, "User2", 30000),
        BaselineUser(3, "Lestari Rahayu", 1770000),
        BaselineUser(4, "Eko Purba", 8370000),
        BaselineUser(5, "Lukman Hidayat", 5380000),
        BaselineUser(6, "Budi Panjaitan", 7930000),
        BaselineUser(7, "Fira Permana", 9190000),
        BaselineUser(8, "Eka Kurniawan", 4250000),
        BaselineUser(9, "Putri Lubis", 8750000),
        BaselineUser(10, "Bagas Hidayat", 4360000),
        BaselineUser(11, "Bella Wijaya", 6790000),
        BaselineUser(12, "Rudi Panjaitan", 2860000),
        BaselineUser(13, "Yogi Panjaitan", 6850000),
        BaselineUser(14, "Yogi Simbolon", 2430000),
        BaselineUser(15, "Zahra Harahap", 6130000),
        BaselineUser(16, "Ulfa Panjaitan", 5510000),
        BaselineUser(17, "Sari Wijaya", 8100000),
        BaselineUser(18, "Fitri Nasution", 8170000),
        BaselineUser(19, "Agus Purba", 5790000),
        BaselineUser(20, "Hesty Purba", 590000),
        BaselineUser(21, "Ahmad Nasution", 8610000),
        BaselineUser(22, "Putri Nugroho", 9790000),
        BaselineUser(23, "Devi Pratama", 2790000),
        BaselineUser(24, "Eka Susanto", 8160000),
        BaselineUser(25, "Hani Lubis", 9530000),
        BaselineUser(26, "Fani Harahap", 3250000),
        BaselineUser(27, "Devi Saputra", 5150000),
        BaselineUser(28, "Hendra Pratama", 5640000),
        BaselineUser(29, "Citra Saputra", 1800000),
        BaselineUser(30, "Fira Nugroho", 6270000),
        BaselineUser(31, "Dewi Lubis", 6740000),
        BaselineUser(32, "Ahmad Kurniawan", 9290000),
        BaselineUser(33, "Yogi Maharani", 1660000),
        BaselineUser(34, "Vina Pratama", 6590000),
        BaselineUser(35, "Fitri Lubis", 8860000),
        BaselineUser(36, "Ahmad Simbolon", 2440000),
        BaselineUser(37, "Rina Nugroho", 2980000),
        BaselineUser(38, "Citra Purba", 4730000),
        BaselineUser(39, "Joko Wijaya", 1030000),
        BaselineUser(40, "Lukman Kurniawan", 680000),
        BaselineUser(41, "Citra Situmorang", 6970000),
        BaselineUser(42, "Rizky Hidayat", 1180000),
        BaselineUser(43, "Doni Rahayu", 5220000),
        BaselineUser(44, "Nanda Purnama", 7090000),
        BaselineUser(45, "Hesty Nasution", 7330000),
        BaselineUser(46, "Ogi Nasution", 8740000),
        BaselineUser(47, "Chandra Wijaya", 5180000),
        BaselineUser(48, "Chandra Panjaitan", 2780000),
        BaselineUser(49, "Joko Wibowo", 2550000),
        BaselineUser(50, "Rina Wibowo", 8200000),
        BaselineUser(51, "Jihan Hidayat", 7000000),
        BaselineUser(52, "Dewi Pratama", 5300000),
        BaselineUser(53, "Joko Permana", 6610000),
        BaselineUser(54, "Vina Permana", 6070000),
        BaselineUser(55, "Maya Rahayu", 350000),
        BaselineUser(56, "Taufik Kusuma", 2160000),
        BaselineUser(57, "Doni Purnama", 9770000),
        BaselineUser(58, "Siti Saputra", 7000000),
        BaselineUser(59, "Bagas Situmorang", 3060000),
        BaselineUser(60, "Dian Rahayu", 4590000),
        BaselineUser(61, "Ivan Panjaitan", 7300000),
        BaselineUser(62, "Wahyu Wibowo", 2740000),
        BaselineUser(63, "Fariz Rahayu", 2560000),
        BaselineUser(64, "Devi Hidayat", 430000),
        BaselineUser(65, "Gilang Nugroho", 6070000),
        BaselineUser(66, "Qori Harahap", 7580000),
        BaselineUser(67, "Rizky Panjaitan", 880000),
        BaselineUser(68, "Eka Wijaya", 9330000),
        BaselineUser(69, "Eka Simbolon", 8590000),
        BaselineUser(70, "Rizky Saputra", 8070000),
        BaselineUser(71, "Andi Nugroho", 5320000),
        BaselineUser(72, "Qori Santoso", 5620000),
        BaselineUser(73, "Ivan Purba", 1340000),
        BaselineUser(74, "Andi Maharani", 9300000),
        BaselineUser(75, "Fira Manurung", 3560000),
        BaselineUser(76, "Wawan Purnama", 4740000),
        BaselineUser(77, "Ayu Saputra", 4170000),
        BaselineUser(78, "Doni Lubis", 9800000),
        BaselineUser(79, "Rudi Nasution", 6180000),
        BaselineUser(80, "Irfan Situmorang", 2950000),
        BaselineUser(81, "Eko Santoso", 2260000),
        BaselineUser(82, "Rina Maharani", 7650000),
        BaselineUser(83, "Yogi Situmorang", 2110000),
        BaselineUser(84, "Fariz Panjaitan", 6160000),
        BaselineUser(85, "Rudi Harahap", 5550000),
        BaselineUser(86, "Qori Wibowo", 140000),
        BaselineUser(87, "Hani Situmorang", 8730000),
        BaselineUser(88, "Lestari Siregar", 8830000),
        BaselineUser(89, "Kartika Saputra", 9860000),
        BaselineUser(90, "Lestari Kusuma", 8120000),
        BaselineUser(91, "Nanda Wibowo", 1800000),
        BaselineUser(92, "Chandra Saputra", 6350000),
        BaselineUser(93, "Bella Setiawan", 970000),
        BaselineUser(94, "Nurul Situmorang", 9040000),
        BaselineUser(95, "Wahyu Situmorang", 8760000),
        BaselineUser(96, "Bella Nugroho", 7470000),
        BaselineUser(97, "Yogi Siregar", 6600000),
        BaselineUser(98, "Putri Purba", 5920000),
        BaselineUser(99, "Nanda Permana", 6540000),
        BaselineUser(100, "Lestari Harahap", 6580000)
    )

    private var currentUserId = 1

    private val defaultUser = UserAccount(
        fullName = "Fariz",
        username = "fariz",
        password = "user123"
    )

    private var registeredUser = defaultUser
    private var currentUser = defaultUser

    private var lastScannedQrString: String = "NMID877996734914"

    private var currentMerchantId: Int = 1
    private var currentMerchantName: String = "Rumah Makan Bu Putri"
    private var currentMerchantCode: String = "NMID877996734914"
    private var currentMerchantCategory: String = "UMKM"

    private val backendTransactionList = mutableListOf<TransactionData>()

    private val merchantNameById = mutableMapOf<Int, String>()
    private val merchantCodeById = mutableMapOf<Int, String>()
    private val merchantCategoryById = mutableMapOf<Int, String>()

    fun getBaselineUsers(): List<BaselineUser> {
        return baselineUsers
    }

    fun getCurrentUserId(): Int {
        return currentUserId
    }

    fun getCurrentBaselineUser(): BaselineUser {
        return baselineUsers.find {
            it.id == currentUserId
        } ?: baselineUsers.first()
    }

    fun loginBaselineUser(
        userId: Int,
        password: String
    ): Boolean {
        val selectedUser = baselineUsers.find {
            it.id == userId
        } ?: return false

        if (password != "user123") {
            return false
        }

        currentUserId = selectedUser.id

        currentUser = UserAccount(
            fullName = selectedUser.name,
            username = normalizeUsername(selectedUser.name),
            password = "user123"
        )

        registeredUser = currentUser
        backendTransactionList.clear()

        return true
    }

    fun loginUser(
        username: String,
        password: String
    ): Boolean {
        if (password != "user123") {
            return false
        }

        val cleanInput = username.trim().lowercase()

        val selectedUser = baselineUsers.find { user ->
            user.id.toString() == cleanInput ||
                    normalizeUsername(user.name) == cleanInput ||
                    user.name.lowercase() == cleanInput
        }

        return if (selectedUser != null) {
            loginBaselineUser(
                userId = selectedUser.id,
                password = password
            )
        } else {
            false
        }
    }

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
        val baselineUser = baselineUsers.find {
            it.name.equals(fullName.trim(), ignoreCase = true)
        }

        if (baselineUser != null) {
            currentUserId = baselineUser.id

            currentUser = UserAccount(
                fullName = baselineUser.name,
                username = normalizeUsername(baselineUser.name),
                password = "user123"
            )

            registeredUser = currentUser
        } else {
            currentUserId = 1
            currentUser = defaultUser
            registeredUser = defaultUser
        }

        backendTransactionList.clear()
    }

    fun logoutUser() {
        backendTransactionList.clear()
    }

    fun deleteCurrentAccount() {
        currentUserId = 1
        currentUser = defaultUser
        registeredUser = defaultUser
        backendTransactionList.clear()
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
        response: TransactionHistoryResponse
    ): List<TransactionData> {
        return convertBackendTransactions(response.transactions)
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
        val currentLoginUserId = getCurrentUserId()

        val normalizedType = normalizeTransactionType(
            transactionType = item.transactionType,
            merchantId = item.merchantId,
            recipientUserId = item.recipientUserId
        )

        val direction = item.direction
            ?.uppercase()
            ?: if (item.userId == currentLoginUserId) {
                "OUT"
            } else {
                "IN"
            }

        val displayName = when {
            normalizedType == "TRANSFER" && direction == "OUT" -> {
                item.recipientUserName
                    ?.takeIf { it.isNotBlank() }
                    ?: item.recipientUserId?.let { recipientId ->
                        getUserNameById(recipientId) ?: "User ID $recipientId"
                    }
                    ?: "Penerima Transfer"
            }

            normalizedType == "TRANSFER" && direction == "IN" -> {
                item.senderName
                    ?.takeIf { it.isNotBlank() }
                    ?: getUserNameById(item.userId)
                    ?: "Pengirim Transfer"
            }

            normalizedType == "QRIS" -> {
                item.merchantName
                    ?.takeIf { it.isNotBlank() }
                    ?: item.merchantId?.let { merchantId ->
                        merchantNameById[merchantId] ?: "Merchant ID $merchantId"
                    }
                    ?: "Merchant QRIS"
            }

            normalizedType == "PAYMENT" -> {
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
            idempotencyKey = item.idempotencyKey ?: "-",
            direction = direction,
            senderName = item.senderName ?: getUserNameById(item.userId),
            recipientUserId = item.recipientUserId,
            recipientUserName = item.recipientUserName ?: item.recipientUserId?.let {
                getUserNameById(it)
            },
            referenceNo = item.referenceNo
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

    private fun getUserNameById(userId: Int): String? {
        return baselineUsers.find {
            it.id == userId
        }?.name
    }

    private fun normalizeUsername(name: String): String {
        return name
            .trim()
            .lowercase()
            .replace(" ", "")
    }

    private fun normalizeTransactionType(
        transactionType: String?,
        merchantId: Int?,
        recipientUserId: Int?
    ): String {
        val type = transactionType
            ?.trim()
            ?.uppercase()
            .orEmpty()

        return when {
            type == "QRIS" -> "QRIS"
            type == "TRANSFER" -> "TRANSFER"
            type == "PAYMENT" -> "PAYMENT"
            recipientUserId != null -> "TRANSFER"
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
            "yyyy-MM-dd HH:mm",
            "2006-01-02 15:04:05"
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