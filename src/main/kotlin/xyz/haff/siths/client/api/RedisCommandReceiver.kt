package xyz.haff.siths.client.api

import xyz.haff.siths.option.ExclusiveMode
import xyz.haff.siths.option.ExpirationCondition
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
        >, SetRedisCommandReceiver<
        LongResponseType,
        StringSetResponseType,
        BooleanResponseType,
        StringToBooleanMapResponseType,
        NullableStringResponseType,
        StringCursorResponseType,
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

    suspend fun clientList(): ClientListResponseType
    suspend fun ping(): BooleanResponseType
}