package xyz.haff.siths.client

import io.ktor.utils.io.errors.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.channels.ClosedSendChannelException
import xyz.haff.siths.common.RedisBrokenConnectionException
import java.util.*



/**
 * This Siths connection is pooled. Actually, just a decorator around a StandaloneSithsConnection, that instead of
 * closing the underlying socket, only releases it to the pool.
 */
class PooledSithsConnection private constructor(
    private val connection: StandaloneSithsConnection,
    private val pool: SithsPool,
    override val name: String,
) : SithsConnection {

    companion object {
        suspend fun open(
            pool: SithsPool,
            host: String = "localhost",
            port: Int = 6379,
            name: String = UUID.randomUUID().toString())
            = PooledSithsConnection(
                connection = StandaloneSithsConnection.open(host = host, port = port, name = name),
                pool = pool,
                name = name,
            )
    }

    override suspend fun runCommand(command: RedisCommand): RespType<*> = try {
        connection.runCommand(command)
    } catch (e: Exception) {
        handleChannelException(e)
    }

    override suspend fun runPipeline(pipeline: RedisPipeline): List<RespType<*>> = try {
        connection.runPipeline(pipeline)
    } catch (e: IOException) {
        handleChannelException(e)
    }

    override fun close() {
        pool.releaseConnection(this)
    }

    private fun handleChannelException(e: Exception): Nothing = when(e) {
        is ClosedReceiveChannelException, is ClosedSendChannelException -> {
            // Connection is broken, so we notify the pool to discard it and maybe create a new one that works
            connection.close()
            pool.removeConnection(this)
            throw RedisBrokenConnectionException(e)
        }
        else -> throw e
    }
}