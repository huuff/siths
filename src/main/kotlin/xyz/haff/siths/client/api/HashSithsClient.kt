package xyz.haff.siths.client.api

interface HashSithsClient<
        LongResponseType,

        StringResponseType,
        NullableStringResponseType,

        StringToStringMapResponseType,
        >
    : HashRedisCommandReceiver<LongResponseType, StringResponseType, StringToStringMapResponseType> {

    suspend fun hgetOrNull(key: String, field: String): NullableStringResponseType
}