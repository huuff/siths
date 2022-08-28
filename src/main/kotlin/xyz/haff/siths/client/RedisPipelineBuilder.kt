package xyz.haff.siths.client

import kotlin.time.Duration

private data class Operation<T>(val command: RedisCommand, val response: QueuedResponse<T>)

// TODO: Is PipelineBuilder the correct name? It's not simply a builder since it also executes it
class RedisPipelineBuilder(
    // TODO: Surely we shouldn't take the connection at the constructor, but only at execution! otherwise we'd be locking a
    // connection without actually using it
    private val connection: SithsConnection,
    private val commandBuilder: RedisCommandBuilder = RedisCommandBuilder(),
) : Siths<
        QueuedResponse<Unit>,
        QueuedResponse<String>,
        QueuedResponse<String?>,
        QueuedResponse<RespType<*>>,
        QueuedResponse<Long>,
        QueuedResponse<Long?>,
        QueuedResponse<List<Long>>,
        QueuedResponse<List<RedisClient>>,
        QueuedResponse<Duration?>,
        QueuedResponse<Set<String>>,
        QueuedResponse<RedisCursor<String>>,
        QueuedResponse<Boolean>,
        QueuedResponse<Map<String, Boolean>>,
        QueuedResponse<List<String>>
        > {
    private val operations = mutableListOf<Operation<*>>()
    val length get() = operations.size

    private fun <T> addOperation(operation: Operation<T>): QueuedResponse<T> {
        operations += operation
        return operation.response
    }

    suspend fun exec(inTransaction: Boolean = false): List<RespType<*>> {
        val pipeline = if (inTransaction) {
            RedisPipeline(
                commands = listOf(
                    RedisCommand("MULTI"),
                    *operations.map { it.command }.toTypedArray(),
                    RedisCommand("EXEC")
                )
            )
        } else {
            RedisPipeline(commands = operations.map { it.command })
        }
        val queuedResponses = operations.map { it.response }
        val actualResponses = if (inTransaction) {
            val results = connection.runPipeline(pipeline)
            val multiResponse = results[1 + operations.size] // Skip all QUEUED responses, and the OK for the multi
            if (multiResponse is RespArray) {
                multiResponse.value // It must be a RespArray, since that's the specification for an EXEC
            } else {
                multiResponse.handleAsUnexpected()
            }
        } else {
            connection.runPipeline(pipeline)
        }

        (queuedResponses zip actualResponses).forEach { (queuedResponse, actualResponse) ->
            queuedResponse.set(actualResponse)
        }

        return actualResponses
    }

    override suspend fun set(
        key: String,
        value: Any,
        exclusiveMode: ExclusiveMode?,
        timeToLive: Duration?
    ): QueuedResponse<Unit> = addOperation(
        Operation(
            command = commandBuilder.set(key, value, exclusiveMode, timeToLive),
            response = QueuedResponse(RespType<*>::toUnit)
        )
    )

    override suspend fun get(key: String): QueuedResponse<String> {
        return addOperation(Operation(commandBuilder.get(key), QueuedResponse(RespType<*>::toStringNonNull)))
    }

    override suspend fun del(key: String, vararg rest: String): QueuedResponse<Long> =
        addOperation(Operation(commandBuilder.del(key, *rest), QueuedResponse(RespType<*>::toLong)))

    override suspend fun ttl(key: String): QueuedResponse<Duration?> =
        addOperation(Operation(commandBuilder.ttl(key), QueuedResponse(RespType<*>::toDurationOrNull)))

    override suspend fun scriptLoad(script: String): QueuedResponse<String> = addOperation(
        Operation(
            command = commandBuilder.scriptLoad(script),
            response = QueuedResponse(RespType<*>::toStringNonNull)
        )
    )

    override suspend fun evalSha(sha: String, keys: List<String>, args: List<String>): QueuedResponse<RespType<*>> =
        addOperation(
            Operation(
                command = commandBuilder.evalSha(sha, keys, args),
                response = QueuedResponse(RespType<*>::throwOnError)
            )
        )

    override suspend fun eval(script: String, keys: List<String>, args: List<String>): QueuedResponse<RespType<*>> =
        addOperation(
            Operation(
                command = commandBuilder.eval(script, keys, args),
                response = QueuedResponse(RespType<*>::throwOnError)
            )
        )

    override suspend fun incrBy(key: String, value: Long): QueuedResponse<Long> =
        addOperation(Operation(commandBuilder.incrBy(key, value), QueuedResponse(RespType<*>::toLong)))

    override suspend fun exists(key: String, vararg rest: String): QueuedResponse<Boolean> {
        return addOperation(
            Operation(
                command = commandBuilder.exists(key, *rest),
                response = QueuedResponse(converter = { it.existenceToBoolean(setOf(key, *rest).size.toLong()) })
            )
        )
    }

    override suspend fun expire(
        key: String,
        duration: Duration,
        expirationCondition: ExpirationCondition?
    ): QueuedResponse<Boolean> = addOperation(
        Operation(
            command = commandBuilder.expire(key, duration, expirationCondition),
            response = QueuedResponse(RespType<*>::integerToBoolean)
        )
    )

    override suspend fun sadd(key: String, value: Any, vararg rest: Any): QueuedResponse<Long> = addOperation(
        Operation(
            command = commandBuilder.sadd(key, value, *rest),
            response = QueuedResponse(RespType<*>::toLong)
        )
    )

    override suspend fun smembers(key: String): QueuedResponse<Set<String>> =
        addOperation(Operation(commandBuilder.smembers(key), QueuedResponse(RespType<*>::toStringSet)))

    override suspend fun sismember(key: String, member: Any): QueuedResponse<Boolean> = addOperation(
        Operation(
            command = commandBuilder.sismember(key, member),
            response = QueuedResponse(RespType<*>::integerToBoolean)
        )
    )

    override suspend fun scard(key: String): QueuedResponse<Long> =
        addOperation(Operation(commandBuilder.scard(key), QueuedResponse(RespType<*>::toLong)))

    override suspend fun srem(key: String, member: Any, vararg rest: Any): QueuedResponse<Long> = addOperation(
        Operation(
            command = commandBuilder.srem(key, member, *rest),
            response = QueuedResponse(RespType<*>::toLong)
        )
    )

    override suspend fun sintercard(key: String, vararg rest: String, limit: Int?): QueuedResponse<Long> = addOperation(
        Operation(
            command = commandBuilder.sintercard(key, rest = rest, limit = limit),
            response = QueuedResponse(RespType<*>::toLong)
        )
    )

    override suspend fun sdiffstore(destination: String, key: String, vararg rest: String): QueuedResponse<Long> =
        addOperation(
            Operation(
                command = commandBuilder.sdiffstore(destination, key, *rest),
                response = QueuedResponse(RespType<*>::toLong)
            )
        )

    override suspend fun sinterstore(destination: String, key: String, vararg rest: String): QueuedResponse<Long> =
        addOperation(
            Operation(
                command = commandBuilder.sinterstore(destination, key, *rest),
                response = QueuedResponse(RespType<*>::toLong)
            )
        )

    override suspend fun sscan(
        key: String,
        cursor: Long,
        match: String?,
        count: Int?
    ): QueuedResponse<RedisCursor<String>> = addOperation(
        Operation(
            command = commandBuilder.sscan(key, cursor, match, count),
            response = QueuedResponse(RespType<*>::toStringCursor)
        )
    )

    override suspend fun clientList(): QueuedResponse<List<RedisClient>> =
        addOperation(Operation(commandBuilder.clientList(), QueuedResponse(RespType<*>::toClientList)))

    override suspend fun ping(): QueuedResponse<Boolean> =
        addOperation(Operation(commandBuilder.ping(), QueuedResponse(RespType<*>::pongToBoolean)))

    // SET OPERATIONS

    override suspend fun sdiff(key: String, vararg rest: String): QueuedResponse<Set<String>> = addOperation(
        Operation(
            command = commandBuilder.sdiff(key, *rest),
            response = QueuedResponse(RespType<*>::toStringSet)
        )
    )

    override suspend fun sinter(key: String, vararg rest: String): QueuedResponse<Set<String>> = addOperation(
        Operation(
            command = commandBuilder.sinter(key, *rest),
            response = QueuedResponse(RespType<*>::toStringSet)
        )
    )

    override suspend fun smove(source: String, destination: String, member: Any): QueuedResponse<Boolean> =
        addOperation(
            Operation(
                command = commandBuilder.smove(source, destination, member),
                response = QueuedResponse(RespType<*>::integerToBoolean)
            )
        )

    override suspend fun spop(key: String): QueuedResponse<String?> =
        addOperation(
            Operation(
                command = commandBuilder.spop(key),
                response = QueuedResponse(RespType<*>::toStringOrNull)
            )
        )

    override suspend fun spop(key: String, count: Int?): QueuedResponse<Set<String>> = addOperation(
        Operation(
            command = commandBuilder.spop(key, count),
            response = QueuedResponse(RespType<*>::bulkOrArrayToStringSet)
        )
    )

    override suspend fun srandmember(key: String): QueuedResponse<String?> = addOperation(
        Operation(
            command = commandBuilder.srandmember(key),
            response = QueuedResponse(RespType<*>::toStringOrNull)
        )
    )

    override suspend fun srandmember(key: String, count: Int?): QueuedResponse<Set<String>> {
        return addOperation(
            Operation(
                command = commandBuilder.srandmember(key, count),
                response = QueuedResponse(RespType<*>::bulkOrArrayToStringSet)
            )
        )
    }

    override suspend fun sunion(key: String, vararg rest: String): QueuedResponse<Set<String>> = addOperation(
        Operation(
            command = commandBuilder.sunion(key, *rest),
            response = QueuedResponse(RespType<*>::toStringSet)
        )
    )

    override suspend fun sunionstore(destination: String, key: String, vararg rest: String): QueuedResponse<Long> =
        addOperation(
            Operation(
                commandBuilder.sunionstore(destination, key, *rest),
                QueuedResponse(RespType<*>::toLong)
            )
        )

    override suspend fun smismember(
        key: String,
        member: Any,
        vararg rest: Any
    ): QueuedResponse<Map<String, Boolean>> = addOperation(
        Operation(
            commandBuilder.smismember(key, member, *rest),
            QueuedResponse(converter = { it.toStringToBooleanMap(member, *rest) })
        )
    )

    // LIST OPERATIONS

    override suspend fun llen(key: String): QueuedResponse<Long> =
        addOperation(Operation(commandBuilder.llen(key), QueuedResponse(RespType<*>::toLong)))

    override suspend fun lindex(key: String, index: Int): QueuedResponse<String?> =
        addOperation(Operation(commandBuilder.lindex(key, index), QueuedResponse(RespType<*>::toStringOrNull)))

    override suspend fun linsert(
        key: String,
        relativePosition: RelativePosition,
        pivot: Any,
        element: Any
    ): QueuedResponse<Long?> =
        addOperation(
            Operation(
                command = commandBuilder.linsert(key, relativePosition, pivot, element),
                response = QueuedResponse(RespType<*>::toPositiveLongOrNull)
            )
        )

    override suspend fun lpop(key: String, count: Int): QueuedResponse<List<String>> = addOperation(
        Operation(
            command = commandBuilder.lpop(key, count),
            response = QueuedResponse(RespType<*>::bulkOrArrayToStringList)
        )
    )

    override suspend fun lpop(key: String): QueuedResponse<String?> =
        addOperation(Operation(commandBuilder.lpop(key), QueuedResponse(RespType<*>::toStringOrNull)))

    override suspend fun rpop(key: String, count: Int): QueuedResponse<List<String>> = addOperation(
        Operation(
            command = commandBuilder.rpop(key, count),
            response = QueuedResponse(RespType<*>::bulkOrArrayToStringList)
        )
    )

    override suspend fun rpop(key: String): QueuedResponse<String?> = addOperation(
        Operation(
            command = commandBuilder.rpop(key),
            response = QueuedResponse(RespType<*>::toStringOrNull)
        )
    )

    override suspend fun lpush(key: String, element: Any, vararg rest: Any): QueuedResponse<Long> =
        addOperation(Operation(commandBuilder.lpush(key, element, *rest), QueuedResponse(RespType<*>::toLong)))

    override suspend fun rpush(key: String, element: Any, vararg rest: Any): QueuedResponse<Long> =
        addOperation(Operation(commandBuilder.rpush(key, element, *rest), QueuedResponse(RespType<*>::toLong)))

    override suspend fun lrem(key: String, element: Any, count: Int): QueuedResponse<Long> =
        addOperation(Operation(commandBuilder.lrem(key, element, count), QueuedResponse(RespType<*>::toLong)))

    override suspend fun lrange(key: String, start: Int, stop: Int): QueuedResponse<List<String>> = addOperation(
        Operation(
            command = commandBuilder.lrange(key, start, stop),
            response = QueuedResponse(RespType<*>::toStringList)
        )
    )

    override suspend fun lpos(key: String, element: Any, rank: Int?, maxlen: Int?): QueuedResponse<Long?> =
        addOperation(
            Operation(
                command = commandBuilder.lpos(key, element, rank, maxlen),
                response = QueuedResponse(RespType<*>::toLongOrNull)
            )
        )

    override suspend fun lpos(
        key: String,
        element: Any,
        rank: Int?,
        count: Int,
        maxlen: Int?
    ): QueuedResponse<List<Long>> = addOperation(
        Operation(
            command = commandBuilder.lpos(key, element, rank, count, maxlen),
            response = QueuedResponse(RespType<*>::toLongList)
        )
    )

}