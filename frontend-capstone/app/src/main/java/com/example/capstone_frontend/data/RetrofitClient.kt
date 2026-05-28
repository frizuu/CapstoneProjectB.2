package com.example.capstone_frontend.data

import com.example.capstone_frontend.Constants // 1. ADD THIS IMPORT
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okio.Buffer
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    // 2. CHANGE THIS LINE (Remove 'const' and use the Constants file)
    private val BASE_URL = "http://${Constants.SERVER_IP_AND_PORT}/"

    private val dynamicUserInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val currentUserId = DummyRepository.getCurrentUserId()

        val originalUrl = originalRequest.url()

        val updatedUrl = if (originalUrl.queryParameter("user_id") != null) {
            originalUrl.newBuilder()
                .setQueryParameter("user_id", currentUserId.toString())
                .build()
        } else {
            originalUrl
        }

        val requestBuilder = originalRequest.newBuilder()
            .url(updatedUrl)

        val originalBody = originalRequest.body()

        if (originalBody != null) {
            val buffer = Buffer()
            originalBody.writeTo(buffer)

            val originalBodyText = buffer.readUtf8()
            val contentType = originalBody.contentType()

            val updatedBodyText = originalBodyText.replace(
                Regex("\"user_id\"\\s*:\\s*1"),
                "\"user_id\":$currentUserId"
            )

            val updatedBody = RequestBody.create(
                contentType,
                updatedBodyText
            )

            requestBuilder.method(
                originalRequest.method(),
                updatedBody
            )
        }

        chain.proceed(requestBuilder.build())
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(dynamicUserInterceptor)
        .build()

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}