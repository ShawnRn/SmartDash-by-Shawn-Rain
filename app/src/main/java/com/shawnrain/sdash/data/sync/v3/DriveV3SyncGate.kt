package com.shawnrain.sdash.data.sync.v3

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal object DriveV3SyncGate {
    private val mutex = Mutex()

    suspend fun <T> withLock(block: suspend () -> T): T =
        mutex.withLock { block() }
}
