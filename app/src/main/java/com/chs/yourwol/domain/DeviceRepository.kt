package com.chs.yourwol.domain

import kotlinx.coroutines.flow.Flow

interface DeviceRepository {
    fun observeDevices(): Flow<List<RemoteDeviceInfo>>
    suspend fun getDevice(id: Long): RemoteDeviceInfo?

    suspend fun saveDevice(device: RemoteDeviceInfo, password: String?): Long
    suspend fun deleteDevice(id: Long)

    suspend fun wake(device: RemoteDeviceInfo): Result<Unit>
    suspend fun powerOff(device: RemoteDeviceInfo, reboot: Boolean): PowerOffResult
    suspend fun checkPower(device: RemoteDeviceInfo): PowerState

    suspend fun resetHostKey(id: Long)
}
