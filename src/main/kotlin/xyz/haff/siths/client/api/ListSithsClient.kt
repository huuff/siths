package xyz.haff.siths.client.api

import xyz.haff.siths.protocol.SourceAndData

// XXX: Note that there is one level of abstraction more for a SithsClient than for a ListSithsClient
interface ListSithsClient: ListRedisCommandReceiver<
        Long,
        Long?,
        List<Long>,

        String,
        String?,
        List<String>,

        SourceAndData<String>?,
        SourceAndData<List<String>>?,

        Boolean,
        Unit
        > {
}