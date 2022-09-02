package xyz.haff.siths.client.api

import xyz.haff.siths.protocol.RedisCursor

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
            >