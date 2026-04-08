package com.shawnrain.sdash.data.migration

import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.Inet4Address
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.Socket
import java.util.Collections
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class LanBackupOffer(
    val host: String,
    val port: Int,
    val code: String
)

class LanBackupTransfer(
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    private var serverSocket: ServerSocket? = null
    private var serverLoopJob: Job? = null

    suspend fun start(
        scope: CoroutineScope,
        code: String,
        onRestoreApplied: (() -> Unit)? = null,
        payloadProvider: suspend () -> String
    ): LanBackupOffer {
        stop()
        val socket = withContext(ioDispatcher) { ServerSocket(0) }
        serverSocket = socket
        val host = resolveLocalIpv4Address() ?: "127.0.0.1"
        serverLoopJob = scope.launch(ioDispatcher) {
            while (isActive && !socket.isClosed) {
                val client = runCatching { socket.accept() }.getOrNull() ?: break
                val shouldStop = client.use { serveClient(it, code, payloadProvider, onRestoreApplied) }
                if (shouldStop) {
                    runCatching { socket.close() }
                    break
                }
            }
            if (serverSocket === socket) {
                serverSocket = null
            }
        }
        return LanBackupOffer(host = host, port = socket.localPort, code = code)
    }

    suspend fun stop() {
        val socket = serverSocket
        serverSocket = null
        // Close server socket first to unblock a pending accept() immediately.
        withContext(ioDispatcher) {
            runCatching { socket?.close() }
        }
        val loopJob = serverLoopJob
        serverLoopJob = null
        if (loopJob != null) {
            loopJob.cancel()
            loopJob.join()
        }
    }

    suspend fun pull(
        host: String,
        port: Int,
        code: String,
        timeoutMs: Int = 8000
    ): String = withContext(ioDispatcher) {
        val socket = Socket()
        socket.connect(InetSocketAddress(host, port), timeoutMs)
        socket.soTimeout = timeoutMs
        socket.use { client ->
            val input = DataInputStream(BufferedInputStream(client.getInputStream()))
            val output = DataOutputStream(BufferedOutputStream(client.getOutputStream()))
            output.writeUTF(MAGIC)
            output.writeUTF(code)
            output.flush()

            val ok = input.readBoolean()
            if (!ok) {
                val reason = runCatching { input.readUTF() }.getOrElse { "unknown" }
                throw IllegalStateException("LAN transfer rejected: $reason")
            }
            val size = input.readInt()
            if (size <= 0 || size > MAX_BACKUP_BYTES) {
                throw IllegalStateException("Invalid backup size: $size")
            }
            val bytes = ByteArray(size)
            input.readFully(bytes)
            bytes.toString(Charsets.UTF_8)
        }
    }

    suspend fun notifyRestoreApplied(
        host: String,
        port: Int,
        code: String,
        timeoutMs: Int = 5000
    ): Boolean = withContext(ioDispatcher) {
        val socket = Socket()
        socket.connect(InetSocketAddress(host, port), timeoutMs)
        socket.soTimeout = timeoutMs
        socket.use { client ->
            val input = DataInputStream(BufferedInputStream(client.getInputStream()))
            val output = DataOutputStream(BufferedOutputStream(client.getOutputStream()))
            output.writeUTF(ACK_MAGIC)
            output.writeUTF(code)
            output.flush()
            runCatching { input.readBoolean() }.getOrDefault(false)
        }
    }

    private suspend fun serveClient(
        socket: Socket,
        expectedCode: String,
        payloadProvider: suspend () -> String,
        onRestoreApplied: (() -> Unit)? = null
    ): Boolean {
        val input = DataInputStream(BufferedInputStream(socket.getInputStream()))
        val output = DataOutputStream(BufferedOutputStream(socket.getOutputStream()))
        val magic = runCatching { input.readUTF() }.getOrElse { return false }
        val code = runCatching { input.readUTF() }.getOrElse { return false }

        if (magic == ACK_MAGIC) {
            val ok = code == expectedCode
            output.writeBoolean(ok)
            if (!ok) {
                output.writeUTF("AUTH_FAILED")
            }
            output.flush()
            if (ok) {
                onRestoreApplied?.invoke()
                return true
            }
            return false
        }

        if (magic != MAGIC || code != expectedCode) {
            output.writeBoolean(false)
            output.writeUTF("AUTH_FAILED")
            output.flush()
            return false
        }
        val payload = payloadProvider().toByteArray(Charsets.UTF_8)
        output.writeBoolean(true)
        output.writeInt(payload.size)
        output.write(payload)
        output.flush()
        return false
    }

    private fun resolveLocalIpv4Address(): String? {
        val interfaces = runCatching { Collections.list(NetworkInterface.getNetworkInterfaces()) }
            .getOrDefault(emptyList())
        for (iface in interfaces) {
            if (!iface.isUp || iface.isLoopback) continue
            val addresses = Collections.list(iface.inetAddresses)
            for (address in addresses) {
                if (address is Inet4Address && !address.isLoopbackAddress) {
                    return address.hostAddress
                }
            }
        }
        return null
    }

    companion object {
        private const val MAGIC = "HABE_LAN_BACKUP_V1"
        private const val ACK_MAGIC = "HABE_LAN_BACKUP_ACK_V1"
        private const val MAX_BACKUP_BYTES = 24 * 1024 * 1024
    }
}
