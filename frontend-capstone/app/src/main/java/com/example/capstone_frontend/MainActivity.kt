package com.example.capstone_frontend

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.capstone_frontend.data.DummyRepository
import com.example.capstone_frontend.navigation.AppNavigation
import com.example.capstone_frontend.ui.theme.Capstone_FrontendTheme
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

    private lateinit var webSocket: WebSocket

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Capstone_FrontendTheme {
                AppNavigation()
            }
        }

        connectToWebSocket()
    }

    private fun connectToWebSocket() {
        val client = OkHttpClient.Builder()
            .readTimeout(0, TimeUnit.MILLISECONDS)
            .build()

        val currentUserId = DummyRepository.getCurrentUserId()

        val request = Request.Builder()
            .url("ws://${Constants.SERVER_IP_AND_PORT}/ws?user_id=$currentUserId")
            .build()

        val listener = TransactionWebSocketListener()
        webSocket = client.newWebSocket(request, listener)
    }

    override fun onDestroy() {
        super.onDestroy()

        if (::webSocket.isInitialized) {
            webSocket.close(1000, "Activity Destroyed")
        }
    }
}