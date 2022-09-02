package xyz.haff.siths.pipelining

import xyz.haff.siths.command.RedisCommandBuilder
import xyz.haff.siths.option.ListEnd
import xyz.haff.siths.option.RelativePosition
import xyz.haff.siths.protocol.*
import kotlin.text.toLongOrNull
import kotlin.time.Duration

class PipelinedListSithsClient(
    private val executor: RedisPipelineExecutor = RedisPipelineExecutor(),
    private val commandBuilder: RedisCommandBuilder = RedisCommandBuilder(),
): IPipelinedListSithsClient {

    override suspend fun llen(key: String): QueuedResponse<Long> =
        executor.addOperation(DeferredCommand(commandBuilder.llen(key), QueuedResponse(RespType<*>::toLong)))

    override suspend fun lindex(key: String, index: Int): QueuedResponse<String?> =
        executor.addOperation(DeferredCommand(commandBuilder.lindex(key, index), QueuedResponse(RespType<*>::toStringOrNull)))

    override suspend fun linsert(
        key: String,
        relativePosition: RelativePosition,
        pivot: String,
        element: String
    ): QueuedResponse<Long?> =
        executor.addOperation(
            DeferredCommand(
                command = commandBuilder.linsert(key, relativePosition, pivot, element),
                response = QueuedResponse(RespType<*>::toPositiveLongOrNull)
            )
        )

    override suspend fun lpop(key: String, count: Int?): QueuedResponse<List<String>> = executor.addOperation(
        DeferredCommand(
            command = commandBuilder.lpop(key, count),
            response = QueuedResponse(RespType<*>::bulkOrArrayToStringList)
        )
    )

    override suspend fun lpop(key: String): QueuedResponse<String?> =
        executor.addOperation(DeferredCommand(commandBuilder.lpop(key), QueuedResponse(RespType<*>::toStringOrNull)))

    override suspend fun lmpop(
        keys: List<String>,
        end: ListEnd,
        count: Int?
    ): QueuedResponse<SourceAndData<List<String>>?> = executor.addOperation(
        DeferredCommand(
            command = commandBuilder.lmpop(keys, end, count),
            response = QueuedResponse(RespType<*>::toSourceAndStringListOrNull)
        )
    )

    override suspend fun lmpop(key: String, end: ListEnd, count: Int?): QueuedResponse<List<String>> = executor.addOperation(
        DeferredCommand(
            command = commandBuilder.lmpop(key, end, count),
            response = QueuedResponse({ it.toSourceAndStringListOrNull()?.data ?: listOf() })
        )
    )

    override suspend fun rpop(key: String, count: Int?): QueuedResponse<List<String>> = executor.addOperation(
        DeferredCommand(
            command = commandBuilder.rpop(key, count),
            response = QueuedResponse(RespType<*>::bulkOrArrayToStringList)
        )
    )

    override suspend fun rpop(key: String): QueuedResponse<String?> = executor.addOperation(
        DeferredCommand(
            command = commandBuilder.rpop(key),
            response = QueuedResponse(RespType<*>::toStringOrNull)
        )
    )

    override suspend fun lpush(key: String, element: String, vararg rest: String): QueuedResponse<Long> =
        executor.addOperation(DeferredCommand(commandBuilder.lpush(key, element, *rest), QueuedResponse(RespType<*>::toLong)))

    override suspend fun lpushx(key: String, element: String, vararg rest: String): QueuedResponse<Long> =
        executor.addOperation(DeferredCommand(commandBuilder.lpushx(key, element, *rest), QueuedResponse(RespType<*>::toLong)))

    override suspend fun rpush(key: String, element: String, vararg rest: String): QueuedResponse<Long> =
        executor.addOperation(DeferredCommand(commandBuilder.rpush(key, element, *rest), QueuedResponse(RespType<*>::toLong)))

    override suspend fun rpushx(key: String, element: String, vararg rest: String): QueuedResponse<Long> =
        executor.addOperation(DeferredCommand(commandBuilder.rpushx(key, element, *rest), QueuedResponse(RespType<*>::toLong)))

    override suspend fun lrem(key: String, element: String, count: Int): QueuedResponse<Long> =
        executor.addOperation(DeferredCommand(commandBuilder.lrem(key, element, count), QueuedResponse(RespType<*>::toLong)))

    override suspend fun lrange(key: String, start: Int, stop: Int): QueuedResponse<List<String>> = executor.addOperation(
        DeferredCommand(
            command = commandBuilder.lrange(key, start, stop),
            response = QueuedResponse(RespType<*>::toStringList)
        )
    )

    override suspend fun lpos(key: String, element: String, rank: Int?, maxlen: Int?): QueuedResponse<Long?> =
        executor.addOperation(
            DeferredCommand(
                command = commandBuilder.lpos(key, element, rank, maxlen),
                response = QueuedResponse(RespType<*>::toLongOrNull)
            )
        )

    override suspend fun lpos(
        key: String,
        element: String,
        rank: Int?,
        count: Int,
        maxlen: Int?
    ): QueuedResponse<List<Long>> = executor.addOperation(
        DeferredCommand(
            command = commandBuilder.lpos(key, element, rank, count, maxlen),
            response = QueuedResponse(RespType<*>::toLongList)
        )
    )

    override suspend fun lset(key: String, index: Int, element: String): QueuedResponse<Boolean> = executor.addOperation(
        DeferredCommand(
            command = commandBuilder.lset(key, index, element),
            response = QueuedResponse(RespType<*>::isOk)
        )
    )

    override suspend fun ltrim(key: String, start: Int, stop: Int): QueuedResponse<Unit> = executor.addOperation(
        DeferredCommand(
            command = commandBuilder.ltrim(key, start, stop),
            response = QueuedResponse(RespType<*>::assertOk)
        )
    )

    override suspend fun lmove(
        source: String,
        destination: String,
        sourceEnd: ListEnd,
        destinationEnd: ListEnd
    ): QueuedResponse<String> = executor.addOperation(
        DeferredCommand(
            command = commandBuilder.lmove(source, destination, sourceEnd, destinationEnd),
            response = QueuedResponse(RespType<*>::toStringNonNull)
        )
    )

    override suspend fun blmpop(
        timeout: Duration,
        key: String,
        vararg otherKeys: String,
        end: ListEnd,
        count: Int?
    ): QueuedResponse<SourceAndData<List<String>>?> = executor.addOperation(
        DeferredCommand(
            command = commandBuilder.blmpop(timeout, key, otherKeys = otherKeys, end = end, count),
            response = QueuedResponse(RespType<*>::toSourceAndStringListOrNull)
        )
    )

    override suspend fun brpop(
        key: String,
        vararg otherKeys: String,
        timeout: Duration?
    ): QueuedResponse<SourceAndData<String>?> = executor.addOperation(
        DeferredCommand(
            command = commandBuilder.brpop(key, otherKeys = otherKeys, timeout),
            response = QueuedResponse(RespType<*>::toSourceAndStringOrNull)
        )
    )

    override suspend fun blpop(
        key: String,
        vararg otherKeys: String,
        timeout: Duration?
    ): QueuedResponse<SourceAndData<String>?> = executor.addOperation(
        DeferredCommand(
            command = commandBuilder.blpop(key, otherKeys = otherKeys, timeout),
            response = QueuedResponse(RespType<*>::toSourceAndStringOrNull)
        )
    )

    override suspend fun blmove(
        source: String,
        destination: String,
        sourceEnd: ListEnd,
        destinationEnd: ListEnd,
        timeout: Duration?
    ): QueuedResponse<String?> = executor.addOperation(
        DeferredCommand(
            command = commandBuilder.blmove(source, destination, sourceEnd, destinationEnd, timeout),
            response = QueuedResponse(RespType<*>::toStringOrNull)
        )
    )

    override suspend fun lpushAny(key: String, element: Any, vararg rest: Any): QueuedResponse<Long>
            = lpush(key, element.toString(), *rest.map(Any::toString).toTypedArray())

    override suspend fun rpushAny(key: String, element: Any, vararg rest: Any): QueuedResponse<Long>
            = rpush(key, element.toString(), *rest.map(Any::toString).toTypedArray())
}