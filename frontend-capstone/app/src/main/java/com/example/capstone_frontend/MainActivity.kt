package com.example.capstone_frontend

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var webSocket: okhttp3.WebSocket

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        connectToWebSocket()
    }

    private fun connectToWebSocket() {
        // 1. Build the OkHttpClient (no timeouts for WebSockets)
        val client = OkHttpClient.Builder()
            .readTimeout(0, TimeUnit.MILLISECONDS)
            .build()

        // 2. Point to the Go server using the Emulator's localhost (10.0.2.2)
        val request = Request.Builder()
            .url("ws://10.0.2.2:8080/ws")
            .build()

        // 3. Connect!
        val listener = TransactionWebSocketListener()
        webSocket = client.newWebSocket(request, listener)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clean up the connection when the app closes
        if (::webSocket.isInitialized) {
            webSocket.close(1000, "Activity Destroyed")
        }
    }
}