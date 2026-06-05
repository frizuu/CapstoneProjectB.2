package com.example.capstone_frontend.model

data class NotificationData(
    val id: String,
    val title: String,
    val message: String,
    val amount: Int,
    val amountText: String,
    val type: NotificationType,
    val status: String,
    val createdAt: String,
    val transactionId: String
)

enum class NotificationType {
    INCOMING_TRANSFER,
    OUTGOING_TRANSFER,
    QRIS_SUCCESS,
    QRIS_FAILED,
    TRANSFER_FAILED,
    REFUND,
    INFO
}