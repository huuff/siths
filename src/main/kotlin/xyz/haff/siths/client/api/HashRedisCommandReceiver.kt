package xyz.haff.siths.client.api

interface HashRedisCommandReceiver<
            LongResponseType,

            DoubleResponseType,

            StringResponseType,
            NullableStringResponseType,
            StringListResponseType,

            StringToStringMapResponseType,

            StringPairCursorResponseType,

            BooleanResponseType,
        > {
    suspend fun hget(key: String, field: String): StringResponseType
    suspend fun hset(key: String, pair: Pair<String, String>, vararg rest: Pair<String, String>): LongResponseType
    suspend fun hgetAll(key: String): StringToStringMapResponseType
    suspend fun hkeys(key: String): StringListResponseType
    suspend fun hvals(key: String): StringListResponseType
    suspend fun hexists(key: String, field: String): BooleanResponseType
    suspend fun hincrBy(key: String, field: String, increment: Long): LongResponseType
    suspend fun hincrByFloat(key: String, field: String, increment: Double): DoubleResponseType
    suspend fun hmget(key: String, field: String, vararg rest: String): StringToStringMapResponseType
    suspend fun hlen(key: String): LongResponseType
    suspend fun hdel(key: String, field: String, vararg rest: String): LongResponseType
    suspend fun hstrLen(key: String, field: String): LongResponseType
    suspend fun hsetnx(key: String, field: String, value: String): BooleanResponseType
    suspend fun hscan(key: String, cursor: Long = 0, match: String? = null, count: Int? = null): StringPairCursorResponseType

    // HRANDFIELD
    suspend fun hrandField(key: String): NullableStringResponseType
    suspend fun hrandField(key: String, count: Int): StringListResponseType
    suspend fun hrandFieldWithValues(key: String, count: Int): StringToStringMapResponseType
}