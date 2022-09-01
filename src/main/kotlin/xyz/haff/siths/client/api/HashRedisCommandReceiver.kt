package xyz.haff.siths.client.api

interface HashRedisCommandReceiver<
            LongResponseType,
            StringResponseType
        > {
    suspend fun hget(key: String, field: String): StringResponseType
    suspend fun hset(key: String, pair: Pair<String, String>, vararg rest: Pair<String, String>): LongResponseType
}