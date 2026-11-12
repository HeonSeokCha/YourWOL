package com.chs.yourwol.presentation.device_list

import com.chs.yourwol.domain.RemoteDeviceInfo

sealed interface DeviceListEvent {
    data class Message(val text: String) : DeviceListEvent
    data class HostKeyChanged(
        val device: RemoteDeviceInfo,
        val expected: String,
        val actual: String
    ) : DeviceListEvent
}