package com.chs.yourwol.domain

object Validators {
    private val MAC = Regex("^[0-9A-Fa-f]{2}([:-]?[0-9A-Fa-f]{2}){5}$")
    private val IPV4 =
        Regex("""^((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)\.){3}(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)$""")

    fun isValidMac(value: String): Boolean = MAC.matches(value.trim())
    fun isValidIpv4(value: String): Boolean = IPV4.matches(value.trim())
    fun isValidPort(value: String): Boolean = value.toIntOrNull() in 1..65535
}
