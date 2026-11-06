package com.chs.yourwol.domain.model

data class RemoteDeviceInfo(
    val id: Long = 0,
    val name: String,
    val macAddress: String,
    val ipAddress: String,
    val broadcastAddress: String?,
    val wolPort: Int = 9,
    val sshPort: Int = 22,
    val sshUser: String,
    val hasPassword: Boolean = false,
    val hostKeyFingerprint: String? = null,
)

enum class OsType(
    val shutdownCommand: String,
    val rebootCommand: String
) {
    WINDOWS("shutdown /s /t 3", "shutdown /r /t 3"),
}

enum class PowerState { UNKNOWN, ONLINE, OFFLINE }

enum class PowerAction { WAKE, SHUTDOWN, REBOOT }
