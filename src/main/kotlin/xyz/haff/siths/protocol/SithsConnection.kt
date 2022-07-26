package xyz.haff.siths.protocol

import xyz.haff.siths.pipelining.RedisPipeline
import xyz.haff.siths.command.RedisCommand

/**
 * A connection to a Redis database. It comprises two operations:
 *  * Sending a command and getting an abstract representation of the RESP response in return
 *  * Closing the connection
 */
interface SithsConnection: AutoCloseable {
    val identifier: String

    suspend fun runCommand(command: RedisCommand): RespType<*>
    suspend fun runPipeline(pipeline: RedisPipeline): List<RespType<*>>
    override fun close()
}