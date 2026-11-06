package com.chs.yourwol.data.network

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.common.Buffer
import net.schmizz.sshj.transport.verification.HostKeyVerifier
import org.koin.core.annotation.Single
import java.security.MessageDigest
import java.security.PublicKey
import java.util.Base64
import java.util.concurrent.TimeUnit

@Single
class SshCommandExecutor {

    sealed interface Result {
        data class Success(val fingerprint: String, val exitStatus: Int?) : Result
        data class HostKeyMismatch(val expected: String, val actual: String) : Result
        data class Failure(val cause: Throwable) : Result
    }

    suspend fun execute(
        host: String,
        port: Int,
        user: String,
        password: String,
        command: String,
        knownFingerprint: String?,
    ): Result = withContext(Dispatchers.IO) {
        var seen: String? = null
        try {
            SSHClient().use { ssh ->
                ssh.addHostKeyVerifier(object : HostKeyVerifier {
                    override fun verify(hostname: String, port: Int, key: PublicKey): Boolean {
                        val fp = sha256Fingerprint(key).also { seen = it }
                        return knownFingerprint == null || knownFingerprint == fp
                    }
                    override fun findExistingAlgorithms(hostname: String, port: Int): List<String> =
                        emptyList()
                })
                ssh.connectTimeout = 5_000
                ssh.timeout = 10_000
                ssh.connect(host, port)
                ssh.authPassword(user, password)

                ssh.startSession().use { session ->
                    val cmd = session.exec(command)
                    cmd.join(5, TimeUnit.SECONDS)
                    Result.Success(seen!!, cmd.exitStatus)
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (t: Throwable) {
            val actual = seen
            if (knownFingerprint != null && actual != null && actual != knownFingerprint) {
                Result.HostKeyMismatch(knownFingerprint, actual)
            } else {
                Result.Failure(t)
            }
        }
    }

    private fun sha256Fingerprint(key: PublicKey): String {
        val blob = Buffer.PlainBuffer().putPublicKey(key).compactData
        val digest = MessageDigest.getInstance("SHA-256").digest(blob)
        return "SHA256:" + Base64.getEncoder().withoutPadding().encodeToString(digest)
    }
}
