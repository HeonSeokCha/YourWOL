package com.chs.yourwol.presentation.device_edit

import com.chs.yourwol.domain.model.OsType

data class DeviceEditUiState(
    val isNew: Boolean = true,
    val form: DeviceForm = DeviceForm(),
    val errors: Map<Field, String> = emptyMap(),
    val saving: Boolean = false,
)

data class DeviceForm(
    val name: String = "",
    val mac: String = "",
    val ip: String = "",
    val broadcast: String = "",
    val wolPort: String = "9",
    val sshPort: String = "22",
    val sshUser: String = "",
    val password: String = "",
    val osType: OsType = OsType.WINDOWS,
    val hasSavedPassword: Boolean = false,
)

enum class Field { NAME, MAC, IP, BROADCAST, WOL_PORT, SSH_PORT, SSH_USER }