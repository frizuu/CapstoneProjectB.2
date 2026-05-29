package com.example.capstone_frontend.data

import com.google.gson.annotations.SerializedName

data class BalanceResponse(
    @SerializedName("balance")
    val balance: Int
)

data class PaymentRequest(
    @SerializedName("user_id")
    val userId: Int,

    @SerializedName("recipient_user_id")
    val recipientUserId: Int? = null,

    @SerializedName("amount")
    val amount: Int,

    @SerializedName("reference_no")
    val referenceNo: String? = null
)

data class PaymentResponse(
    @SerializedName("status")
    val status: String,

    @SerializedName("code")
    val code: String? = null,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("transaction_id")
    val transactionId: Int? = null,

    @SerializedName("reference_no")
    val referenceNo: String? = null,

    @SerializedName("audit_id")
    val auditId: Int? = null
)

data class QrisInquiryResponse(
    @SerializedName("status")
    val status: String,

    @SerializedName("merchant_id")
    val merchantId: Int,

    @SerializedName("merchant_name")
    val merchantName: String,

    @SerializedName("merchant_code")
    val merchantCode: String,

    @SerializedName("category")
    val category: String
)

data class QrisPaymentRequest(
    @SerializedName("user_id")
    val userId: Int,

    @SerializedName("merchant_code")
    val merchantCode: String,

    @SerializedName("amount")
    val amount: Int,

    @SerializedName("reference_no")
    val referenceNo: String? = null
)

data class MerchantBalanceResponse(
    @SerializedName("merchant_id")
    val merchantId: Int,

    @SerializedName("balance")
    val balance: Long
)

data class MerchantListResponse(
    @SerializedName("status")
    val status: String,

    @SerializedName("merchants")
    val merchants: List<MerchantDto>
)

data class MerchantDto(
    @SerializedName("ID")
    val id: Int,

    @SerializedName("Name")
    val name: String,

    @SerializedName("Balance")
    val balance: Long,

    @SerializedName("MerchantCode")
    val merchantCode: String,

    @SerializedName("Category")
    val category: String,

    @SerializedName("Status")
    val status: String,

    @SerializedName("CreatedAt")
    val createdAt: String
)

data class TransactionHistoryResponse(
    @SerializedName("status")
    val status: String,

    @SerializedName("code")
    val code: String? = null,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("user_id")
    val userId: Int? = null,

    @SerializedName("transactions")
    val transactions: List<BackendTransactionResponse> = emptyList()
)

data class BackendTransactionResponse(
    @SerializedName("id")
    val id: Int,

    @SerializedName("user_id")
    val userId: Int,

    @SerializedName("sender_name")
    val senderName: String? = null,

    @SerializedName("merchant_id")
    val merchantId: Int? = null,

    @SerializedName("merchant_name")
    val merchantName: String? = null,

    @SerializedName("merchant_code")
    val merchantCode: String? = null,

    @SerializedName("recipient_user_id")
    val recipientUserId: Int? = null,

    @SerializedName("recipient_user_name")
    val recipientUserName: String? = null,

    @SerializedName("amount")
    val amount: Int,

    @SerializedName("status")
    val status: String,

    @SerializedName("transaction_type")
    val transactionType: String? = null,

    @SerializedName("direction")
    val direction: String? = null,

    @SerializedName("reference_no")
    val referenceNo: String? = null,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("idempotency_key")
    val idempotencyKey: String? = null
)