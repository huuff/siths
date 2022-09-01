package xyz.haff.siths.client.api

interface HashSithsClient<
        LongResponseType,

        StringResponseType,
        NullableStringResponseType,
        >
    : HashRedisCommandReceiver<LongResponseType, StringResponseType> {

    suspend fun hgetOrNull(key: String, field: String): NullableStringResponseType
}