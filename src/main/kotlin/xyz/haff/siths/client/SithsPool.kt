package xyz.haff.siths.client

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class SithsPool(
    private val host: String = "localhost",
    private val port: Int = 6379,
    private val user: String? = null,
    private val password: String? = null,
    private val maxConnections: Int = 10,
    private val acquireTimeout: Duration = 10.seconds
) : Pool<SithsConnection, PooledSithsConnection> by DefaultPool(
    acquireTimeout = acquireTimeout,
    maxResources = maxConnections,
    createNewResource = { pool ->
        val connection = StandaloneSithsConnection.open(host, port, user, password)
        PooledSithsConnection(
            resource = connection,
            pool = pool,
            identifier = connection.identifier,
        )
    },
)