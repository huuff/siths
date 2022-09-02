package xyz.haff.siths.pipelining

import xyz.haff.siths.client.api.ListSithsClient
import xyz.haff.siths.client.api.SithsClient
import xyz.haff.siths.protocol.SourceAndData

interface IPipelinedListSithsClient : ListSithsClient<
            QueuedResponse<Long>,
            QueuedResponse<Long?>,
            QueuedResponse<List<Long>>,

            QueuedResponse<String>,
            QueuedResponse<String?>,
            QueuedResponse<List<String>>,

            QueuedResponse<SourceAndData<String>?>,
            QueuedResponse<SourceAndData<List<String>>?>,

            QueuedResponse<Boolean>,
            QueuedResponse<Unit>
        > {
}