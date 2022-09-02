package xyz.haff.siths.pipelining

import xyz.haff.siths.client.api.SetSithsClient
import xyz.haff.siths.protocol.RedisCursor

interface IPipelinedSetSithsClient : SetSithsClient<
        QueuedResponse<Long>,

        QueuedResponse<String?>,
        QueuedResponse<Set<String>>,

        QueuedResponse<Map<String, Boolean>>,

        QueuedResponse<RedisCursor<String>>,

        QueuedResponse<Boolean>
        > {
}