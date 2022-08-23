package xyz.haff.siths.client

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class SithsConnectionPool(
    private val redisConnection: RedisConnection,
    private val maxConnections: Int = 10,
    private val acquireTimeout: Duration = 10.seconds
) : Pool<SithsConnection, PooledSithsConnection> by DefaultPool(
    acquireTimeout = acquireTimeout,
    maxResources = maxConnections,
    createNewResource = { pool ->
        val connection = StandaloneSithsConnection.open(redisConnection)
        PooledSithsConnection(
            resource = connection,
            pool = pool,
            identifier = connection.identifier,
        )
    },
)