package xyz.haff.siths.client

sealed interface RespType<T> {
    val value: T
}

data class RespSimpleString(override val value: String) : RespType<String> {
    fun isOk() = value == "OK"
}
data class RespError(val type: String, override val value: String): RespType<String> {
    fun throwAsException(): Nothing {
        when (type) {
            "NOSCRIPT" -> throw RedisScriptNotLoadedException()
            else -> throw RuntimeException("$type:$value")
        }
    }
}
data class RespInteger(override val value: Int): RespType<Int>
data class RespBulkString(override val value: String): RespType<String>
object RespNullResponse: RespType<Nothing> {
    override val value: Nothing
        get() = throw RuntimeException("Empty response from Redis")
}
