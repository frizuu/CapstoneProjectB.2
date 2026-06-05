package com.example.capstone_frontend

import android.util.Log
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject

class TransactionWebSocketListener : WebSocketListener() {

    private val TAG = "WebSocket"

    override fun onOpen(webSocket: WebSocket, response: Response) {
        Log.d(TAG, "🟢 Connection Opened!")
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        Log.d(TAG, "📩 Message Received: $text")

        try {
            val json = JSONObject(text)
            if (json.has("type") && json.getString("type") == "TRANSACTION_UPDATE") {
                val payload = json.getJSONObject("payload")
                val status = payload.getString("status")
                Log.d(TAG, "Transaction Status: $status")

                // TODO: Update the Android UI here later!
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing message: ${e.message}")
        }
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        webSocket.close(1000, null)
        Log.d(TAG, "🟡 Connection Closing: $code / $reason")
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        Log.e(TAG, "🔴 Connection Failed: ${t.message}")
    }
}