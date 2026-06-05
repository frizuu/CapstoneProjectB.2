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
        val key = buildKey(
            userId = userId,
            transactionId = transactionId
        )

        return shownPopupKeys.contains(key)
    }

    fun markPopupShown(
        userId: Int,
        transactionId: String
    ) {
        val key = buildKey(
            userId = userId,
            transactionId = transactionId
        )

        shownPopupKeys.add(key)
    }

    fun isNotificationRead(
        userId: Int,
        transactionId: String
    ): Boolean {
        val key = buildKey(
            userId = userId,
            transactionId = transactionId
        )

        return readNotificationKeys.contains(key)
    }

    fun markNotificationRead(
        userId: Int,
        transactionId: String
    ) {
        val key = buildKey(
            userId = userId,
            transactionId = transactionId
        )

        readNotificationKeys.add(key)
    }

    fun getUnreadCount(
        userId: Int,
        transactions: List<TransactionData>
    ): Int {
        return transactions.count { transaction ->
            val isNotification = shouldCountAsNotification(
                transaction = transaction,
                currentUserId = userId
            )

            val isRead = isNotificationRead(
                userId = userId,
                transactionId = transaction.transactionId
            )

            isNotification && !isRead
        }
    }

    fun shouldCountAsNotification(
        transaction: TransactionData,
        currentUserId: Int
    ): Boolean {
        val type = transaction.transactionType.trim().uppercase()
        val status = transaction.status.trim().uppercase()
        val direction = transaction.direction.trim().uppercase()
        val transactionUserId = transaction.userId.toIntOrNull()

        val isIncomingTransfer =
            type == "TRANSFER" &&
                    transaction.recipientUserId == currentUserId &&
                    transactionUserId != currentUserId

        val isOutgoingTransfer =
            type == "TRANSFER" &&
                    transactionUserId == currentUserId

        val isQrisForCurrentUser =
            type == "QRIS" &&
                    transactionUserId == currentUserId

        val isRefund =
            type == "REFUND" ||
                    direction == "REFUND" ||
                    transaction.merchantName.contains("refund", ignoreCase = true)

        val isFailed =
            status != "SUCCESS"

        return isIncomingTransfer ||
                isOutgoingTransfer ||
                isQrisForCurrentUser ||
                isRefund ||
                isFailed
    }
}