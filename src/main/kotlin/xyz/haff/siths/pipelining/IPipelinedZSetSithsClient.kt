package xyz.haff.siths.pipelining

import xyz.haff.siths.client.api.ZSetSithsClient

interface IPipelinedZSetSithsClient : ZSetSithsClient<
            QueuedResponse<Long>,
            QueuedResponse<Set<String>>,
            QueuedResponse<List<Pair<String, Double>>>
        >