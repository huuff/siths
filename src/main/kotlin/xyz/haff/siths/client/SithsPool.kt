package xyz.haff.siths.client

import kotlinx.coroutines.delay
import xyz.haff.siths.common.RedisPoolOutOfConnectionsException
import kotlin.time.Duration
import java.util.*
import kotlin.time.Duration.Companion.seconds

// TODO: Make it thread-safe... @Synchronized?
class SithsPool(
    private val host: String = "localhost",
    private val port: Int = 6379,
    private val user: String? = null,
    private val password: String? = null,
    private val maxConnections: Int = 10,
    private val acquireTimeout: Duration = 10.seconds
) {
    private val freeConnections = Collections.synchronizedList(mutableListOf<SithsConnection>())
    private val usedConnections = Collections.synchronizedList(mutableListOf<SithsConnection>())

    internal val totalConnections get() = freeConnections.size + usedConnections.size

    suspend fun getConnection(): SithsConnection {
        val deadline = System.currentTimeMillis() + acquireTimeout.inWholeMilliseconds

        while (System.currentTimeMillis() < deadline) {
            if (freeConnections.isNotEmpty()) {
                val connection = freeConnections[0]
                freeConnections -= connection
                usedConnections += connection
                return connection
            } else {
                if (totalConnections < maxConnections) {
                    val connection = PooledSithsConnection.open(pool = this, host = host, port = port, user = user, password = password)
                    usedConnections += connection
                    return connection
                } else {
                    delay(10)
                    continue
                }
            }
        }

        throw RedisPoolOutOfConnectionsException()
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