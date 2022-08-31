package xyz.haff.siths.client

import xyz.haff.siths.option.ExclusiveMode
import xyz.haff.siths.option.ExpirationCondition
import xyz.haff.siths.option.ListEnd
import xyz.haff.siths.option.RelativePosition
import kotlin.time.Duration

interface RedisCommandReceiver<
        UnitResponseType,
        StringResponseType,
        NullableStringResponseType,
        RespResponseType,
        LongResponseType,
        DoubleResponseType,
        NullableLongResponseType,
        LongListResponseType,
        ClientListResponseType,
        DurationResponseType,
        StringSetResponseType,
        StringCursorResponseType,
        BooleanResponseType,
        StringToBooleanMapResponseType,
        StringListResponseType,
        NullableSourceAndStringListType,
        NullableSourceAndStringType,
        > : ListRedisCommandReceiver<
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
    suspend fun set(
        key: String,
        value: String,
        exclusiveMode: ExclusiveMode? = null,
        timeToLive: Duration? = null
    ): UnitResponseType

    suspend fun get(key: String): StringResponseType
    suspend fun del(key: String, vararg rest: String): LongResponseType
    suspend fun ttl(key: String): DurationResponseType?
    suspend fun scriptLoad(script: String): StringResponseType
    suspend fun evalSha(sha: String, keys: List<String> = listOf(), args: List<String> = listOf()): RespResponseType
    suspend fun eval(script: String, keys: List<String> = listOf(), args: List<String> = listOf()): RespResponseType
    suspend fun incrBy(key: String, value: Long): LongResponseType
    suspend fun incrByFloat(key: String, value: Double): DoubleResponseType
    suspend fun exists(key: String, vararg rest: String): BooleanResponseType
    suspend fun expire(
        key: String,
        duration: Duration,
        expirationCondition: ExpirationCondition? = null
    ): BooleanResponseType

    suspend fun persist(key: String): BooleanResponseType

    // SETS
    suspend fun sadd(key: String, value: String, vararg rest: String): LongResponseType
    suspend fun smembers(key: String): StringSetResponseType
    suspend fun sismember(key: String, member: String): BooleanResponseType
    suspend fun smismember(key: String, member: String, vararg rest: String): StringToBooleanMapResponseType
    suspend fun scard(key: String): LongResponseType
    suspend fun srem(key: String, member: String, vararg rest: String): LongResponseType
    suspend fun sintercard(key: String, vararg rest: String, limit: Int? = null): LongResponseType
    suspend fun sdiffstore(destination: String, key: String, vararg rest: String): LongResponseType
    suspend fun sdiff(key: String, vararg rest: String): StringSetResponseType
    suspend fun sinter(key: String, vararg rest: String): StringSetResponseType
    suspend fun smove(source: String, destination: String, member: String): BooleanResponseType
    suspend fun spop(key: String): NullableStringResponseType
    suspend fun spop(key: String, count: Int? = null): StringSetResponseType
    suspend fun srandmember(key: String): NullableStringResponseType
    suspend fun srandmember(key: String, count: Int? = null): StringSetResponseType
    suspend fun sunion(key: String, vararg rest: String): StringSetResponseType
    suspend fun sunionstore(destination: String, key: String, vararg rest: String): LongResponseType
    suspend fun sinterstore(destination: String, key: String, vararg rest: String): LongResponseType
    suspend fun sscan(
        key: String,
        cursor: Long = 0,
        match: String? = null,
        count: Int? = null
    ): StringCursorResponseType

    suspend fun clientList(): ClientListResponseType
    suspend fun ping(): BooleanResponseType
}