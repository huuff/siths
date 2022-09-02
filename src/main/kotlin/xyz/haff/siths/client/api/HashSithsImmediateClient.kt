package xyz.haff.siths.client.api

import xyz.haff.siths.protocol.RedisCursor

interface HashSithsImmediateClient : HashSithsClient <
        Long,

        Double,

        String,
        String?,
        List<String>,

        Map<String, String>,

        RedisCursor<Pair<String, String>>,

        Boolean
        >