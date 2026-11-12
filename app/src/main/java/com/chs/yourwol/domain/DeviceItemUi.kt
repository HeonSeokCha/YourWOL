package com.chs.yourwol.domain

data class DeviceItemUi(
    val device: RemoteDeviceInfo,
    val power: PowerState,
    val pending: PowerAction?
)
