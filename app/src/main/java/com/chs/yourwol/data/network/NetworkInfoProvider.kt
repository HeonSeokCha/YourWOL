package com.chs.yourwol.data.network

import android.content.Context
import android.net.ConnectivityManager
import org.koin.core.annotation.Single
import java.net.Inet4Address
import java.net.InetAddress

@Single
class NetworkInfoProvider(
    private val context: Context,
) {
    private val cm get() = context.getSystemService(ConnectivityManager::class.java)

    fun currentBroadcastAddress(): InetAddress? {
        val lp = cm.getLinkProperties(cm.activeNetwork) ?: return null
        val link = lp.linkAddresses.firstOrNull { it.address is Inet4Address } ?: return null

        return InetAddress.getByAddress(
            SubnetUtils.broadcastOf(link.address.address, link.prefixLength)
        )
    }
}
