package xyz.haff.siths.client.api

interface HashRedisCommandReceiver<
            LongResponseType,

            DoubleResponseType,

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
    suspend fun hincrby(key: String, field: String, increment: Long): LongResponseType
    suspend fun hincrbyfloat(key: String, field: String, increment: Double): DoubleResponseType
    suspend fun hmget(key: String, field: String, vararg rest: String): StringToStringMapResponseType
    suspend fun hlen(key: String): LongResponseType
    suspend fun hdel(key: String, field: String, vararg rest: String): LongResponseType
    suspend fun hstrlen(key: String, field: String): LongResponseType
    suspend fun hsetnx(key: String, field: String, value: String): BooleanResponseType
}