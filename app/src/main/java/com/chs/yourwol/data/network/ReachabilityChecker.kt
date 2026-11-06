package com.chs.yourwol.data.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Single
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket

@Single
class ReachabilityChecker {

    suspend fun isReachable(host: String, ports: List<Int>, timeoutMs: Int = 1_000): Boolean =
        withContext(Dispatchers.IO) {
            ports.any { port ->
                try {
                    Socket().use { it.connect(InetSocketAddress(host, port), timeoutMs) }
                    true
                } catch (e: IOException) {
                    false
                }
            }
        }
}
