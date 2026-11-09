package com.chs.yourwol.presentation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.serialization.Serializable

@Composable
fun MainNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = DeviceListRoute) {
        composable<DeviceListRoute> {

        }

        composable<DeviceEditRoute> {
        }
    }
}