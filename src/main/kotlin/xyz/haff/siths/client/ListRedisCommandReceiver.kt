package xyz.haff.siths.client

import xyz.haff.siths.option.ListEnd
import xyz.haff.siths.option.RelativePosition
import kotlin.time.Duration

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
    // TODO: These ones have even more variations!! With or without a count, they must return either one element or a list
    suspend fun lpop(key: String, count: Int? = null): StringListResponseType
    suspend fun lmpop(keys: List<String>, end: ListEnd, count: Int? = null): NullableSourceAndStringListType
    suspend fun lmpop(key: String, end: ListEnd, count: Int? = null): StringListResponseType
    suspend fun blmpop(keys: List<String>, end: ListEnd, count: Int? = null): NullableSourceAndStringListType
    suspend fun blmpop(key: String, end: ListEnd, count: Int? = null): StringListResponseType

    suspend fun lpop(key: String): NullableStringResponseType
    suspend fun rpop(key: String, count: Int? = null): StringListResponseType
    suspend fun rpop(key: String): NullableStringResponseType
    suspend fun lpush(key: String, element: String, vararg rest: String): LongResponseType
    suspend fun lpushx(key: String, element: String, vararg rest: String): LongResponseType
    suspend fun rpush(key: String, element: String, vararg rest: String): LongResponseType
    suspend fun rpushx(key: String, element: String, vararg rest: String): LongResponseType
    suspend fun lrem(key: String, element: String, count: Int = 0): LongResponseType
    suspend fun lrange(key: String, start: Int, stop: Int): StringListResponseType
    suspend fun lpos(key: String, element: String, rank: Int? = null, maxlen: Int? = null): NullableLongResponseType
    suspend fun lpos(key: String, element: String, rank: Int? = null, count: Int, maxlen: Int? = null): LongListResponseType
    suspend fun lset(key: String, index: Int, element: String): BooleanResponseType
    suspend fun ltrim(key: String, start: Int, stop: Int): UnitResponseType
    suspend fun lmove(source: String, destination: String, sourceEnd: ListEnd, destinationEnd: ListEnd): StringResponseType
    suspend fun brpop(keys: List<String>, timeout: Duration? = null): NullableSourceAndStringType
    suspend fun brpop(key: String, timeout: Duration? = null): NullableStringResponseType
    suspend fun blpop(keys: List<String>, timeout: Duration? = null): NullableSourceAndStringType
    suspend fun blpop(key: String, timeout: Duration? = null): NullableStringResponseType
}