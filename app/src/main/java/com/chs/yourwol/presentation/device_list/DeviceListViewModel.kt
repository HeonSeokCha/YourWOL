package com.chs.yourwol.presentation.device_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chs.yourwol.domain.DeviceItemUi
import com.chs.yourwol.domain.DeviceRepository
import com.chs.yourwol.domain.PowerAction
import com.chs.yourwol.domain.PowerOffResult
import com.chs.yourwol.domain.PowerState
import com.chs.yourwol.domain.RemoteDeviceInfo
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class DeviceListViewModel(
    private val repository: DeviceRepository
) : ViewModel() {
    private val devices: Flow<List<RemoteDeviceInfo>> = repository.observeDevices()
        .shareIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            replay = 1
        )

    private val refreshTick = MutableStateFlow(0)
    private val pending = MutableStateFlow<Map<Long, PowerAction>>(emptyMap())

    private val _events = Channel<DeviceListEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private val powerStates: Flow<Map<Long, PowerState>> =
        combine(devices, refreshTick) { list, _ -> list }
            .flatMapLatest { list ->
                if (list.isEmpty()) flowOf(emptyMap())
                else flow {
                    while (true) {
                        val states = coroutineScope {
                            list.map { d -> async { d.id to repository.checkPower(d) } }.awaitAll()
                        }.toMap()
                        emit(states)
                        delay(POLL_INTERVAL_MS)
                    }
                }
            }
            .onStart { emit(emptyMap()) }

    val uiState: StateFlow<DeviceListUiState> =
        combine(devices, powerStates, pending) { list, states, pend ->
            DeviceListUiState(
                items = list.map { d ->
                    DeviceItemUi(d, states[d.id] ?: PowerState.UNKNOWN, pend[d.id])
                },
                isLoading = false,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DeviceListUiState())

    fun refresh() = refreshTick.update { it + 1 }

    fun wake(device: RemoteDeviceInfo) = runPowerAction(device, PowerAction.WAKE) {
        repository.wake(device).fold(
            onSuccess = { send("${device.name}에 매직 패킷을 보냈습니다"); true },
            onFailure = { send("전송 실패: ${it.message}"); false },
        )
    }

    fun shutdown(device: RemoteDeviceInfo) = runPowerAction(device, PowerAction.SHUTDOWN) {
        handlePowerOff(device, repository.powerOff(device, reboot = false))
    }

    fun reboot(device: RemoteDeviceInfo) = runPowerAction(device, PowerAction.REBOOT) {
        handlePowerOff(device, repository.powerOff(device, reboot = true))
    }

    fun trustNewHostKey(device: RemoteDeviceInfo) = viewModelScope.launch {
        repository.resetHostKey(device.id)
        send("호스트 키를 초기화했습니다. 다음 접속 시 새 키를 저장합니다.")
    }

    fun delete(device: RemoteDeviceInfo) = viewModelScope.launch { repository.deleteDevice(device.id) }

    private suspend fun handlePowerOff(device: RemoteDeviceInfo, result: PowerOffResult): Boolean =
        when (result) {
            PowerOffResult.Success -> { send("${device.name}에 명령을 보냈습니다"); true }
            PowerOffResult.NoCredential -> { send("SSH 비밀번호를 먼저 등록하세요"); false }
            is PowerOffResult.HostKeyMismatch -> {
                _events.send(DeviceListEvent.HostKeyChanged(device, result.expected, result.actual))
                false
            }
            is PowerOffResult.Failure -> { send("실패: ${result.cause.message}"); false }
        }

    private fun runPowerAction(device: RemoteDeviceInfo, action: PowerAction, block: suspend () -> Boolean) {
        if (pending.value.containsKey(device.id)) return
        viewModelScope.launch {
            pending.update { it + (device.id to action) }
            try {
                if (block()) {
                    val targets = when (action) {
                        PowerAction.WAKE -> listOf(PowerState.ONLINE)
                        PowerAction.SHUTDOWN -> listOf(PowerState.OFFLINE)
                        PowerAction.REBOOT -> listOf(PowerState.OFFLINE, PowerState.ONLINE)
                    }
                    withTimeoutOrNull(TRANSITION_TIMEOUT_MS) {
                        for (target in targets) {
                            while (repository.checkPower(device) != target) delay(TRANSITION_POLL_MS)
                        }
                    } ?: send("${device.name}: 응답 대기 시간이 초과되었습니다")
                    refresh()
                }
            } finally {
                pending.update { it - device.id }
            }
        }
    }

    private suspend fun send(text: String) = _events.send(DeviceListEvent.Message(text))

    private companion object {
        const val POLL_INTERVAL_MS = 5_000L
        const val TRANSITION_POLL_MS = 3_000L
        const val TRANSITION_TIMEOUT_MS = 180_000L
    }
}