package com.example.capstone_frontend.model

data class TransactionData(
    val transactionId: String,
    val userId: String,
    val merchantId: Int? = null,
    val merchantName: String,
    val amount: Int,
    val status: String,
    val transactionType: String = "PAYMENT",
    val createdAt: String,
    val latencyMs: Int = 0,
    val cacheState: String = "Database Baseline",
    val networkProfile: String = "Normal",
    val idempotencyKey: String = "-",

    // Transfer antar-user
    val direction: String = "OUT",
    val senderName: String? = null,
    val recipientUserId: Int? = null,
    val recipientUserName: String? = null,
    val referenceNo: String? = null
)

data class MetricData(
    val totalTransaction: Int,
    val successRate: Int,
    val averageLatency: Int,
    val timeoutCount: Int,
    val cacheHitRate: Int
)