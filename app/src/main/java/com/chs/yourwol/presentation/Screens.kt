package com.chs.yourwol.presentation

import kotlinx.serialization.Serializable

@Serializable
data object DeviceListRoute

@Serializable
data class DeviceEditRoute(val id: Long = NEW_DEVICE) {
    companion object { const val NEW_DEVICE = -1L }
}
