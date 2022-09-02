package xyz.haff.siths.client.api

import xyz.haff.siths.protocol.SourceAndData

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
}