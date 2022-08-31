package xyz.haff.siths.protocol

import io.ktor.utils.io.*
import java.nio.ByteBuffer
import xyz.haff.koy.strings.words

private val CRLF = "\r\n".toByteArray(Charsets.UTF_8)
private val PLUS = "+".toByteArray(Charsets.UTF_8)[0]
private val MINUS = "-".toByteArray(Charsets.UTF_8)[0]
private val COLON = ":".toByteArray(Charsets.UTF_8)[0]
private val DOLLAR = "$".toByteArray(Charsets.UTF_8)[0]
private val ASTERISK = "*".toByteArray(Charsets.UTF_8)[0]

@JvmInline
value class RespParser(private val channel: ByteReadChannel) {

    private suspend fun readWithoutCrlf(length: Int): String {
        val responseBuffer = ByteBuffer.allocate(length)
        channel.readFully(responseBuffer)
        channel.discard(2) // Discard CRLF
        return String(responseBuffer.array(), Charsets.UTF_8)
    }

    private suspend fun parseSimpleString(): RespSimpleString = RespSimpleString(value = channel.readUTF8Line()!!)
    private suspend fun parseError(): RespError {
        val errorMessage = channel.readUTF8Line()!!
        val errorType = errorMessage.words[0]
        return RespError(type = errorType, value = errorMessage.drop(errorType.length))
    }
    private suspend fun parseInteger(): RespInteger = RespInteger(channel.readUTF8Line()!!.toLong())
    private suspend fun parseBulkString(length: Int): RespBulkString = RespBulkString(readWithoutCrlf(length))
    private suspend fun parseArray(length: Int): RespArray {
        val responses: MutableList<RespType<*>> = ArrayList(length)
        repeat(length) {
            responses += parse()
        }
        return RespArray(responses)
    }

    suspend fun parse(): RespType<*> =  when (val responseType = channel.readByte()) {
        PLUS -> parseSimpleString()
        MINUS -> parseError()
        COLON -> parseInteger()
        DOLLAR -> {
            val length = channel.readUTF8Line()!!.toInt()
            if (length == -1) {
                RespNullResponse
            } else {
                parseBulkString(length)
            }
        }
        ASTERISK -> {
            val length = channel.readUTF8Line()!!.toInt()
            if (length == -1) {
                RespNullResponse
            } else {
                parseArray(length)
            }
        }
        else -> throw RuntimeException("Unknown response type: '$responseType'")
    }
}