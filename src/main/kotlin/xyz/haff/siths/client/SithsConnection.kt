package xyz.haff.siths.client

/**
 * A connection to a Redis database. It comprises two operations:
 *  * Sending a command and getting an abstract representation of the RESP response in return
 *  * Closing the connection
 */
interface SithsConnection {
    suspend fun command(command: String): RespType<*>
    fun close(): Unit
}