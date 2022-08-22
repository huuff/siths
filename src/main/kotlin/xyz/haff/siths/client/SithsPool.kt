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
): Pool<SithsConnection> {
    private val connections = mutableMapOf<String, PooledSithsConnection>()

    internal val openConnections get() = connections.size

    override suspend fun get(): PooledSithsConnection {
        val deadline = System.currentTimeMillis() + acquireTimeout.inWholeMilliseconds

        while (System.currentTimeMillis() < deadline) {
            if (connections.values.any { it.status == PoolStatus.FREE }) {
                val connection = connections.values.find { it.status == PoolStatus.FREE }!!
                connection.status = PoolStatus.BUSY
                return connection
            } else {
                if (openConnections < maxConnections) {
                    val connection = PooledSithsConnection.open(pool = this, host = host, port = port, user = user, password = password, status = PoolStatus.BUSY)
                    connections[connection.identifier] = connection
                    return connection
                } else {
                    delay(10)
                    continue
                }
            }
        }

        throw RedisPoolOutOfConnectionsException()
    }

    override fun release(resourceIdentifier: String) {
        connections[resourceIdentifier]?.status = PoolStatus.FREE
    }

    /**
     * Remove connection from the pool, because p.e. it is broken. This allows the pool to heal by creating a new one.
     */
    override fun remove(resourceIdentifier: String) {
        connections -= resourceIdentifier
    }
}