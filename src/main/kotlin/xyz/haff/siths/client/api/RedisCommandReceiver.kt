package xyz.haff.siths.client.api

import xyz.haff.siths.option.ExistenceCondition
import xyz.haff.siths.option.ExpirationCondition
import xyz.haff.siths.option.SyncMode
import java.time.ZonedDateTime
import kotlin.time.Duration

interface RedisCommandReceiver<
        LongResponseType,
        NullableLongResponseType,
        LongListResponseType,

        DoubleResponseType,

        StringResponseType,
        NullableStringResponseType,
        StringListResponseType,
        StringSetResponseType,

        ClientListResponseType,

        DurationResponseType,
        NullableZonedDateTimeResponseType,

        StringToBooleanMapResponseType,
        StringToStringMapResponseType,

        StringToDoubleListResponseType,

        StringCursorResponseType,
        StringPairCursorResponseType,

        NullableSourceAndStringType,
        NullableSourceAndStringListType,

        BooleanResponseType,
        UnitResponseType,
        RespResponseType,
        > : ListRedisCommandReceiver<
        LongResponseType,
        NullableLongResponseType,
        LongListResponseType,

        StringResponseType,
        NullableStringResponseType,
        StringListResponseType,

        NullableSourceAndStringType,
        NullableSourceAndStringListType,

        BooleanResponseType,
        UnitResponseType
        >, SetRedisCommandReceiver<
        LongResponseType,

        NullableStringResponseType,
        StringSetResponseType,

        StringToBooleanMapResponseType,

        StringCursorResponseType,

        BooleanResponseType,
        >, HashRedisCommandReceiver <
        LongResponseType,

        DoubleResponseType,

        StringResponseType,
        NullableStringResponseType,
        StringListResponseType,

        StringToStringMapResponseType,

        StringPairCursorResponseType,

        BooleanResponseType,
        >, ZSetRedisCommandReceiver<
        LongResponseType,
        StringSetResponseType,
        StringToDoubleListResponseType,
        >{
    suspend fun set(
        key: String,
        value: String,
        existenceCondition: ExistenceCondition? = null,
        timeToLive: Duration? = null
    ): UnitResponseType

    suspend fun mset(vararg pairs: Pair<String, String>): UnitResponseType
    suspend fun mget(keys: Collection<String>): StringToStringMapResponseType

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

    suspend fun expireAt(key: String, time: ZonedDateTime, expirationCondition: ExpirationCondition? = null): BooleanResponseType
    suspend fun expireTime(key: String): NullableZonedDateTimeResponseType

    suspend fun clientList(): ClientListResponseType
    suspend fun ping(): BooleanResponseType
    suspend fun dbSize(): LongResponseType
    suspend fun flushDb(mode: SyncMode? = null): UnitResponseType
}