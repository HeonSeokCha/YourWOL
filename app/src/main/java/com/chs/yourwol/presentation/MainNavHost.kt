package com.chs.yourwol.presentation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.chs.yourwol.presentation.device_edit.DeviceEditScreen
import com.chs.yourwol.presentation.device_edit.DeviceEditViewModel
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

@Composable
fun MainNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = DeviceListRoute) {
        composable<DeviceListRoute> {

        }

        composable<DeviceEditRoute> {
            val viewModel: DeviceEditViewModel = koinViewModel()
            DeviceEditScreen(
                viewModel = viewModel,
                onDone = { navController.popBackStack() }
            )
        }
    }
}