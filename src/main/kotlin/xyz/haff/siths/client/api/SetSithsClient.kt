package xyz.haff.siths.client.api

import xyz.haff.siths.protocol.RedisCursor

interface SetSithsClient :
    SetRedisCommandReceiver<Long, Set<String>, Boolean, Map<String, Boolean>, String?, RedisCursor<String>>