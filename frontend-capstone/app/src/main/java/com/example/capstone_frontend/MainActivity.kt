package com.example.capstone_frontend

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.capstone_frontend.navigation.AppNavigation
import com.example.capstone_frontend.ui.theme.Capstone_FrontendTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            Capstone_FrontendTheme {
                AppNavigation()
            }
        }
    }
}