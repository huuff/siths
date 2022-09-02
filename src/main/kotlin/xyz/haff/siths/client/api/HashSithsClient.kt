package xyz.haff.siths.client.api

interface HashSithsClient<
        LongResponseType,

        StringResponseType,
        NullableStringResponseType,
        StringListResponseType,

        StringToStringMapResponseType,
        >
    : HashRedisCommandReceiver<
        LongResponseType,

        StringResponseType,
        StringListResponseType,

        StringToStringMapResponseType
        > {

    suspend fun hgetOrNull(key: String, field: String): NullableStringResponseType
}