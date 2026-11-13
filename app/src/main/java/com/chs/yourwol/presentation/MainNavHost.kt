package com.chs.yourwol.presentation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.chs.yourwol.presentation.device_edit.DeviceEditScreen
import com.chs.yourwol.presentation.device_edit.DeviceEditViewModel
import com.chs.yourwol.presentation.device_list.DeviceListScreen
import com.chs.yourwol.presentation.device_list.DeviceListViewModel
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

@Composable
fun MainNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = DeviceListRoute) {
        composable<DeviceListRoute> {
            val viewModel: DeviceListViewModel = koinViewModel()
            DeviceListScreen(
                viewModel = viewModel,
                onAddDevice = { navController.navigate(DeviceEditRoute()) },
                onEditDevice = { navController.navigate(DeviceEditRoute(it)) }
            )
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