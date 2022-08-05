package xyz.haff.siths.client

import kotlinx.coroutines.runBlocking
import java.time.Duration
import java.time.LocalDateTime
import java.util.*

class SithPool(
    private val host: String = "localhost",
    private val port: Int = 6379,
    private val maxConnections: Int = 10,
    private val acquireTimeout: Duration = Duration.ofSeconds(10)
) {
    private val freeConnections = Collections.synchronizedList(mutableListOf<SithConnection>())
    private val usedConnections = Collections.synchronizedList(mutableListOf<SithConnection>())

    fun getConnection(): SithConnection {
        val deadline = LocalDateTime.now() + acquireTimeout

        while (LocalDateTime.now() < deadline) {
            if (freeConnections.isNotEmpty()) {
                val connection = freeConnections[0]
                freeConnections -= connection
                usedConnections += connection
                return connection
            } else {
                if (freeConnections.size + usedConnections.size < maxConnections) {
                    val connection = runBlocking { SithConnection.open(host, port) }
                    usedConnections += connection
                    return connection
                } else {
                    Thread.sleep(10)
                    continue
                }
            }
        }

        // TODO: A specific exception
        throw RuntimeException("Unable to acquire connection! All busy")
    }

    fun releaseConnection(connection: SithConnection) {
        usedConnections -= connection
        freeConnections += connection
    }

    inline fun <T> pooled(f: SithConnection.() -> T): T {
        val connection = getConnection()
        return try {
            connection.f()
        } finally {
            releaseConnection(connection)
        }
    }

}