package xyz.haff.siths.client.api

import xyz.haff.siths.protocol.SourceAndData

// XXX: Note that there is one level of abstraction more for a HashSithsClient than for a ListSithsClient or SetSithsClient,
// that's because I haven't introduced higher-level abstraction methods for lists yet
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