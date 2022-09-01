package xyz.haff.siths.client.api

import xyz.haff.siths.option.ListEnd
import xyz.haff.siths.option.RelativePosition
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

interface ListRedisCommandReceiver<
        LongResponseType,
        StringResponseType,
        NullableStringResponseType,
        NullableLongResponseType,
        StringListResponseType,
        NullableSourceAndStringListType,
        NullableSourceAndStringType,
        LongListResponseType,
        BooleanResponseType,
        UnitResponseType
        > {
    suspend fun llen(key: String): LongResponseType
    suspend fun lindex(key: String, index: Int): NullableStringResponseType
    suspend fun linsert(key: String, relativePosition: RelativePosition, pivot: String, element: String): NullableLongResponseType
    suspend fun lrem(key: String, element: String, count: Int = 0): LongResponseType
    suspend fun lrange(key: String, start: Int, stop: Int): StringListResponseType
    suspend fun lset(key: String, index: Int, element: String): BooleanResponseType
    suspend fun ltrim(key: String, start: Int, stop: Int): UnitResponseType
    suspend fun lmove(source: String, destination: String, sourceEnd: ListEnd, destinationEnd: ListEnd): StringResponseType

    suspend fun lpop(key: String, count: Int? = null): StringListResponseType
    suspend fun lpop(key: String): NullableStringResponseType

    suspend fun lmpop(keys: List<String>, end: ListEnd, count: Int? = null): NullableSourceAndStringListType
    suspend fun lmpop(key: String, end: ListEnd, count: Int? = null): StringListResponseType

    suspend fun rpop(key: String, count: Int? = null): StringListResponseType
    suspend fun rpop(key: String): NullableStringResponseType

    // BLOCKING
    suspend fun blmpop(timeout: Duration = 0.seconds, key: String, vararg otherKeys: String, end: ListEnd, count: Int? = null): NullableSourceAndStringListType
    suspend fun brpop(key: String, vararg otherKeys: String, timeout: Duration? = 0.seconds): NullableSourceAndStringType
    suspend fun blpop(key: String, vararg otherKeys: String, timeout: Duration? = 0.seconds): NullableSourceAndStringType

    // LPOS
    suspend fun lpos(key: String, element: String, rank: Int? = null, maxlen: Int? = null): NullableLongResponseType
    suspend fun lpos(key: String, element: String, rank: Int? = null, count: Int, maxlen: Int? = null): LongListResponseType

    // PUSH
    suspend fun lpush(key: String, element: String, vararg rest: String): LongResponseType
    suspend fun lpushx(key: String, element: String, vararg rest: String): LongResponseType
    suspend fun rpush(key: String, element: String, vararg rest: String): LongResponseType
    suspend fun rpushx(key: String, element: String, vararg rest: String): LongResponseType
}