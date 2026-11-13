package com.chs.yourwol.presentation.device_list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chs.yourwol.domain.DeviceItemUi
import com.chs.yourwol.domain.RemoteDeviceInfo
import com.chs.yourwol.domain.PowerAction
import com.chs.yourwol.domain.PowerState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceListScreen(
    onAddDevice: () -> Unit,
    onEditDevice: (Long) -> Unit,
    viewModel: DeviceListViewModel
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackBar = remember { SnackbarHostState() }
    var hostKeyDialog by remember { mutableStateOf<DeviceListEvent.HostKeyChanged?>(null) }
    var confirmShutdown by remember { mutableStateOf<RemoteDeviceInfo?>(null) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { e ->
            when (e) {
                is DeviceListEvent.Message -> snackBar.showSnackbar(e.text)
                is DeviceListEvent.HostKeyChanged -> hostKeyDialog = e
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("WOL Remote") },
                actions = {
                    IconButton(onClick = viewModel::refresh) { Icon(Icons.Default.Refresh, "새로고침") }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddDevice) { Icon(Icons.Default.Add, "기기 추가") }
        },
        snackbarHost = { SnackbarHost(snackBar) },
    ) { padding ->
        when {
            state.isLoading -> Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                CircularProgressIndicator()
            }
            state.items.isEmpty() -> Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                Text("+ 버튼으로 PC를 등록하세요")
            }
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(state.items, key = { it.device.id }) { item ->
                    DeviceCard(
                        item = item,
                        onWake = { viewModel.wake(item.device) },
                        onShutdown = { confirmShutdown = item.device },
                        onReboot = { viewModel.reboot(item.device) },
                        onEdit = { onEditDevice(item.device.id) },
                        onDelete = { viewModel.delete(item.device) },
                    )
                }
            }
        }
    }

    confirmShutdown?.let { device ->
        AlertDialog(
            onDismissRequest = { confirmShutdown = null },
            title = { Text("${device.name} 끄기") },
            text = { Text("저장되지 않은 작업이 있으면 사라질 수 있습니다.") },
            confirmButton = {
                TextButton(onClick = { viewModel.shutdown(device); confirmShutdown = null }) { Text("끄기") }
            },
            dismissButton = { TextButton(onClick = { confirmShutdown = null }) { Text("취소") } },
        )
    }

    hostKeyDialog?.let { e ->
        AlertDialog(
            onDismissRequest = { hostKeyDialog = null },
            title = { Text("호스트 키가 변경되었습니다") },
            text = {
                Text(
                    "PC를 재설치했거나 SSH 서버를 다시 설치한 경우가 아니라면 " +
                            "중간자 공격(MITM)일 수 있습니다.\n\n저장된 키: ${e.expected}\n현재 키: ${e.actual}"
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.trustNewHostKey(e.device); hostKeyDialog = null }) {
                    Text("새 키 신뢰")
                }
            },
            dismissButton = { TextButton(onClick = { hostKeyDialog = null }) { Text("취소") } },
        )
    }
}

@Composable
private fun DeviceCard(
    item: DeviceItemUi,
    onWake: () -> Unit,
    onShutdown: () -> Unit,
    onReboot: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    var menuOpen by remember { mutableStateOf(false) }
    val busy = item.pending != null

    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                StatusDot(item.power)
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    Text(item.device.name, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "${item.device.ipAddress} · ${item.device.macAddress}",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                Box {
                    IconButton(onClick = { menuOpen = true }) { Icon(Icons.Default.MoreVert, "더보기") }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        DropdownMenuItem(text = { Text("재시작") }, enabled = item.power == PowerState.ONLINE && !busy,
                            onClick = { menuOpen = false; onReboot() })
                        DropdownMenuItem(text = { Text("편집") }, onClick = { menuOpen = false; onEdit() })
                        DropdownMenuItem(text = { Text("삭제") }, onClick = { menuOpen = false; onDelete() })
                    }
                }
            }

            Spacer(Modifier.size(12.dp))

            if (busy) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        when (item.pending) {
                            PowerAction.WAKE -> "켜는 중…"
                            PowerAction.SHUTDOWN -> "끄는 중…"
                            PowerAction.REBOOT -> "재시작 중…"
                            null -> ""
                        }
                    )
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onWake, enabled = item.power != PowerState.ONLINE) {
                        Icon(Icons.Default.PowerSettingsNew, null)
                        Spacer(Modifier.width(4.dp))
                        Text("켜기")
                    }
                    OutlinedButton(
                        onClick = onShutdown,
                        enabled = item.power == PowerState.ONLINE && item.device.hasPassword,
                    ) { Text("끄기") }
                }
            }
        }
    }
}

@Composable
private fun StatusDot(state: PowerState) {
    val color = when (state) {
        PowerState.ONLINE -> Color(0xFF2E7D32)
        PowerState.OFFLINE -> Color(0xFF9E9E9E)
        PowerState.UNKNOWN -> Color(0xFFFFB300)
    }
    Box(Modifier.size(12.dp).background(color, CircleShape))
}
