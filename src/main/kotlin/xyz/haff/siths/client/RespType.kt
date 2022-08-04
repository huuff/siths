package xyz.haff.siths.client

sealed interface RespType<T> {
    val value: T
}

data class RespSimpleString(override val value: String) : RespType<String>
data class RespError(override val value: String): RespType<String> {
    fun throwAsException() {
        throw RuntimeException(value)
    }
}
data class RespInteger(override val value: Int): RespType<Int>
data class RespBulkString(override val value: String): RespType<String>

// TODO: Arrays
fun makeRespType(response: String): RespType<*> = when (response[0]) {
    '+' -> RespSimpleString(response.drop(1))
    '-' -> RespError(response.drop(1))
    ':' -> RespInteger(response.drop(1).toInt())
    '$' -> RespBulkString(response.drop(1))
    else -> throw RuntimeException("Incapable of deciding the resp type of $response")
}
