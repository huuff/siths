package xyz.haff.siths.client.api

import xyz.haff.siths.protocol.RedisCursor

interface SetSithsImmediateClient : SetSithsClient<
        Long,

        String?,
        Set<String>,

        Map<String, Boolean>,

        RedisCursor<String>,

        Boolean
        > {
}