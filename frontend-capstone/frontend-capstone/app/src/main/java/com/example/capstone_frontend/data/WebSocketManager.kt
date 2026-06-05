package com.example.capstone_frontend.data

// Add this import so the file can see your hardcoded IP!
import com.example.capstone_frontend.Constants

import android.util.Log
import kotlinx.coroutines.*
import okhttp3.*
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class WebSocketManager(private val loggedInUserId: Int) {

    private var webSocket: WebSocket? = null
    private var isIntentionalClose = false

    // Notice the "ws://" prefix here for WebSockets!
    private val serverUrl = "ws://${Constants.SERVER_IP_AND_PORT}/ws?user_id=$loggedInUserId"

    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .pingInterval(30, TimeUnit.SECONDS)
        .build()

    fun connect() {
        isIntentionalClose = false
        val request = Request.Builder().url(serverUrl).build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("WebSocket", "✅ Connected to Local Backend!")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("WebSocket", "🔔 New Event: $text")
                // Parse the JSON and trigger UI updates here!
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("WebSocket", "🚪 Connection Closed: $reason")
                if (!isIntentionalClose) reconnect()
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("WebSocket", "❌ Connection Failed. Retrying...")
                if (!isIntentionalClose) reconnect()
            }
        })
    }

    private fun reconnect() {
        CoroutineScope(Dispatchers.IO).launch {
            delay(3000) // Wait 3 seconds before retrying
            Log.d("WebSocket", "🔄 Attempting to reconnect...")
            connect()
        }
    }

    fun disconnect() {
        isIntentionalClose = true
        webSocket?.close(1000, "App closed")
        webSocket = null
    }
}