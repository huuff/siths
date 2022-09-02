package xyz.haff.siths.client.api

interface HashSithsClient<
        LongResponseType,

        DoubleResponseType,

        StringResponseType,
        NullableStringResponseType,
        StringListResponseType,

        StringToStringMapResponseType,

        BooleanResponseType,
        >
    : HashRedisCommandReceiver<
        LongResponseType,

        DoubleResponseType,

        StringResponseType,
        NullableStringResponseType,
        StringListResponseType,

        StringToStringMapResponseType,

        BooleanResponseType,
        > {

    suspend fun hgetOrNull(key: String, field: String): NullableStringResponseType
    suspend fun hsetAny(key: String, pair: Pair<String, Any>, vararg rest: Pair<String, Any>): LongResponseType
}