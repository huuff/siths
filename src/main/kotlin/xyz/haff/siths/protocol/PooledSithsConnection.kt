package xyz.haff.siths.protocol

import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.channels.ClosedSendChannelException
import xyz.haff.siths.pipelining.RedisPipeline
import xyz.haff.siths.command.RedisCommand
import xyz.haff.siths.common.RedisBrokenConnectionException
import xyz.haff.siths.pooling.Pool
import xyz.haff.siths.pooling.PoolStatus
import xyz.haff.siths.pooling.PooledResource
import java.io.IOException

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
    } catch (e: RedisBrokenConnectionException) {
        handleChannelException(e)
    }

    override suspend fun runPipeline(pipeline: RedisPipeline): List<RespType<*>> = try {
        resource.runPipeline(pipeline)
    } catch (e: RedisBrokenConnectionException) {
        handleChannelException(e)
    }

    override fun close() {
        pool.release(identifier)
    }

    private fun handleChannelException(e: RedisBrokenConnectionException): Nothing {
        // Connection is broken, so we notify the pool to discard it and maybe create a new one that works
        resource.close()
        status = PoolStatus.BROKEN
        throw e
    }
}