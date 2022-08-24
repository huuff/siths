package xyz.haff.siths.client

import xyz.haff.siths.common.RedisUnexpectedRespResponseException
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * Throw an exception from this response. It might be an error, or something weird. In any case, throw an exception.
 */
fun RespType<*>.handleAsUnexpected(): Nothing = when (this) {
    is RespError -> this.throwAsException()
    else -> throw RedisUnexpectedRespResponseException(this)
}

fun RespType<*>.toUnit() {
    if (this is RespError) { throwAsException() }
}

fun RespType<*>.throwOnError(): RespType<*> = when (this) {
    is RespError -> throwAsException()
    else -> this
}

fun RespType<*>.toStringOrNull(): String? = when (this) {
    is RespBulkString -> value
    is RespNullResponse -> null
    else -> handleAsUnexpected()
}

// TODO: This takes a possibly null response... I should throw some exception that describes which key is missing
fun RespType<*>.toStringNonNull(): String = this.toStringOrNull()!!

fun RespType<*>.toLong(): Long = when (this) {
    is RespInteger -> value
    else -> handleAsUnexpected()
}

fun RespType<*>.toDurationOrNull(): Duration? = when (this) {
    is RespInteger -> if (value < 0) {
        null
    } else {
        value.milliseconds
    }
    else -> handleAsUnexpected()
}

// XXX: Redis returns the number of DIFFERENT keys that exist. Since our client's semantics is `true` if all exist
// and `false` otherwise, we count the number of different keys and compare it to the response
fun RespType<*>.existenceToBoolean(expectedKeys: Long): Boolean = when (this) {
    is RespInteger -> value == expectedKeys
    else -> handleAsUnexpected()
}

fun RespType<*>.integerToBoolean(): Boolean = when (this) {
    is RespInteger -> value == 1L
    else -> handleAsUnexpected()
}

fun RespType<*>.toClientList(): List<RedisClient> = when (this) {
    is RespBulkString -> parseClientList(value)
    else -> handleAsUnexpected()
}

fun RespType<*>.pongToBoolean(): Boolean = when {
    (this is RespSimpleString) && (value == "PONG") -> true
    else -> handleAsUnexpected()
}

fun RespType<*>.toStringSet(): Set<String> = when (this) {
    is RespArray -> value.map {
        if (it is RespBulkString) {
            it.value
        } else {
            handleAsUnexpected()
        }
    }.toSet()
    else -> handleAsUnexpected()
}

@Suppress("UNCHECKED_CAST") // Not actually unchecked, but Kotlin is not smart enough to notice
fun RespType<*>.toStringCursor(): RedisCursor<String> = when {
    (this is RespArray)
            && (this.value[0] is RespBulkString)
            && (this.value[1] is RespArray)
            && (this.value[1] as RespArray).value.all { it is RespBulkString }
    -> {
        RedisCursor(
            next = (this.value[0] as RespBulkString).value.toLong(),
            contents = ((this.value[1] as RespArray).value as List<RespBulkString>).map { it.value }
        )
    }
    else -> handleAsUnexpected()
}