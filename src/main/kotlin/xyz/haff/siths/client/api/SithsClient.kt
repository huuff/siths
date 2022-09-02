package xyz.haff.siths.client.api

/**
 * A thin wrapper over a plain redis connection. Things it does:
 *  * Provide a discoverable entrypoint for available functions
 *  * Escape the commands that are sent to Redis
 *  * Convert the responses to the appropriate Kotlin types
 */

// XXX: Heaps upon heaps of copy-pasted lists of generic parameters... all to marginally refine each interface
interface SithsClient<
        LongResponseType,
        NullableLongResponseType,
        LongListResponseType,

        DoubleResponseType,

        StringResponseType,
        NullableStringResponseType,
        StringListResponseType,
        StringSetResponseType,

        ClientListResponseType,

        DurationResponseType,
        NullableZonedDateTimeResponseType,

        StringToBooleanMapResponseType,
        StringToStringMapResponseType,

        StringCursorResponseType,
        StringPairCursorResponseType,

        NullableSourceAndStringType,
        NullableSourceAndStringListType,

        BooleanResponseType,
        UnitResponseType,
        RespResponseType,
>: RedisCommandReceiver<
        LongResponseType,
        NullableLongResponseType,
        LongListResponseType,

        DoubleResponseType,

        StringResponseType,
        NullableStringResponseType,
        StringListResponseType,
        StringSetResponseType,

        ClientListResponseType,

        DurationResponseType,
        NullableZonedDateTimeResponseType,

        StringToBooleanMapResponseType,
        StringToStringMapResponseType,

        StringCursorResponseType,
        StringPairCursorResponseType,

        NullableSourceAndStringType,
        NullableSourceAndStringListType,

        BooleanResponseType,
        UnitResponseType,
        RespResponseType,
        >, HashSithsClient<
        LongResponseType,

        DoubleResponseType,

        StringResponseType,
        NullableStringResponseType,
        StringListResponseType,

        StringToStringMapResponseType,

        StringPairCursorResponseType,

        BooleanResponseType
        >, ListSithsClient<
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
        >, SetSithsClient<
        LongResponseType,

        NullableStringResponseType,
        StringSetResponseType,

        StringToBooleanMapResponseType,

        StringCursorResponseType,

        BooleanResponseType,
        > {

    suspend fun getOrNull(key: String): NullableStringResponseType
    suspend fun getLong(key: String): LongResponseType
}