package xyz.haff.siths.client.api

import xyz.haff.siths.protocol.SourceAndData

interface ListSithsImmediateClient : ListSithsClient<
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
        >