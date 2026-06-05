package com.example.capstone_frontend

import android.util.Log
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject

class TransactionWebSocketListener : WebSocketListener() {

    private val tag = "WebSocket"

    override fun onOpen(webSocket: WebSocket, response: Response) {
        Log.d(tag, "Connection opened")
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        Log.d(tag, "Message received: $text")

        try {
            val json = JSONObject(text)

            if (json.has("type") && json.getString("type") == "TRANSACTION_UPDATE") {
                val payload = json.getJSONObject("payload")
                val status = payload.optString("status")

                Log.d(tag, "Transaction status: $status")
            }
        } catch (e: Exception) {
            Log.e(tag, "Error parsing message: ${e.message}")
        }
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        webSocket.close(1000, null)
        Log.d(tag, "Connection closing: $code / $reason")
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        Log.e(tag, "Connection failed: ${t.message}")
    }
}