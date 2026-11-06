package com.chs.yourwol.data.network

import java.nio.ByteBuffer

object SubnetUtils {
    fun broadcastOf(ipv4: ByteArray, prefixLength: Int): ByteArray {
        require(ipv4.size == 4) { "IPv4 only" }
        require(prefixLength in 0..32) { "prefix must be 0..32" }
        val ip = ByteBuffer.wrap(ipv4).int
        val mask = if (prefixLength == 0) 0 else (-1 shl (32 - prefixLength))
        return ByteBuffer.allocate(4).putInt(ip or mask.inv()).array()
    }
}
