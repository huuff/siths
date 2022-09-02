package xyz.haff.siths.client.api

interface HashRedisCommandReceiver<
            LongResponseType,

            StringResponseType,
            StringListResponseType,

            StringToStringMapResponseType,

            BooleanResponseType,
        > {
    suspend fun hget(key: String, field: String): StringResponseType
    suspend fun hset(key: String, pair: Pair<String, String>, vararg rest: Pair<String, String>): LongResponseType
    suspend fun hgetall(key: String): StringToStringMapResponseType
    suspend fun hkeys(key: String): StringListResponseType
    suspend fun hvals(key: String): StringListResponseType
    suspend fun hexists(key: String, field: String): BooleanResponseType
}