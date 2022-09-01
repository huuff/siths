package xyz.haff.siths.client.api

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
        Long,
        Long?,
        List<Long>,

        Double,

        String,
        String?,
        List<String>,
        Set<String>,

        List<RedisClient>,
        Duration,

        Map<String, Boolean>,
        RedisCursor<String>,

        SourceAndData<String>?,
        SourceAndData<List<String>>?,

        Boolean,
        Unit,
        RespType<*>,
        >, ListSithsClient {

    suspend fun getOrNull(key: String): String?
}

