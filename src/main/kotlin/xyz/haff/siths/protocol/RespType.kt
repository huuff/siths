package xyz.haff.siths.protocol

import xyz.haff.siths.common.RedisException
import xyz.haff.siths.common.RedisScriptNotLoadedException

sealed interface RespType<T> {
    val value: T

    fun isOk() = when (this) {
        is RespSimpleString -> value == "OK"
        is RespError -> throwAsException()
        else -> false
    }
}

data class RespSimpleString(override val value: String) : RespType<String>
data class RespError(val type: String, override val value: String): RespType<String> {
    fun throwAsException(): Nothing {
        when (type) {
            "NOSCRIPT" -> throw RedisScriptNotLoadedException()
            else -> throw RedisException(type, value)
        }
    }
}
data class RespInteger(override val value: Long): RespType<Long>
data class RespBulkString(override val value: String): RespType<String>

data class RespArray(override val value: List<RespType<*>>): RespType<List<RespType<*>>>

object RespNullResponse: RespType<Nothing> {
    override val value: Nothing
        get() = throw RuntimeException("Empty response from Redis")
}
