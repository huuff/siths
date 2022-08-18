package xyz.haff.siths.client

import xyz.haff.siths.common.RedisScriptNotLoadedException

sealed interface RespType<T> {
    val value: T

    fun isOk() = this is RespSimpleString && this.value == "OK"
}

data class RespSimpleString(override val value: String) : RespType<String>
data class RespError(val type: String, override val value: String): RespType<String> {
    fun throwAsException(): Nothing {
        when (type) {
            "NOSCRIPT" -> throw RedisScriptNotLoadedException()
            else -> throw RuntimeException("$type:$value")
        }
    }
}
data class RespInteger(override val value: Long): RespType<Long>
data class RespBulkString(override val value: String): RespType<String>
object RespNullResponse: RespType<Nothing> {
    override val value: Nothing
        get() = throw RuntimeException("Empty response from Redis")
}
