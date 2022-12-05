package xyz.haff.siths.pipelining

import xyz.haff.siths.command.RedisCommandBuilder
import xyz.haff.siths.option.ListEnd
import xyz.haff.siths.option.RelativePosition
import xyz.haff.siths.protocol.*
import kotlin.time.Duration

class PipelinedListSithsClient(
    private val executor: RedisPipelineExecutor = RedisPipelineExecutor(),
    private val commandBuilder: RedisCommandBuilder = RedisCommandBuilder(),
): IPipelinedListSithsClient {

    override suspend fun llen(key: String): QueuedResponseImpl<Long> =
        executor.addOperation(DeferredCommand(commandBuilder.llen(key), QueuedResponseImpl(RespType<*>::toLong)))

    override suspend fun lindex(key: String, index: Int): QueuedResponseImpl<String?> =
        executor.addOperation(DeferredCommand(commandBuilder.lindex(key, index), QueuedResponseImpl(RespType<*>::toStringOrNull)))

    override suspend fun linsert(
        key: String,
        relativePosition: RelativePosition,
        pivot: String,
        element: String
    ): QueuedResponseImpl<Long?> =
        executor.addOperation(
            DeferredCommand(
                command = commandBuilder.linsert(key, relativePosition, pivot, element),
                response = QueuedResponseImpl(RespType<*>::toPositiveLongOrNull)
            )
        )

    override suspend fun lpop(key: String, count: Int?): QueuedResponseImpl<List<String>> = executor.addOperation(
        DeferredCommand(
            command = commandBuilder.lpop(key, count),
            response = QueuedResponseImpl(RespType<*>::bulkOrArrayToStringList)
        )
    )

    override suspend fun lpop(key: String): QueuedResponseImpl<String?> =
        executor.addOperation(DeferredCommand(commandBuilder.lpop(key), QueuedResponseImpl(RespType<*>::toStringOrNull)))

    override suspend fun lmpop(
        keys: List<String>,
        end: ListEnd,
        count: Int?
    ): QueuedResponseImpl<SourceAndData<List<String>>?> = executor.addOperation(
        DeferredCommand(
            command = commandBuilder.lmpop(keys, end, count),
            response = QueuedResponseImpl(RespType<*>::toSourceAndStringListOrNull)
        )
    )

    override suspend fun lmpop(key: String, end: ListEnd, count: Int?): QueuedResponseImpl<List<String>> = executor.addOperation(
        DeferredCommand(
            command = commandBuilder.lmpop(key, end, count),
            response = QueuedResponseImpl({ it.toSourceAndStringListOrNull()?.data ?: listOf() })
        )
    )

    override suspend fun rpop(key: String, count: Int?): QueuedResponseImpl<List<String>> = executor.addOperation(
        DeferredCommand(
            command = commandBuilder.rpop(key, count),
            response = QueuedResponseImpl(RespType<*>::bulkOrArrayToStringList)
        )
    )

    override suspend fun rpop(key: String): QueuedResponseImpl<String?> = executor.addOperation(
        DeferredCommand(
            command = commandBuilder.rpop(key),
            response = QueuedResponseImpl(RespType<*>::toStringOrNull)
        )
    )

    override suspend fun lpush(key: String, elements: Collection<String>): QueuedResponse<Long>
        = executor.addOperation(DeferredCommand(commandBuilder.lpush(key, elements), QueuedResponseImpl(RespType<*>::toLong)))

    override suspend fun lpush(key: String, element: String, vararg rest: String): QueuedResponseImpl<Long> =
        executor.addOperation(DeferredCommand(commandBuilder.lpush(key, element, *rest), QueuedResponseImpl(RespType<*>::toLong)))

    override suspend fun lpushx(key: String, element: String, vararg rest: String): QueuedResponseImpl<Long> =
        executor.addOperation(DeferredCommand(commandBuilder.lpushx(key, element, *rest), QueuedResponseImpl(RespType<*>::toLong)))

    override suspend fun rpush(key: String, elements: Collection<String>): QueuedResponse<Long>
        = executor.addOperation(DeferredCommand(commandBuilder.rpush(key, elements), QueuedResponseImpl(RespType<*>::toLong)))

    override suspend fun rpush(key: String, element: String, vararg rest: String): QueuedResponseImpl<Long> =
        executor.addOperation(DeferredCommand(commandBuilder.rpush(key, element, *rest), QueuedResponseImpl(RespType<*>::toLong)))

    override suspend fun rpushx(key: String, element: String, vararg rest: String): QueuedResponseImpl<Long> =
        executor.addOperation(DeferredCommand(commandBuilder.rpushx(key, element, *rest), QueuedResponseImpl(RespType<*>::toLong)))

    override suspend fun lrem(key: String, element: String, count: Int): QueuedResponseImpl<Long> =
        executor.addOperation(DeferredCommand(commandBuilder.lrem(key, element, count), QueuedResponseImpl(RespType<*>::toLong)))

    override suspend fun lrange(key: String, start: Int, stop: Int): QueuedResponseImpl<List<String>> = executor.addOperation(
        DeferredCommand(
            command = commandBuilder.lrange(key, start, stop),
            response = QueuedResponseImpl(RespType<*>::toStringList)
        )
    )

    override suspend fun lpos(key: String, element: String, rank: Int?, maxlen: Int?): QueuedResponseImpl<Long?> =
        executor.addOperation(
            DeferredCommand(
                command = commandBuilder.lpos(key, element, rank, maxlen),
                response = QueuedResponseImpl(RespType<*>::toLongOrNull)
            )
        )

    override suspend fun lpos(
        key: String,
        element: String,
        rank: Int?,
        count: Int,
        maxlen: Int?
    ): QueuedResponseImpl<List<Long>> = executor.addOperation(
        DeferredCommand(
            command = commandBuilder.lpos(key, element, rank, count, maxlen),
            response = QueuedResponseImpl(RespType<*>::toLongList)
        )
    )

    override suspend fun lset(key: String, index: Int, element: String): QueuedResponseImpl<Boolean> = executor.addOperation(
        DeferredCommand(
            command = commandBuilder.lset(key, index, element),
            response = QueuedResponseImpl(RespType<*>::isOk)
        )
    )

    override suspend fun ltrim(key: String, start: Int, stop: Int): QueuedResponseImpl<Unit> = executor.addOperation(
        DeferredCommand(
            command = commandBuilder.ltrim(key, start, stop),
            response = QueuedResponseImpl(RespType<*>::assertOk)
        )
    )

    override suspend fun lmove(
        source: String,
        destination: String,
        sourceEnd: ListEnd,
        destinationEnd: ListEnd
    ): QueuedResponseImpl<String> = executor.addOperation(
        DeferredCommand(
            command = commandBuilder.lmove(source, destination, sourceEnd, destinationEnd),
            response = QueuedResponseImpl(RespType<*>::toStringNonNull)
        )
    )

    override suspend fun blmpop(
        timeout: Duration,
        key: String,
        vararg otherKeys: String,
        end: ListEnd,
        count: Int?
    ): QueuedResponseImpl<SourceAndData<List<String>>?> = executor.addOperation(
        DeferredCommand(
            command = commandBuilder.blmpop(timeout, key, otherKeys = otherKeys, end = end, count),
            response = QueuedResponseImpl(RespType<*>::toSourceAndStringListOrNull)
        )
    )

    override suspend fun brpop(
        key: String,
        vararg otherKeys: String,
        timeout: Duration?
    ): QueuedResponseImpl<SourceAndData<String>?> = executor.addOperation(
        DeferredCommand(
            command = commandBuilder.brpop(key, otherKeys = otherKeys, timeout),
            response = QueuedResponseImpl(RespType<*>::toSourceAndStringOrNull)
        )
    )

    override suspend fun blpop(
        key: String,
        vararg otherKeys: String,
        timeout: Duration?
    ): QueuedResponseImpl<SourceAndData<String>?> = executor.addOperation(
        DeferredCommand(
            command = commandBuilder.blpop(key, otherKeys = otherKeys, timeout),
            response = QueuedResponseImpl(RespType<*>::toSourceAndStringOrNull)
        )
    )

    override suspend fun blmove(
        source: String,
        destination: String,
        sourceEnd: ListEnd,
        destinationEnd: ListEnd,
        timeout: Duration?
    ): QueuedResponseImpl<String?> = executor.addOperation(
        DeferredCommand(
            command = commandBuilder.blmove(source, destination, sourceEnd, destinationEnd, timeout),
            response = QueuedResponseImpl(RespType<*>::toStringOrNull)
        )
    )

    override suspend fun lpushAny(key: String, element: Any, vararg rest: Any): QueuedResponseImpl<Long>
            = lpush(key, element.toString(), *rest.map(Any::toString).toTypedArray())

    override suspend fun rpushAny(key: String, element: Any, vararg rest: Any): QueuedResponseImpl<Long>
            = rpush(key, element.toString(), *rest.map(Any::toString).toTypedArray())
}