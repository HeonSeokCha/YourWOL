package com.chs.yourwol.data.db

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "devices")
data class DeviceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val macAddress: String,
    val ipAddress: String,
    val broadcastAddress: String?,
    val wolPort: Int,
    val sshPort: Int,
    val sshUser: String,
    val encryptedPassword: ByteArray?,
    val hostKeyFingerprint: String?,
)
