package xyz.haff.siths.client

import kotlin.time.Duration

interface Siths<
        UnitResponseType,
        StringResponseType,
        NullableStringResponseType,
        RespResponseType,
        LongResponseType,
        NullableLongResponseType,
        ClientListResponseType,
        DurationResponseType,
        StringSetResponseType,
        StringCursorResponseType,
        BooleanResponseType,
        StringToBooleanMapResponseType,
        StringListResponseType,
        > {
    suspend fun set(key: String, value: Any, exclusiveMode: ExclusiveMode? = null, timeToLive: Duration? = null): UnitResponseType
    suspend fun get(key: String): StringResponseType
    suspend fun del(key: String, vararg rest: String): LongResponseType
    suspend fun ttl(key: String): DurationResponseType?
    suspend fun scriptLoad(script: String): StringResponseType
    suspend fun evalSha(sha: String, keys: List<String> = listOf(), args: List<String> = listOf()): RespResponseType
    suspend fun eval(script: String, keys: List<String> = listOf(), args: List<String> = listOf()): RespResponseType
    suspend fun incrBy(key: String, value: Long): LongResponseType
    suspend fun exists(key: String, vararg rest: String): BooleanResponseType
    suspend fun expire(key: String, duration: Duration, expirationCondition: ExpirationCondition? = null): BooleanResponseType

    // SETS
    suspend fun sadd(key: String, value: Any, vararg rest: Any): LongResponseType
    suspend fun smembers(key: String): StringSetResponseType
    suspend fun sismember(key: String, member: Any): BooleanResponseType
    suspend fun smismember(key: String, member: Any, vararg rest: Any): StringToBooleanMapResponseType
    suspend fun scard(key: String): LongResponseType
    suspend fun srem(key: String, member: Any, vararg rest: Any): LongResponseType
    suspend fun sintercard(key: String, vararg rest: String, limit: Int? = null): LongResponseType
    suspend fun sdiffstore(destination: String, key: String, vararg rest: String): LongResponseType
    suspend fun sdiff(key: String, vararg rest: String): StringSetResponseType
    suspend fun sinter(key: String, vararg rest: String): StringSetResponseType
    suspend fun smove(source: String, destination: String, member: Any): BooleanResponseType
    suspend fun spop(key: String, count: Int? = null): StringSetResponseType
    suspend fun srandmember(key: String, count: Int? = null): StringSetResponseType
    suspend fun sunion(key: String, vararg rest: String): StringSetResponseType
    suspend fun sunionstore(destination: String, key: String, vararg rest: String): LongResponseType
    suspend fun sinterstore(destination: String, key: String, vararg rest: String): LongResponseType
    suspend fun sscan(key: String, cursor: Long = 0, match: String? = null, count: Int? = null): StringCursorResponseType

    // LISTS
    suspend fun llen(key: String): LongResponseType
    suspend fun lindex(key: String, index: Int): NullableStringResponseType
    suspend fun linsert(key: String, relativePosition: RelativePosition, pivot: Any, element: Any): NullableLongResponseType
    suspend fun lpop(key: String, count: Int): StringListResponseType
    suspend fun lpop(key: String): NullableStringResponseType
    suspend fun rpop(key: String, count: Int): StringListResponseType
    suspend fun rpop(key: String): NullableStringResponseType
    suspend fun lpush(key: String, element: Any, vararg rest: Any): LongResponseType
    suspend fun rpush(key: String, element: Any, vararg rest: Any): LongResponseType
    suspend fun lrem(key: String, element: Any, count: Int = 0): LongResponseType
    suspend fun lrange(key: String, start: Int, stop: Int): StringListResponseType

    suspend fun clientList(): ClientListResponseType
    suspend fun ping(): BooleanResponseType
}