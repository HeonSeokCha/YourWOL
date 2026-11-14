package com.chs.yourwol

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation3.runtime.rememberNavBackStack
import com.chs.yourwol.presentation.MainNavHost
import com.chs.yourwol.presentation.Screens
import com.chs.yourwol.ui.theme.YourWOLTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val backStack = rememberNavBackStack().apply {
                this.add(Screens.DeviceListRoute)
            }
            YourWOLTheme {
                MainNavHost(backStack)
            }
        }
    }
}