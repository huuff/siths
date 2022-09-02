package xyz.haff.siths.client.api

interface SetRedisCommandReceiver<
        LongResponseType,
        NullableStringResponseType,

        StringSetResponseType,
        StringToBooleanMapResponseType,
        StringCursorResponseType,

        BooleanResponseType,
        > {

    suspend fun sadd(key: String, value: String, vararg rest: String): LongResponseType
    suspend fun smembers(key: String): StringSetResponseType
    suspend fun sisMember(key: String, member: String): BooleanResponseType
    suspend fun smisMember(key: String, member: String, vararg rest: String): StringToBooleanMapResponseType
    suspend fun scard(key: String): LongResponseType
    suspend fun srem(key: String, member: String, vararg rest: String): LongResponseType
    suspend fun sinterCard(key: String, vararg rest: String, limit: Int? = null): LongResponseType
    suspend fun sdiffStore(destination: String, key: String, vararg rest: String): LongResponseType
    suspend fun sdiff(key: String, vararg rest: String): StringSetResponseType
    suspend fun sinter(key: String, vararg rest: String): StringSetResponseType
    suspend fun smove(source: String, destination: String, member: String): BooleanResponseType
    suspend fun spop(key: String): NullableStringResponseType
    suspend fun spop(key: String, count: Int? = null): StringSetResponseType
    suspend fun srandMember(key: String): NullableStringResponseType
    suspend fun srandMember(key: String, count: Int? = null): StringSetResponseType
    suspend fun sunion(key: String, vararg rest: String): StringSetResponseType
    suspend fun sunionStore(destination: String, key: String, vararg rest: String): LongResponseType
    suspend fun sinterStore(destination: String, key: String, vararg rest: String): LongResponseType
    suspend fun sscan(
        key: String,
        cursor: Long = 0,
        match: String? = null,
        count: Int? = null
    ): StringCursorResponseType
}