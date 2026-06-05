package com.example.capstone_frontend.data

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {

    @GET("balance")
    suspend fun getBalance(
        @Query("user_id") userId: Int
    ): BalanceResponse

    @Headers("Content-Type: application/json")
    @POST("payment")
    suspend fun createPayment(
        @Body request: PaymentRequest
    ): PaymentResponse

    @GET("qris/inquiry")
    suspend fun inquiryQris(
        @Query("merchant_code") merchantCode: String
    ): QrisInquiryResponse

    @Headers("Content-Type: application/json")
    @POST("qris/payment")
    suspend fun qrisPayment(
        @Header("Idempotency-Key") idempotencyKey: String,
        @Body request: QrisPaymentRequest
    ): PaymentResponse

    @GET("merchant/balance")
    suspend fun getMerchantBalance(
        @Query("merchant_id") merchantId: Int
    ): MerchantBalanceResponse

    @GET("merchants")
    suspend fun getMerchants(): MerchantListResponse

    @GET("transactions")
    suspend fun getTransactions(
        @Query("user_id") userId: Int
    ): TransactionHistoryResponse
}