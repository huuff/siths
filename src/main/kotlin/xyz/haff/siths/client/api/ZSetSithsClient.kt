package xyz.haff.siths.client.api

interface ZSetSithsClient<LongResponseType, StringSetResponseType, StringToDoubleListResponseType>
    : ZSetRedisCommandReceiver<LongResponseType, StringSetResponseType, StringToDoubleListResponseType> {
}