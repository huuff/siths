package xyz.haff.siths.client

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