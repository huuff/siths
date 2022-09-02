package xyz.haff.siths.client.api

interface ListSithsClient<
        LongResponseType,
        NullableLongResponseType,
        LongListResponseType,

        StringResponseType,
        NullableStringResponseType,
        StringListResponseType,

        NullableSourceAndStringType,
        NullableSourceAndStringListType,

        BooleanResponseType,
        UnitResponseType,
        >: ListRedisCommandReceiver<
        LongResponseType,
        NullableLongResponseType,
        LongListResponseType,

        StringResponseType,
        NullableStringResponseType,
        StringListResponseType,

        NullableSourceAndStringType,
        NullableSourceAndStringListType,

        BooleanResponseType,
        UnitResponseType,
        > {

    suspend fun lpushAny(key: String, element: Any, vararg rest: Any): LongResponseType
    suspend fun rpushAny(key: String, element: Any, vararg rest: Any): LongResponseType
}