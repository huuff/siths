package xyz.haff.siths.client

import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.channels.ClosedSendChannelException
import xyz.haff.siths.common.RedisBrokenConnectionException

/**
 * This Siths connection is pooled. Actually, just a decorator around a StandaloneSithsConnection, that instead of
 * closing the underlying socket, only releases it to the pool.
 */
class PooledSithsConnection(
    override val resource: StandaloneSithsConnection,
    override val pool: Pool<SithsConnection, PooledSithsConnection>,
    override val identifier: String = resource.identifier,
    override var status: PoolStatus = PoolStatus.FREE,
) : PooledResource<SithsConnection>, SithsConnection {

    override suspend fun runCommand(command: RedisCommand): RespType<*> = try {
        resource.runCommand(command)
    } catch (e: Exception) {
        handleChannelException(e)
    }

    override suspend fun runPipeline(pipeline: RedisPipeline): List<RespType<*>> = try {
        resource.runPipeline(pipeline)
    } catch (e: Exception) {
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