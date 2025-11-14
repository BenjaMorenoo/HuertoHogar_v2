package com.mecagoentodo.huertohogar_v2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.mecagoentodo.huertohogar_v2.ui.theme.HuertoHogar_v2Theme
import com.mecagoentodo.huertohogar_v2.view.MainScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HuertoHogar_v2Theme {
                MainScreen()
            }
        }
    }
}