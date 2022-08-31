package xyz.haff.siths.client

import xyz.haff.siths.protocol.SourceAndData

interface ListSithsClient: ListRedisCommandReceiver<
        Long,
        String,
        String?,
        Long?,
        List<String>,
        SourceAndData<List<String>>?,
        SourceAndData<String>?,
        List<Long>,
        Boolean,
        Unit
        > {
}