package com.chs.yourwol.presentation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface Screens : NavKey {
    @Serializable
    data object DeviceListRoute : Screens

    @Serializable
    data class DeviceEditRoute(val id: Long = NEW_DEVICE) : Screens {
        companion object {
            const val NEW_DEVICE = -1L
        }
    }
}
