package com.example.capstone_frontend

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.capstone_frontend.data.DummyRepository // assuming you use this to get the user ID
import com.example.capstone_frontend.data.WebSocketManager
import com.example.capstone_frontend.navigation.AppNavigation
import com.example.capstone_frontend.ui.theme.Capstone_FrontendTheme

class MainActivity : ComponentActivity() {

    // 1. Declare the WebSocketManager
    private lateinit var webSocketManager: WebSocketManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 2. Initialize it with the current User's ID (e.g., User 2)
        val currentUserId = DummyRepository.getCurrentUserId() // Or just hardcode '2' for testing
        webSocketManager = WebSocketManager(loggedInUserId = currentUserId)

        // 3. Connect it!
        webSocketManager.connect()

        setContent {
            Capstone_FrontendTheme {
                AppNavigation()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clean up the connection when the app closes
        webSocketManager.disconnect()
    }
}
