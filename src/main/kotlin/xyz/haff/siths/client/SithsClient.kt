package xyz.haff.siths.client

import xyz.haff.siths.protocol.RedisClient
import xyz.haff.siths.protocol.RedisCursor
import xyz.haff.siths.protocol.RespType
import xyz.haff.siths.protocol.SourceAndData
import kotlin.time.Duration

/**
 * A thin wrapper over a plain redis connection. Things it does:
 *  * Provide a discoverable entrypoint for available functions
 *  * Escape the commands that are sent to Redis
 *  * Convert the responses to the appropriate Kotlin types
 */
interface SithsClient : RedisCommandReceiver<
        Unit,
        String,
        String?,
        RespType<*>,
        Long,
        Double,
        Long?,
        List<Long>,
        List<RedisClient>,
        Duration,
        Set<String>,
        RedisCursor<String>,
        Boolean,
        Map<String, Boolean>,
        List<String>,
        SourceAndData<List<String>>?,
        SourceAndData<String>?,
        >, ListSithsClient {

    suspend fun getOrNull(key: String): String?
}