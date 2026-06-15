package com.tertiaryinfotech.fractalapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.tertiaryinfotech.fractalapp.ui.FractalTheme
import com.tertiaryinfotech.fractalapp.ui.HomeScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val demo = intent?.getStringExtra("demo")
        setContent {
            FractalTheme {
                HomeScreen(initialPreset = demo)
            }
        }
    }
}
