package xyz.haff.siths.client.api

interface SetRedisCommandReceiver<
        LongResponseType,
        StringSetResponseType,
        BooleanResponseType,
        StringToBooleanMapResponseType,
        NullableStringResponseType,
        StringCursorResponseType
        > {

    suspend fun sadd(key: String, value: String, vararg rest: String): LongResponseType
    suspend fun smembers(key: String): StringSetResponseType
    suspend fun sismember(key: String, member: String): BooleanResponseType
    suspend fun smismember(key: String, member: String, vararg rest: String): StringToBooleanMapResponseType
    suspend fun scard(key: String): LongResponseType
    suspend fun srem(key: String, member: String, vararg rest: String): LongResponseType
    suspend fun sintercard(key: String, vararg rest: String, limit: Int? = null): LongResponseType
    suspend fun sdiffstore(destination: String, key: String, vararg rest: String): LongResponseType
    suspend fun sdiff(key: String, vararg rest: String): StringSetResponseType
    suspend fun sinter(key: String, vararg rest: String): StringSetResponseType
    suspend fun smove(source: String, destination: String, member: String): BooleanResponseType
    suspend fun spop(key: String): NullableStringResponseType
    suspend fun spop(key: String, count: Int? = null): StringSetResponseType
    suspend fun srandmember(key: String): NullableStringResponseType
    suspend fun srandmember(key: String, count: Int? = null): StringSetResponseType
    suspend fun sunion(key: String, vararg rest: String): StringSetResponseType
    suspend fun sunionstore(destination: String, key: String, vararg rest: String): LongResponseType
    suspend fun sinterstore(destination: String, key: String, vararg rest: String): LongResponseType
    suspend fun sscan(
        key: String,
        cursor: Long = 0,
        match: String? = null,
        count: Int? = null
    ): StringCursorResponseType
}