package com.example.capstone_frontend.data

import com.example.capstone_frontend.model.TransactionData

object NotificationReadStore {

    private val shownPopupKeys = mutableSetOf<String>()
    private val readNotificationKeys = mutableSetOf<String>()

    fun buildKey(
        userId: Int,
        transactionId: String
    ): String {
        return "$userId:$transactionId"
    }

    fun hasShownPopup(
        userId: Int,
        transactionId: String
    ): Boolean {
        return shownPopupKeys.contains(
            buildKey(
                userId = userId,
                transactionId = transactionId
            )
        )
    }

    fun markPopupShown(
        userId: Int,
        transactionId: String
    ) {
        shownPopupKeys.add(
            buildKey(
                userId = userId,
                transactionId = transactionId
            )
        )
    }

    fun isNotificationRead(
        userId: Int,
        transactionId: String
    ): Boolean {
        return readNotificationKeys.contains(
            buildKey(
                userId = userId,
                transactionId = transactionId
            )
        )
    }

    fun markNotificationRead(
        userId: Int,
        transactionId: String
    ) {
        readNotificationKeys.add(
            buildKey(
                userId = userId,
                transactionId = transactionId
            )
        )
    }

    fun getUnreadCount(
        userId: Int,
        transactions: List<TransactionData>
    ): Int {
        return transactions.count { transaction ->
            shouldCountAsNotification(
                transaction = transaction,
                currentUserId = userId
            ) && !isNotificationRead(
                userId = userId,
                transactionId = transaction.transactionId
            )
        }
    }

    fun shouldCountAsNotification(
        transaction: TransactionData,
        currentUserId: Int
    ): Boolean {
        val type = transaction.transactionType.uppercase()
        val status = transaction.status.uppercase()

        val isIncomingTransfer = type == "TRANSFER" &&
                transaction.recipientUserId == currentUserId &&
                transaction.userId.toIntOrNull() != currentUserId

        val isOutgoingTransfer = type == "TRANSFER" &&
                transaction.userId.toIntOrNull() == currentUserId

        val isQris = type == "QRIS"
        val isFailed = status != "SUCCESS"

        return isIncomingTransfer ||
                isOutgoingTransfer ||
                isQris ||
                isFailed
    }
}