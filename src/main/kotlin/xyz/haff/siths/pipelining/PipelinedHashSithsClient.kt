package xyz.haff.siths.pipelining

import xyz.haff.siths.command.RedisCommandBuilder
import xyz.haff.siths.common.mapSecond
import xyz.haff.siths.protocol.*
import kotlin.text.toDouble

class PipelinedHashSithsClient(
    private val executor: RedisPipelineExecutor = RedisPipelineExecutor(),
    private val commandBuilder: RedisCommandBuilder = RedisCommandBuilder(),
) : IPipelinedHashSithsClient {

    // HASH OPERATIONS
    override suspend fun hget(key: String, field: String): QueuedResponse<String> = executor.addOperation(
        DeferredCommand(
            command = commandBuilder.hget(key, field),
            response = QueuedResponse(RespType<*>::toStringNonNull)
        )
    )

    override suspend fun hset(
        key: String,
        pair: Pair<String, String>,
        vararg rest: Pair<String, String>
    ): QueuedResponse<Long> = executor.addOperation(
        DeferredCommand(
            command = commandBuilder.hset(key, pair, *rest),
            response = QueuedResponse(RespType<*>::toLong)
        )
    )

    override suspend fun hsetAny(
        key: String,
        pair: Pair<String, Any>,
        vararg rest: Pair<String, Any>
    ): QueuedResponse<Long> = executor.addOperation(
        DeferredCommand(
            command = commandBuilder.hset(
                key,
                pair.mapSecond(Any::toString),
                *rest.map { it.mapSecond(Any::toString) }.toTypedArray()
            ),
            response = QueuedResponse(RespType<*>::toLong)
        )
    )

    override suspend fun hgetOrNull(key: String, field: String): QueuedResponse<String?> = executor.addOperation(
        DeferredCommand(
            command = commandBuilder.hget(key, field),
            response = QueuedResponse(RespType<*>::toStringOrNull)
        )
    )

    override suspend fun hgetall(key: String): QueuedResponse<Map<String, String>> = executor.addOperation(
        DeferredCommand(
            command = commandBuilder.hgetall(key),
            response = QueuedResponse(RespType<*>::toStringMap)
        )
    )

    override suspend fun hkeys(key: String): QueuedResponse<List<String>> = executor.addOperation(
        DeferredCommand(
            command = commandBuilder.hkeys(key),
            response = QueuedResponse(RespType<*>::toStringList)
        )
    )

    override suspend fun hvals(key: String): QueuedResponse<List<String>> = executor.addOperation(
        DeferredCommand(
            command = commandBuilder.hvals(key),
            response = QueuedResponse(RespType<*>::toStringList)
        )
    )

    override suspend fun hexists(key: String, field: String): QueuedResponse<Boolean> = executor.addOperation(
        DeferredCommand(
            command = commandBuilder.hexists(key, field),
            response = QueuedResponse(RespType<*>::integerToBoolean)
        )
    )

    override suspend fun hincrby(key: String, field: String, increment: Long): QueuedResponse<Long> = executor.addOperation(
        DeferredCommand(
            command = commandBuilder.hincrby(key, field, increment),
            response = QueuedResponse(RespType<*>::toLong)
        )
    )

    override suspend fun hincrbyfloat(key: String, field: String, increment: Double): QueuedResponse<Double> =
        executor.addOperation(
            DeferredCommand(
                command = commandBuilder.hincrbyfloat(key, field, increment),
                response = QueuedResponse(RespType<*>::toDouble)
            )
        )

    override suspend fun hmget(key: String, field: String, vararg rest: String): QueuedResponse<Map<String, String>> =
        executor.addOperation(
            DeferredCommand(
                command = commandBuilder.hmget(key, field, *rest),
                response = QueuedResponse({ it.associateArrayToArguments(field, *rest) })
            )
        )

    override suspend fun hlen(key: String): QueuedResponse<Long> = executor.addOperation(
        DeferredCommand(
            command = commandBuilder.hlen(key),
            response = QueuedResponse(RespType<*>::toLong)
        )
    )

    override suspend fun hdel(key: String, field: String, vararg rest: String): QueuedResponse<Long> = executor.addOperation(
        DeferredCommand(
            command = commandBuilder.hdel(key, field, *rest),
            response = QueuedResponse(RespType<*>::toLong)
        )
    )

    override suspend fun hstrlen(key: String, field: String): QueuedResponse<Long> = executor.addOperation(
        DeferredCommand(
            command = commandBuilder.hstrlen(key, field),
            response = QueuedResponse(RespType<*>::toLong)
        )
    )

    override suspend fun hsetnx(key: String, field: String, value: String): QueuedResponse<Boolean> = executor.addOperation(
        DeferredCommand(
            command = commandBuilder.hsetnx(key, field, value),
            response = QueuedResponse(RespType<*>::integerToBoolean)
        )
    )

    override suspend fun hscan(
        key: String,
        cursor: Long,
        match: String?,
        count: Int?
    ): QueuedResponse<RedisCursor<Pair<String, String>>> = executor.addOperation(
        DeferredCommand(
            command = commandBuilder.hscan(key, cursor, match, count),
            response = QueuedResponse(RespType<*>::toStringPairCursor)
        )
    )

    // HRANDFIELD
    override suspend fun hrandfield(key: String): QueuedResponse<String?> = executor.addOperation(
        DeferredCommand(
            command = commandBuilder.hrandfield(key),
            response = QueuedResponse(RespType<*>::toStringOrNull)
        )
    )

    override suspend fun hrandfield(key: String, count: Int): QueuedResponse<List<String>> = executor.addOperation(
        DeferredCommand(
            command = commandBuilder.hrandfield(key, count),
            response = QueuedResponse(RespType<*>::toStringList)
        )
    )

    override suspend fun hrandfieldWithValues(key: String, count: Int): QueuedResponse<Map<String, String>> = executor.addOperation(
        DeferredCommand(
            command = commandBuilder.hrandfieldWithValues(key, count),
            response = QueuedResponse(RespType<*>::toStringMap)
        )
    )
}