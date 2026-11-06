package com.chs.yourwol.data.network

import com.chs.yourwol.domain.Validators
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Single
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

@Single
class MagicPacketSender {
    suspend fun send(
        macAddress: String,
        broadcast: InetAddress,
        port: Int = 9,
        repeat: Int = 3,
    ) = withContext(Dispatchers.IO) {
        val payload = buildPacket(parseMac(macAddress))
        DatagramSocket().use { socket ->
            socket.broadcast = true
            val packet = DatagramPacket(payload, payload.size, broadcast, port)
            repeat(repeat) { i ->
                socket.send(packet)
                if (i < repeat - 1) delay(100)
            }
        }
    }

    companion object {
        fun parseMac(mac: String): ByteArray {
            val hex = mac.trim().replace(":", "").replace("-", "")
            require(hex.length == 12 && Validators.isValidMac(mac)) { "Invalid MAC address: $mac" }
            return ByteArray(6) { i -> hex.substring(i * 2, i * 2 + 2).toInt(16).toByte() }
        }

        fun buildPacket(mac: ByteArray): ByteArray =
            ByteArray(6) { 0xFF.toByte() } + ByteArray(16 * 6) { mac[it % 6] }
    }
}
