package xyz.haff.siths.client

import kotlinx.coroutines.delay
import java.time.Duration
import java.time.LocalDateTime
import java.util.*

class SithsPool(
    private val host: String = "localhost",
    private val port: Int = 6379,
    private val maxConnections: Int = 10,
    private val acquireTimeout: Duration = Duration.ofSeconds(10)
) {
    private val freeConnections = Collections.synchronizedList(mutableListOf<SithsConnection>())
    private val usedConnections = Collections.synchronizedList(mutableListOf<SithsConnection>())

    internal val totalConnections get() = freeConnections.size + usedConnections.size

    suspend fun getConnection(): SithsConnection {
        val deadline = System.currentTimeMillis() + acquireTimeout.toMillis()

        while (System.currentTimeMillis() < deadline) {
            if (freeConnections.isNotEmpty()) {
                val connection = freeConnections[0]
                freeConnections -= connection
                usedConnections += connection
                return connection
            } else {
                if (totalConnections < maxConnections) {
                    val connection = PooledSithsConnection.open(this, host, port)
                    usedConnections += connection
                    return connection
                } else {
                    delay(10)
                    continue
                }
            }
        }

        // TODO: A specific exception
        throw RuntimeException("Unable to acquire connection! All busy")
    }

    fun releaseConnection(connection: SithsConnection) {
        if (connection in usedConnections) {
            usedConnections -= connection
            freeConnections += connection
        }
    }

    /**
     * Remove connection from the pool, because p.e. it is broken. This allows the pool to heal by creating a new one.
     */
    fun removeConnection(connection: SithsConnection) {
        usedConnections -= connection
        freeConnections -= connection
    }
}