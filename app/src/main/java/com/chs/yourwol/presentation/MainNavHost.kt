package com.chs.yourwol.presentation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.chs.yourwol.presentation.device_edit.DeviceEditScreen
import com.chs.yourwol.presentation.device_edit.DeviceEditViewModel
import com.chs.yourwol.presentation.device_list.DeviceListScreen
import com.chs.yourwol.presentation.device_list.DeviceListViewModel
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun MainNavHost(
    backStack: NavBackStack<NavKey>
) {

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        entryProvider = entryProvider {

            entry<Screens.DeviceListRoute> {
                val viewModel: DeviceListViewModel = koinViewModel()
                DeviceListScreen(
                    viewModel = viewModel,
                    onAddDevice = { backStack.add(Screens.DeviceEditRoute()) },
                    onEditDevice = { backStack.add(Screens.DeviceEditRoute(it)) }
                )
            }

            entry<Screens.DeviceEditRoute> { key ->
                val viewModel: DeviceEditViewModel = koinViewModel {
                    parametersOf(key.id)
                }
                DeviceEditScreen(
                    viewModel = viewModel,
                    onDone = { backStack.removeLastOrNull() }
                )
            }
        }
    )
}