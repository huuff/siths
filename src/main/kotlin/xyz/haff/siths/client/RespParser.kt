package xyz.haff.siths.client

import io.ktor.utils.io.*
import java.nio.ByteBuffer

// TODO: Somewhere else
private val firstWordRegex = Regex("""\w+""")

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

    suspend fun parseSimpleString(): RespSimpleString = RespSimpleString(value = channel.readUTF8Line()!!)
    suspend fun parseError(): RespError {
        // TODO: `firstWord` in koy
        val errorMessage = channel.readUTF8Line()!!
        val errorType = firstWordRegex.find(errorMessage)!!.value
        return RespError(type = errorType, value = errorMessage.drop(errorType.length))
    }
    suspend fun parseInteger(): RespInteger = RespInteger(channel.readUTF8Line()!!.toLong())
    suspend fun parseBulkString(length: Int): RespBulkString = RespBulkString(readWithoutCrlf(length))
    suspend fun parseArray(length: Int): RespArray {
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
            if (length == -1)
                RespNullResponse
            else {
                parseBulkString(length)
            }
        }
        ASTERISK -> {
            val length = channel.readUTF8Line()!!.toInt()
            parseArray(length)
        }
        else -> throw RuntimeException("Unknown response type: '$responseType'")
    }
}