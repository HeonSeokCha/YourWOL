package com.chs.yourwol.presentation.device_edit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.chs.yourwol.domain.model.OsType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceEditScreen(
    onDone: () -> Unit,
    viewModel: DeviceEditViewModel
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val onChange = viewModel::onFormChange

    LaunchedEffect(Unit) { viewModel.saved.collect { onDone() } }

    val form = state.form

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.isNew) "PC 등록" else "PC 편집") },
                navigationIcon = {
                    IconButton(onClick = onDone) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "뒤로") }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Section("기본 정보")
            FormField("이름", form.name, state.errors[Field.NAME]) { v -> onChange { it.copy(name = v) } }
            FormField("MAC 주소", form.mac, state.errors[Field.MAC], hint = "AA:BB:CC:DD:EE:FF") { v ->
                onChange { it.copy(mac = v) }
            }
            FormField("IP 주소", form.ip, state.errors[Field.IP], keyboard = KeyboardType.Decimal) { v ->
                onChange { it.copy(ip = v) }
            }

            Section("Wake-on-LAN")
            FormField(
                "브로드캐스트 주소 (선택)", form.broadcast, state.errors[Field.BROADCAST],
                hint = "비워두면 현재 Wi-Fi 기준 자동 계산", keyboard = KeyboardType.Decimal,
            ) { v -> onChange { it.copy(broadcast = v) } }
            FormField("WOL 포트", form.wolPort, state.errors[Field.WOL_PORT], keyboard = KeyboardType.Number) { v ->
                onChange { it.copy(wolPort = v) }
            }

            Section("원격 종료 (SSH)")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OsType.entries.forEach { os ->
                    FilterChip(
                        selected = form.osType == os,
                        onClick = { onChange { it.copy(osType = os) } },
                        label = { Text(os.name.lowercase().replaceFirstChar { it.uppercaseChar() }) },
                    )
                }
            }
            FormField("SSH 포트", form.sshPort, state.errors[Field.SSH_PORT], keyboard = KeyboardType.Number) { v ->
                onChange { it.copy(sshPort = v) }
            }
            FormField("사용자명", form.sshUser, state.errors[Field.SSH_USER]) { v -> onChange { it.copy(sshUser = v) } }
            FormField(
                label = "비밀번호",
                value = form.password,
                error = null,
                hint = if (form.hasSavedPassword) "변경할 때만 입력" else null,
                keyboard = KeyboardType.Password,
                visual = PasswordVisualTransformation(),
            ) { v -> onChange { it.copy(password = v) } }

            Button(
                onClick = viewModel::save,
                enabled = !state.saving,
                modifier = Modifier.fillMaxWidth(),
            ) { Text("저장") }
        }
    }
}

@Composable
private fun Section(title: String) {
    Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
}

@Composable
private fun FormField(
    label: String,
    value: String,
    error: String?,
    hint: String? = null,
    keyboard: KeyboardType = KeyboardType.Text,
    visual: VisualTransformation = VisualTransformation.None,
    onValueChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        isError = error != null,
        supportingText = (error ?: hint)?.let { s -> @Composable { Text(s) } },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboard),
        visualTransformation = visual,
        modifier = Modifier.fillMaxWidth(),
    )
}
