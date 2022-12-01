package xyz.haff.siths.protocol

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.channels.ClosedSendChannelException
import xyz.haff.siths.command.RedisCommand
import xyz.haff.siths.common.RedisAuthException
import xyz.haff.siths.common.RedisBrokenConnectionException
import xyz.haff.siths.common.RedisUnexpectedRespResponseException
import xyz.haff.siths.pipelining.RedisPipeline
import java.io.IOException
import java.net.SocketException
import java.util.*
import kotlin.text.Charsets
import kotlin.text.toByteArray


private fun Exception.isSocketException(): Boolean {
    return (
            this is ClosedReceiveChannelException
                    || this is ClosedSendChannelException
                    || (this is IOException && this.message == "Broken pipe")
                    || this is SocketException
            )
}

/**
 * This connection is "standalone", which means that it isn't associated to any pool. (Or rather, if it is, it has no
 * knowledge thereof!)
 */
class StandaloneSithsConnection private constructor(
    private val selectorManager: SelectorManager,
    private val socket: Socket,
    override val identifier: String,
) : SithsConnection {
    private val sendChannel = socket.openWriteChannel(autoFlush = false)
    private val receiveChannel = socket.openReadChannel()

    companion object {
        suspend fun open(
            redisConnection: RedisConnection,
            name: String = UUID.randomUUID().toString(),
        ): StandaloneSithsConnection {
            val selectorManager = SelectorManager(Dispatchers.IO)

            return StandaloneSithsConnection(
                selectorManager = selectorManager,
                socket = aSocket(selectorManager).tcp().connect(redisConnection.host, redisConnection.port),
                identifier = name,
            ).also {
                if (redisConnection.password != null) {
                    val response = it.runCommand(RedisCommand("AUTH", redisConnection.user, redisConnection.password))
                    if (!response.isOk()) {
                        throw RedisAuthException(response)
                    }
                } else if (redisConnection.user != null) {
                    throw IllegalArgumentException("Can't create a connection with an username but without a password")
                }

                val response = it.runCommand(RedisCommand("CLIENT", "SETNAME", name))
                if (!response.isOk()) {
                    if (response is RespError) {
                        response.throwAsException()
                    } else {
                        throw RedisUnexpectedRespResponseException(response)
                    } // Maybe a better error?
                }
            }
        }
    }

    // TODO: Some way (through slf4j or something) of logging all responses if DEBUG is enabled
    private suspend fun readResponse(): RespType<*> {
        receiveChannel.awaitContent()

        return RespParser(receiveChannel).parse()

    }

    override suspend fun runCommand(command: RedisCommand): RespType<*> {
        try {
            sendChannel.writeStringUtf8(command.toResp())
            sendChannel.flush()

            return readResponse()
        } catch (e: Exception) {
            if (e.isSocketException()) {
                throw RedisBrokenConnectionException(command, e)
            } else {
                throw e
            }
        }
    }

    override suspend fun runPipeline(pipeline: RedisPipeline): List<RespType<*>> {
        try {
            sendChannel.writeFully(pipeline.toResp().toByteArray(Charsets.UTF_8))
            sendChannel.flush()

            return (1..pipeline.commands.size).map { readResponse() }
        } catch (e: Exception) {
            if (e.isSocketException()) {
                throw RedisBrokenConnectionException(pipeline, e)
            } else {
                throw e
            }
        }
    }

    override fun close() {
        socket.close()
        selectorManager.close()
    }

}