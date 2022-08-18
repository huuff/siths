package xyz.haff.siths.client

import kotlin.time.Duration

/**
 * A thin wrapper over a plain redis connection. Things it does:
 *  * Provide a discoverable entrypoint for available functions
 *  * Escape the commands that are sent to Redis
 *  * Convert the responses to the appropriate Kotlin types
 */
// TODO: Add ping! It'll be useful!
interface Siths<
        UnitResponseType,
        StringResponseType,
        RespResponseType,
        LongResponseType,
        ClientListResponseType,
        DurationResponseType
        > {
    suspend fun set(key: String, value: Any, exclusiveMode: ExclusiveMode? = null, timeToLive: Duration? = null): UnitResponseType
    suspend fun get(key: String): StringResponseType
    suspend fun ttl(key: String): DurationResponseType?
    suspend fun scriptLoad(script: String): StringResponseType
    suspend fun evalSha(sha: String, keys: List<String> = listOf(), args: List<String> = listOf()): RespResponseType
    suspend fun incrBy(key: String, value: Long): LongResponseType
    suspend fun clientList(): ClientListResponseType
}