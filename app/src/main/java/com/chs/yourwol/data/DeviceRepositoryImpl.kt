package com.chs.yourwol.data

import com.chs.yourwol.data.db.DeviceDao
import com.chs.yourwol.domain.PowerState
import com.chs.yourwol.domain.RemoteDeviceInfo
import com.chs.yourwol.data.db.DeviceEntity
import com.chs.yourwol.domain.DeviceRepository
import com.chs.yourwol.domain.PowerOffResult
import com.chs.yourwol.data.network.MagicPacketSender
import com.chs.yourwol.data.network.NetworkInfoProvider
import com.chs.yourwol.data.network.ReachabilityChecker
import com.chs.yourwol.data.network.SshCommandExecutor
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Single
import java.net.InetAddress

@Single
class DeviceRepositoryImpl(
    private val dao: DeviceDao,
    private val cipher: CredentialCipher,
    private val networkInfo: NetworkInfoProvider,
    private val magicPacket: MagicPacketSender,
    private val reachability: ReachabilityChecker,
    private val ssh: SshCommandExecutor,
) : DeviceRepository {

    override fun observeDevices(): Flow<List<RemoteDeviceInfo>> = dao.observeAll().map { it.map { it.toDomain() } }

    override suspend fun getDevice(id: Long): RemoteDeviceInfo? = dao.getById(id)?.toDomain()

    override suspend fun saveDevice(device: RemoteDeviceInfo, password: String?): Long {
        val existing = if (device.id != 0L) dao.getById(device.id) else null
        val encrypted = when {
            !password.isNullOrEmpty() -> cipher.encrypt(password)
            else -> existing?.encryptedPassword
        }

        val fingerprint = existing?.hostKeyFingerprint
            ?.takeIf { existing.ipAddress == device.ipAddress && existing.sshPort == device.sshPort }

        val entity = DeviceEntity(
            id = device.id,
            name = device.name.trim(),
            macAddress = device.macAddress.trim().uppercase().replace("-", ":"),
            ipAddress = device.ipAddress.trim(),
            broadcastAddress = device.broadcastAddress?.trim()?.ifEmpty { null },
            wolPort = device.wolPort,
            sshPort = device.sshPort,
            sshUser = device.sshUser.trim(),
            encryptedPassword = encrypted,
            hostKeyFingerprint = fingerprint,
        )
        val rowId = dao.upsert(entity)
        return if (device.id != 0L) device.id else rowId
    }

    override suspend fun deleteDevice(id: Long) = dao.delete(id)

    override suspend fun wake(device: RemoteDeviceInfo): Result<Unit> = runCatchingNonCancel {
        val target = withContext(Dispatchers.IO) {
            device.broadcastAddress?.let(InetAddress::getByName)
                ?: networkInfo.currentBroadcastAddress()
        } ?: error("네트워크 정보를 가져올 수 없습니다. Wi-Fi 연결을 확인하세요.")
        magicPacket.send(device.macAddress, target, device.wolPort)
    }

    override suspend fun powerOff(device: RemoteDeviceInfo, reboot: Boolean): PowerOffResult {
        val entity = dao.getById(device.id) ?: return PowerOffResult.Failure(NoSuchElementException())
        val encrypted = entity.encryptedPassword ?: return PowerOffResult.NoCredential
        val password = try {
            cipher.decrypt(encrypted)
        } catch (e: Exception) {
            return PowerOffResult.NoCredential
        }
//        val command = if (reboot) device.osType.rebootCommand else device.osType.shutdownCommand

        val r = ssh.execute(
            host = device.ipAddress,
            port = device.sshPort,
            user = device.sshUser,
            password = password,
            command = "",
            knownFingerprint = entity.hostKeyFingerprint,
        )

        return when (r) {
            is SshCommandExecutor.Result.Success -> {
                if (entity.hostKeyFingerprint == null) dao.updateHostKey(device.id, r.fingerprint)
                if (r.exitStatus == null || r.exitStatus == 0) PowerOffResult.Success
                else PowerOffResult.Failure(IllegalStateException("명령 실패 (exit=${r.exitStatus})"))
            }
            is SshCommandExecutor.Result.HostKeyMismatch ->
                PowerOffResult.HostKeyMismatch(r.expected, r.actual)
            is SshCommandExecutor.Result.Failure -> PowerOffResult.Failure(r.cause)
        }
    }

    override suspend fun checkPower(device: RemoteDeviceInfo): PowerState {
        val ports = buildList {
            add(device.sshPort)
            add(445)
            add(3389)
        }.distinct()
        return if (reachability.isReachable(device.ipAddress, ports)) PowerState.ONLINE
        else PowerState.OFFLINE
    }

    override suspend fun resetHostKey(id: Long) = dao.updateHostKey(id, null)

    private inline fun <T> runCatchingNonCancel(block: () -> T): Result<T> =
        try {
            Result.success(block())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            Result.failure(e)
        }
}
