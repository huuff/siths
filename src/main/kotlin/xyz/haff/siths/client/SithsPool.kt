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

    suspend fun getConnection(): SithsConnection {
        val deadline = System.currentTimeMillis() + acquireTimeout.toMillis()

        while (System.currentTimeMillis() < deadline) {
            if (freeConnections.isNotEmpty()) {
                val connection = freeConnections[0]
                freeConnections -= connection
                usedConnections += connection
                return connection
            } else {
                if (freeConnections.size + usedConnections.size < maxConnections) {
                    val connection = SithsConnection.open(host, port)
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
        usedConnections -= connection
        freeConnections += connection
    }

    suspend inline fun <T> pooled(f: SithsConnection.() -> T): T {
        val connection = getConnection()
        return try {
            connection.f()
        } finally {
            releaseConnection(connection)
        }
    }

}