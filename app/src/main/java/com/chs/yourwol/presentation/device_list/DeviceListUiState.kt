package com.chs.yourwol.presentation.device_list

import com.chs.yourwol.domain.DeviceItemUi

data class DeviceListUiState(
    val items: List<DeviceItemUi> = emptyList(),
    val isLoading: Boolean = true,
)
