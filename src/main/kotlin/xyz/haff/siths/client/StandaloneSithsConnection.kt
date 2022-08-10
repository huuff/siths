package xyz.haff.siths.client

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers

// TODO: Somewhere else
private val firstWordRegex = Regex("""\w+""")

/**
 * This connection is "standalone", which means that it isn't associated to any pool. (Or rather, if it is, it has no
 * knowledge thereof!)
 */
class StandaloneSithsConnection private constructor(
    private val selectorManager: SelectorManager,
    private val socket: Socket,
): SithsConnection {
    private val sendChannel = socket.openWriteChannel(autoFlush = true)
    private val receiveChannel = socket.openReadChannel()

    companion object {
        suspend fun open(host: String = "localhost", port: Int = 6379): StandaloneSithsConnection {
            val selectorManager = SelectorManager(Dispatchers.IO)

            return StandaloneSithsConnection(
                selectorManager = selectorManager,
                socket = aSocket(selectorManager).tcp().connect(host, port)
            )
        }
    }

    // TODO: Some way (through slf4j or something) of logging all responses if DEBUG is enabled
    private suspend fun readLine(length: Int = Int.MAX_VALUE): String {
        receiveChannel.awaitContent()
        return receiveChannel.readUTF8Line(length)!!
    }

    override suspend fun command(command: String): RespType<*> {
        sendChannel.writeStringUtf8("$command\r\n")

        val firstResponse = readLine()

        // TODO: Arrays
        return when (firstResponse[0]) {
            '+' -> RespSimpleString(firstResponse.drop(1))
            '-' -> {
                // TODO: `firstWord` function in koy
                val errorMessage = firstResponse.drop(1)
                val errorType = firstWordRegex.find(errorMessage)!!.value
                RespError(type = errorType, value = errorMessage.drop(errorType.length))
            }
            ':' -> RespInteger(firstResponse.drop(1).toInt())
            '$' -> {
                val length = firstResponse.drop(1).toInt()
                if (length == -1) {
                    return RespNullResponse
                } else {
                    return RespBulkString(readLine(length + 2)) // String length plus carriage return and newline
                }
            }
            else -> throw RuntimeException("Incapable of deciding the resp type of $firstResponse")
        }
    }

    // TODO: Do something with this (mark the connection as closed?)
    override fun close() {
        socket.close()
        selectorManager.close()
    }

}