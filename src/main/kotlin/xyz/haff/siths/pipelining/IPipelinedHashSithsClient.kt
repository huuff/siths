package xyz.haff.siths.pipelining

import xyz.haff.siths.client.api.HashSithsClient
import xyz.haff.siths.protocol.RedisCursor

interface IPipelinedHashSithsClient : HashSithsClient<
        QueuedResponse<Long>,

        QueuedResponse<Double>,

        QueuedResponse<String>,
        QueuedResponse<String?>,
        QueuedResponse<List<String>>,

        QueuedResponse<Map<String, String>>,

        QueuedResponse<RedisCursor<Pair<String, String>>>,

        QueuedResponse<Boolean>
        > {
}