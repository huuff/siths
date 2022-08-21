package xyz.haff.siths.client

import kotlin.time.Duration

interface Siths<
        UnitResponseType,
        StringResponseType,
        RespResponseType,
        LongResponseType,
        ClientListResponseType,
        DurationResponseType,
        StringSetResponseType,
        StringSetCursorResponseType,
        BooleanResponseType,
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

    // SETS
    suspend fun sadd(key: String, value: Any, vararg rest: Any): LongResponseType
    suspend fun smembers(key: String): StringSetResponseType
    suspend fun sismember(key: String, member: Any): BooleanResponseType
    suspend fun scard(key: String): LongResponseType
    suspend fun srem(key: String, member: Any, vararg rest: Any): LongResponseType
    suspend fun sintercard(key: String, vararg rest: String, limit: Int? = null): LongResponseType
    suspend fun sdiffstore(destination: String, key: String, vararg rest: String): LongResponseType
    suspend fun sinterstore(destination: String, key: String, vararg rest: String): LongResponseType
    suspend fun sscan(key: String, cursor: Long = 0, match: String? = null, count: Int? = null): StringSetCursorResponseType

    suspend fun clientList(): ClientListResponseType
    suspend fun ping(): BooleanResponseType
}