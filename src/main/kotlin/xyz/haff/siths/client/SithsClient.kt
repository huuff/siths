package xyz.haff.siths.client

import kotlin.time.Duration

/**
 * A thin wrapper over a plain redis connection. Things it does:
 *  * Provide a discoverable entrypoint for available functions
 *  * Escape the commands that are sent to Redis
 *  * Convert the responses to the appropriate Kotlin types
 */
interface SithsClient : Siths<
        Unit,
        String,
        RespType<*>,
        Long,
        List<RedisClient>,
        Duration,
        Set<String>,
        RedisCursor<String>,
        Boolean,
        Map<String, Boolean>,
        List<String>
        > {

    suspend fun getOrNull(key: String): String?
}