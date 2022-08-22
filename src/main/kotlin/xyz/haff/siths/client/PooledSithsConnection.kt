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
    override val resource: StandaloneSithsConnection,
    override val pool: SithsPool,
    override val identifier: String,
    override var status: PoolStatus = PoolStatus.FREE,
) : PooledResource<SithsConnection>, SithsConnection {

    companion object {
        suspend fun open(
            pool: SithsPool,
            host: String = "localhost",
            port: Int = 6379,
            user: String? = null,
            password: String? = null,
            name: String = UUID.randomUUID().toString(),
            status: PoolStatus = PoolStatus.FREE,
        )
            = PooledSithsConnection(
                resource = StandaloneSithsConnection.open(host = host, port = port, name = name, user = user, password = password),
                pool = pool,
                identifier = name,
                status = status,
            )
    }

    override suspend fun runCommand(command: RedisCommand): RespType<*> = try {
        resource.runCommand(command)
    } catch (e: Exception) {
        handleChannelException(e)
    }

    override suspend fun runPipeline(pipeline: RedisPipeline): List<RespType<*>> = try {
        resource.runPipeline(pipeline)
    } catch (e: IOException) {
        handleChannelException(e)
    }

    override fun close() {
        pool.release(identifier)
    }

    private fun handleChannelException(e: Exception): Nothing = when(e) {
        is ClosedReceiveChannelException, is ClosedSendChannelException -> {
            // Connection is broken, so we notify the pool to discard it and maybe create a new one that works
            resource.close()
            pool.remove(identifier)
            throw RedisBrokenConnectionException(e)
        }
        else -> throw e
    }
}