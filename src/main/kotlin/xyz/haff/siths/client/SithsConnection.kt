package xyz.haff.siths.client

/**
 * A connection to a Redis database. It comprises two operations:
 *  * Sending a command and getting an abstract representation of the RESP response in return
 *  * Closing the connection
 */
interface SithsConnection: AutoCloseable {
    suspend fun command(command: RedisCommand): RespType<*>
    override fun close(): Unit
}