package xyz.haff.siths.client

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.util.*
import io.ktor.utils.io.*
import io.ktor.utils.io.charsets.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.util.*
import kotlin.text.Charsets
import kotlin.text.toByteArray

// TODO: Somewhere else
private val firstWordRegex = Regex("""\w+""")

private val CRLF = "\r\n".toByteArray(Charsets.UTF_8)
private val PLUS = "+".toByteArray(Charsets.UTF_8)[0]
private val MINUS = "-".toByteArray(Charsets.UTF_8)[0]
private val COLON = ":".toByteArray(Charsets.UTF_8)[0]
private val DOLLAR = "$".toByteArray(Charsets.UTF_8)[0]


/**
 * This connection is "standalone", which means that it isn't associated to any pool. (Or rather, if it is, it has no
 * knowledge thereof!)
 */
class StandaloneSithsConnection private constructor(
    private val selectorManager: SelectorManager,
    private val socket: Socket,
    override val name: String,
): SithsConnection {
    private val sendChannel = socket.openWriteChannel(autoFlush = false)
    private val receiveChannel = socket.openReadChannel()

    companion object {
        suspend fun open(
            host: String = "localhost",
            port: Int = 6379,
            name: String = UUID.randomUUID().toString(),
        ): StandaloneSithsConnection {
            val selectorManager = SelectorManager(Dispatchers.IO)

            return StandaloneSithsConnection(
                selectorManager = selectorManager,
                socket = aSocket(selectorManager).tcp().connect(host, port),
                name = name,
            ).also {
                val response = it.runCommand(RedisCommand("CLIENT", "SETNAME", name))
                if (!response.isOk()) {
                    throw UnexpectedRespResponse(response) // Maybe a better error?
                }
            }
        }
    }

    // TODO: Some way (through slf4j or something) of logging all responses if DEBUG is enabled
    // TODO: Optimize reading! Maybe I could read ByteBuffers and use the lengths to know exactly how much to consume,
    // also skipping these here? I don't know, I must investigate it further
    private suspend fun readSingleResp(): RespType<*> {
        receiveChannel.awaitContent()

        return when (val responseType = receiveChannel.readByte()) {
            PLUS -> RespSimpleString(receiveChannel.readUTF8Line()!!)
            MINUS -> {
                // TODO: `firstWord` function in koy
                val errorMessage = receiveChannel.readUTF8Line()!!
                val errorType = firstWordRegex.find(errorMessage)!!.value
                RespError(type = errorType, value = errorMessage.drop(errorType.length))
            }
            COLON -> RespInteger(receiveChannel.readUTF8Line()!!.toLong())
            DOLLAR -> {
                val length = receiveChannel.readUTF8Line()!!.toInt()
                if (length == -1)
                    return RespNullResponse
                else
                    return RespBulkString(receiveChannel.readUTF8Line(length + 2)!!) // +2 for CLRF
            }
            else -> throw RuntimeException("Unknown response type: '$responseType'")
        }
    }

    override suspend fun runCommand(command: RedisCommand): RespType<*> {
        sendChannel.writeFully(command.toResp().toByteArray(Charsets.UTF_8)) // TODO: Doesn't writeUtf8String work?
        sendChannel.flush()

        // TODO: Arrays
        return readSingleResp()
    }

    override suspend fun runPipeline(pipeline: RedisPipeline): List<RespType<*>> {
        sendChannel.writeFully(pipeline.toResp().toByteArray(Charsets.UTF_8))
        sendChannel.flush()

        return (1..pipeline.commands.size).map { readSingleResp() }
    }

    // TODO: Do something with this (mark the connection as closed?)
    override fun close() {
        socket.close()
        selectorManager.close()
    }

}