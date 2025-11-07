package com.example.myapplicationfrancescarezza

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.myapplicationfrancescarezza.ui.FlightSearchApp
import com.example.myapplicationfrancescarezza.ui.theme.MyApplicationFrancescaRezzaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationFrancescaRezzaTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    FlightSearchApp()
                }
            }
        }
    }
}