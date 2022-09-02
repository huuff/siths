package xyz.haff.siths.pipelining

import xyz.haff.siths.command.RedisCommandBuilder
import xyz.haff.siths.common.mapSecond
import xyz.haff.siths.protocol.*

class PipelinedHashSithsClient(
    private val executor: RedisPipelineExecutor = RedisPipelineExecutor(),
    private val commandBuilder: RedisCommandBuilder = RedisCommandBuilder(),
) : IPipelinedHashSithsClient {

    // HASH OPERATIONS
    override suspend fun hget(key: String, field: String): QueuedResponseImpl<String> = executor.addOperation(
        DeferredCommand(
            command = commandBuilder.hget(key, field),
            response = QueuedResponseImpl(RespType<*>::toStringNonNull)
        )
    )

    override suspend fun hset(
        key: String,
        pair: Pair<String, String>,
        vararg rest: Pair<String, String>
    ): QueuedResponseImpl<Long> = executor.addOperation(
        DeferredCommand(
            command = commandBuilder.hset(key, pair, *rest),
            response = QueuedResponseImpl(RespType<*>::toLong)
        )
    )

    override suspend fun hsetAny(
        key: String,
        pair: Pair<String, Any>,
        vararg rest: Pair<String, Any>
    ): QueuedResponseImpl<Long> = executor.addOperation(
        DeferredCommand(
            command = commandBuilder.hset(
                key,
                pair.mapSecond(Any::toString),
                *rest.map { it.mapSecond(Any::toString) }.toTypedArray()
            ),
            response = QueuedResponseImpl(RespType<*>::toLong)
        )
    )

    override suspend fun hgetOrNull(key: String, field: String): QueuedResponseImpl<String?> = executor.addOperation(
        DeferredCommand(
            command = commandBuilder.hget(key, field),
            response = QueuedResponseImpl(RespType<*>::toStringOrNull)
        )
    )

    override suspend fun hgetAll(key: String): QueuedResponseImpl<Map<String, String>> = executor.addOperation(
        DeferredCommand(
            command = commandBuilder.hgetAll(key),
            response = QueuedResponseImpl(RespType<*>::toStringMap)
        )
    )

    override suspend fun hkeys(key: String): QueuedResponseImpl<List<String>> = executor.addOperation(
        DeferredCommand(
            command = commandBuilder.hkeys(key),
            response = QueuedResponseImpl(RespType<*>::toStringList)
        )
    )

    override suspend fun hvals(key: String): QueuedResponseImpl<List<String>> = executor.addOperation(
        DeferredCommand(
            command = commandBuilder.hvals(key),
            response = QueuedResponseImpl(RespType<*>::toStringList)
        )
    )

    override suspend fun hexists(key: String, field: String): QueuedResponseImpl<Boolean> = executor.addOperation(
        DeferredCommand(
            command = commandBuilder.hexists(key, field),
            response = QueuedResponseImpl(RespType<*>::integerToBoolean)
        )
    )

    override suspend fun hincrBy(key: String, field: String, increment: Long): QueuedResponseImpl<Long> = executor.addOperation(
        DeferredCommand(
            command = commandBuilder.hincrBy(key, field, increment),
            response = QueuedResponseImpl(RespType<*>::toLong)
        )
    )

    override suspend fun hincrByFloat(key: String, field: String, increment: Double): QueuedResponseImpl<Double> =
        executor.addOperation(
            DeferredCommand(
                command = commandBuilder.hincrByFloat(key, field, increment),
                response = QueuedResponseImpl(RespType<*>::toDouble)
            )
        )

    override suspend fun hmget(key: String, field: String, vararg rest: String): QueuedResponseImpl<Map<String, String>> =
        executor.addOperation(
            DeferredCommand(
                command = commandBuilder.hmget(key, field, *rest),
                response = QueuedResponseImpl({ it.associateArrayToArguments(field, *rest) })
            )
        )

    override suspend fun hlen(key: String): QueuedResponseImpl<Long> = executor.addOperation(
        DeferredCommand(
            command = commandBuilder.hlen(key),
            response = QueuedResponseImpl(RespType<*>::toLong)
        )
    )

    override suspend fun hdel(key: String, field: String, vararg rest: String): QueuedResponseImpl<Long> = executor.addOperation(
        DeferredCommand(
            command = commandBuilder.hdel(key, field, *rest),
            response = QueuedResponseImpl(RespType<*>::toLong)
        )
    )

    override suspend fun hstrLen(key: String, field: String): QueuedResponseImpl<Long> = executor.addOperation(
        DeferredCommand(
            command = commandBuilder.hstrLen(key, field),
            response = QueuedResponseImpl(RespType<*>::toLong)
        )
    )

    override suspend fun hsetnx(key: String, field: String, value: String): QueuedResponseImpl<Boolean> = executor.addOperation(
        DeferredCommand(
            command = commandBuilder.hsetnx(key, field, value),
            response = QueuedResponseImpl(RespType<*>::integerToBoolean)
        )
    )

    override suspend fun hscan(
        key: String,
        cursor: Long,
        match: String?,
        count: Int?
    ): QueuedResponseImpl<RedisCursor<Pair<String, String>>> = executor.addOperation(
        DeferredCommand(
            command = commandBuilder.hscan(key, cursor, match, count),
            response = QueuedResponseImpl(RespType<*>::toStringPairCursor)
        )
    )

    // HRANDFIELD
    override suspend fun hrandField(key: String): QueuedResponseImpl<String?> = executor.addOperation(
        DeferredCommand(
            command = commandBuilder.hrandField(key),
            response = QueuedResponseImpl(RespType<*>::toStringOrNull)
        )
    )

    override suspend fun hrandField(key: String, count: Int): QueuedResponseImpl<List<String>> = executor.addOperation(
        DeferredCommand(
            command = commandBuilder.hrandField(key, count),
            response = QueuedResponseImpl(RespType<*>::toStringList)
        )
    )

    override suspend fun hrandFieldWithValues(key: String, count: Int): QueuedResponseImpl<Map<String, String>> = executor.addOperation(
        DeferredCommand(
            command = commandBuilder.hrandFieldWithValues(key, count),
            response = QueuedResponseImpl(RespType<*>::toStringMap)
        )
    )
}