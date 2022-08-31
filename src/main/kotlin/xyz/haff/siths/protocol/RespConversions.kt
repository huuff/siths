package xyz.haff.siths.protocol

import xyz.haff.siths.common.RedisUnexpectedRespResponseException
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

@Suppress("UNCHECKED_CAST") // Not actually unchecked, but Kotlin is not smart enough to notice
inline fun <reified T> RespArray.contentsOfType(): List<T> {
    if (value.any { it !is T} ) { handleAsUnexpected() }
    return value as List<T>
}

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

fun RespType<*>.toStringNonNull(): String = when (this) {
    is RespBulkString -> value
    else -> handleAsUnexpected()
}

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
            && (this.value[1] as RespArray).value.all { it is RespBulkString } // Can I use contentsOfType?
    -> {
        RedisCursor(
            next = (this.value[0] as RespBulkString).value.toLong(),
            contents = ((this.value[1] as RespArray).value as List<RespBulkString>).map { it.value }
        )
    }
    else -> handleAsUnexpected()
}

fun RespType<*>.bulkOrArrayToStringSet(): Set<String> = when (this) {
    is RespBulkString -> setOf(value)
    is RespArray -> contentsOfType<RespBulkString>().map { it.value }.toSet()
    is RespNullResponse -> setOf()
    else -> handleAsUnexpected()
}

fun RespType<*>.bulkOrArrayToStringList(): List<String> = when (this) {
    is RespBulkString -> listOf(value)
    is RespArray -> contentsOfType<RespBulkString>().map { it.value }
    is RespNullResponse -> listOf()
    else -> handleAsUnexpected()
}

fun RespType<*>.toStringToBooleanMap(vararg input: Any): Map<String, Boolean> = when(this) {
    is RespArray -> {
        if (input.size != value.size) { handleAsUnexpected() }
        val booleanResponses = contentsOfType<RespInteger>().map { it.integerToBoolean() }
        (input.map(Any::toString) zip booleanResponses).associate { it }
    }
    else -> handleAsUnexpected()
}

fun RespType<*>.toStringList(): List<String> = when (this) {
    is RespArray -> contentsOfType<RespBulkString>().map { it.value }
    else -> handleAsUnexpected()
}

fun RespType<*>.toPositiveLongOrNull(): Long? = when (this) {
    is RespInteger -> when (value) {
        -1L -> null
        else -> value
    }
    else -> handleAsUnexpected()
}

fun RespType<*>.toLongOrNull(): Long? = when (this) {
    is RespInteger -> value
    is RespNullResponse -> null
    else -> handleAsUnexpected()
}

fun RespType<*>.toLongList(): List<Long> = when (this) {
    is RespArray -> contentsOfType<RespInteger>().map { it.value }
    else -> handleAsUnexpected()
}

fun RespType<*>.luaBooleanToBoolean(): Boolean = when {
    this is RespInteger && value == 1L -> true
    this is RespNullResponse -> false
    else -> this.handleAsUnexpected()
}

fun RespType<*>.toDouble(): Double = when (this) {
    is RespBulkString -> value.toDouble()
    else -> handleAsUnexpected()
}

fun RespType<*>.toSourceAndStringListOrNull(): SourceAndData<List<String>>? = when (this) {
    is RespArray -> when (this.value.size) {
        2 -> {
           if (value[0] !is RespBulkString || value[1] !is RespArray) { handleAsUnexpected() }

           SourceAndData(
               source = (value[0] as RespBulkString).value,
               data = (value[1] as RespArray).contentsOfType<RespBulkString>().map { it.value }
           )
        }
        else -> handleAsUnexpected()
    }
    is RespNullResponse -> null
    else -> handleAsUnexpected()
}

fun RespType<*>.toSourceAndStringOrNull(): SourceAndData<String>? = when (this) {
    is RespArray -> when (this.value.size) {
        2 -> {
        val contents = this.contentsOfType<RespBulkString>()

        SourceAndData(
            source = contents[0].value,
            data = contents[1].value
        )
    }
        else -> handleAsUnexpected()
    }
    is RespNullResponse -> null
    else -> handleAsUnexpected()
}

fun RespType<*>.assertOk() {
    isOk()
}