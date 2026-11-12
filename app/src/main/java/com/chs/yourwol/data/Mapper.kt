package com.chs.yourwol.data

import com.chs.yourwol.data.db.DeviceEntity
import com.chs.yourwol.domain.RemoteDeviceInfo

fun DeviceEntity.toDomain() = RemoteDeviceInfo(
    id = id,
    name = name,
    macAddress = macAddress,
    ipAddress = ipAddress,
    broadcastAddress = broadcastAddress,
    wolPort = wolPort,
    sshPort = sshPort,
    sshUser = sshUser,
    hasPassword = encryptedPassword != null,
    hostKeyFingerprint = hostKeyFingerprint,
)
