package xyz.haff.siths.client.api

interface SetSithsClient<
        LongResponseType,

        NullableStringResponseType,
        StringSetResponseType,

        StringToBooleanMapResponseType,

        StringCursorResponseType,

        BooleanResponseType,
        > :
    SetRedisCommandReceiver<
            LongResponseType,

            NullableStringResponseType,
            StringSetResponseType,

            StringToBooleanMapResponseType,

            StringCursorResponseType,

            BooleanResponseType,
            > {

    suspend fun saddAny(key: String, value: Any, vararg rest: Any): LongResponseType
}