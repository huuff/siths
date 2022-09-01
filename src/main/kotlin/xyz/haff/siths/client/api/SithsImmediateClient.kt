package xyz.haff.siths.client.api

import xyz.haff.siths.protocol.RedisClient
import xyz.haff.siths.protocol.RedisCursor
import xyz.haff.siths.protocol.RespType
import xyz.haff.siths.protocol.SourceAndData
import kotlin.time.Duration

// Just to provide specific types for generics, and to avoid having to do so in both Standalone and Managed clients
interface SithsImmediateClient : SithsClient<
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
        Map<String, String>,

        RedisCursor<String>,

        SourceAndData<String>?,
        SourceAndData<List<String>>?,

        Boolean,
        Unit,
        RespType<*>,
        >, ListSithsClient {

}

