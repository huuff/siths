package xyz.haff.siths.client

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.Dispatchers
import java.nio.ByteBuffer
import kotlin.text.toByteArray

// TODO: Somewhere else
private val firstWordRegex = Regex("""\w+""")
private val CRLF = "\r\n".toByteArray(Charsets.UTF_8)

/**
 * This connection is "standalone", which means that it isn't associated to any pool. (Or rather, if it is, it has no
 * knowledge thereof!)
 */
class StandaloneSithsConnection private constructor(
    private val selectorManager: SelectorManager,
    private val socket: Socket,
): SithsConnection {
    private val sendChannel = socket.openWriteChannel(autoFlush = false)
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
    // TODO: Optimize reading! Maybe I could read ByteBuffers and use the lengths to know exactly how much to consume,
    // also skipping these here? I don't know, I must investigate it further
    private suspend fun readLine(length: Int = Int.MAX_VALUE): String {
        receiveChannel.awaitContent()
        return receiveChannel.readUTF8Line(length)!!
    }

    // TODO: Maybe I should send commands as arrays of parts of the command, and prepend each one with its length,
    // for example, when interacting directly with Redis through TCP, a SET key value would be
    // $3\r\n
    // SET\r\n
    // $3\r\n
    // key\r\n
    // $5\r\n
    // value\r\n
    // Of course this is much more data! but maybe specifying the exact amount of data will, for example, allow me to
    // skip the escaping of strings, which is kind of a headache
    override suspend fun command(command: String): RespType<*> {
        val commandBytes = command.toByteArray(Charsets.UTF_8)
        val message = ByteBuffer.allocateDirect(commandBytes.size + CRLF.size).apply {
            put(commandBytes)
            put(CRLF)
        }
        sendChannel.writeFully(message.flip())
        sendChannel.flush()

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
            ':' -> RespInteger(firstResponse.drop(1).toLong())
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