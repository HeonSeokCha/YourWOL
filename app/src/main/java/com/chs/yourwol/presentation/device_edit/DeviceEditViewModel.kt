package com.chs.yourwol.presentation.device_edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chs.yourwol.domain.DeviceRepository
import com.chs.yourwol.domain.Validators
import com.chs.yourwol.domain.model.RemoteDeviceInfo
import com.chs.yourwol.presentation.DeviceEditRoute
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class DeviceEditViewModel(
    @InjectedParam private val deviceId: Long,
    private val repository: DeviceRepository,
) : ViewModel() {
    private var original: RemoteDeviceInfo? = null

    private val _uiState = MutableStateFlow(DeviceEditUiState(isNew = deviceId == DeviceEditRoute.NEW_DEVICE))
    val uiState: StateFlow<DeviceEditUiState> = _uiState.asStateFlow()

    private val _saved = Channel<Unit>(Channel.CONFLATED)
    val saved = _saved.receiveAsFlow()

    init {
        if (deviceId != DeviceEditRoute.NEW_DEVICE) viewModelScope.launch {
            repository.getDevice(deviceId)?.let { d ->
                original = d
                _uiState.update {
                    it.copy(
                        form = DeviceForm(
                            name = d.name,
                            mac = d.macAddress,
                            ip = d.ipAddress,
                            broadcast = d.broadcastAddress.orEmpty(),
                            wolPort = d.wolPort.toString(),
                            sshPort = d.sshPort.toString(),
                            sshUser = d.sshUser,
                            hasSavedPassword = d.hasPassword
                        )
                    )
                }
            }
        }
    }

    fun onFormChange(transform: (DeviceForm) -> DeviceForm) =
        _uiState.update { it.copy(form = transform(it.form), errors = emptyMap()) }

    fun save() {
        val form = _uiState.value.form
        val errors = validate(form)
        if (errors.isNotEmpty()) {
            _uiState.update { it.copy(errors = errors) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(saving = true) }
            repository.saveDevice(
                device = RemoteDeviceInfo(
                    id = original?.id ?: 0L,
                    name = form.name,
                    macAddress = form.mac,
                    ipAddress = form.ip,
                    broadcastAddress = form.broadcast.ifBlank { null },
                    wolPort = form.wolPort.toInt(),
                    sshPort = form.sshPort.toInt(),
                    sshUser = form.sshUser
                ),
                password = form.password.ifEmpty { null },
            )
            _saved.send(Unit)
        }
    }

    private fun validate(f: DeviceForm): Map<Field, String> = buildMap {
        if (f.name.isBlank()) put(Field.NAME, "이름을 입력하세요")
        if (!Validators.isValidMac(f.mac)) put(Field.MAC, "형식: AA:BB:CC:DD:EE:FF")
        if (!Validators.isValidIpv4(f.ip)) put(Field.IP, "IPv4 주소를 입력하세요")
        if (f.broadcast.isNotBlank() && !Validators.isValidIpv4(f.broadcast))
            put(Field.BROADCAST, "올바른 IPv4 주소 (비워두면 자동 계산)")
        if (!Validators.isValidPort(f.wolPort)) put(Field.WOL_PORT, "1~65535")
        if (!Validators.isValidPort(f.sshPort)) put(Field.SSH_PORT, "1~65535")
        if (f.sshUser.isBlank()) put(Field.SSH_USER, "SSH 사용자명을 입력하세요")
    }
}