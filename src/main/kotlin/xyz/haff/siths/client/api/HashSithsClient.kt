package xyz.haff.siths.client.api

interface HashSithsClient<
        LongResponseType,

        StringResponseType,
        NullableStringResponseType,
        StringListResponseType,

        StringToStringMapResponseType,

        BooleanResponseType,
        >
    : HashRedisCommandReceiver<
        LongResponseType,

        StringResponseType,
        StringListResponseType,

        StringToStringMapResponseType,

        BooleanResponseType,
        > {

    suspend fun hgetOrNull(key: String, field: String): NullableStringResponseType
}