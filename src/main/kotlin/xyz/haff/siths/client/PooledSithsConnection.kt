package xyz.haff.siths.client

import io.ktor.utils.io.errors.*

/**
 * This Siths connection is pooled. Actually, just a decorator around a StandaloneSithsConnection, that instead of
 * closing the underlying socket, only releases it to the pool.
 */
class PooledSithsConnection(
    private val connection: StandaloneSithsConnection,
    private val pool: SithsPool,
) : SithsConnection {

    companion object {
        suspend fun open( pool: SithsPool, host: String = "localhost", port: Int = 6379)
            = PooledSithsConnection(
                connection = StandaloneSithsConnection.open(host = host, port = port),
                pool = pool,
            )
    }

    override suspend fun runCommand(command: RedisCommand): RespType<*> = try {
        connection.runCommand(command)
    } catch (e: IOException) {
        // Connection is broken, so we notify the pool to discard it and maybe create a new one that works
        connection.close()
        pool.removeConnection(this)
        throw BrokenRedisConnectionException(e)
    }

    override suspend fun runPipeline(pipeline: RedisPipeline): List<RespType<*>> = try {
        connection.runPipeline(pipeline)
    } catch (e: IOException) {
        connection.close()
        pool.removeConnection(this)
        throw BrokenRedisConnectionException(e)
    }

    override fun close() {
        pool.releaseConnection(this)
    }
}