package xyz.haff.siths.client

import kotlin.time.Duration

private data class CommandAndResponse<T>(val command: RedisCommand, val response: QueuedResponse<T>)

// TODO: Is PipelineBuilder the correct name? It's not simply a builder since it also executes it
class RedisPipelineBuilder(
    private val connection: SithsConnection,
    private val commandBuilder: RedisCommandBuilder = RedisCommandBuilder(),
): Siths<
        QueuedResponse<Unit>,
        QueuedResponse<String>,
        QueuedResponse<RespType<*>>,
        QueuedResponse<Long>,
        QueuedResponse<List<RedisClient>>,
        QueuedResponse<Duration?>,
        QueuedResponse<Set<String>>,
        QueuedResponse<RedisCursor<String>>,
        QueuedResponse<Boolean>
        > {
    private val operations = mutableListOf<CommandAndResponse<*>>()
    val length get() = operations.size

    suspend fun exec(inTransaction: Boolean = false): List<RespType<*>> {
        val pipeline = if (inTransaction) {
            RedisPipeline(commands = listOf(RedisCommand("MULTI"), *operations.map { it.command }.toTypedArray(), RedisCommand("EXEC")))
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

    override suspend fun set(key: String, value: Any, exclusiveMode: ExclusiveMode?, timeToLive: Duration?): QueuedResponse<Unit> {
        val operation = CommandAndResponse(commandBuilder.set(key, value, exclusiveMode, timeToLive), QueuedResponse(RespType<*>::toUnit))
        operations += operation
        return operation.response
    }

    override suspend fun get(key: String): QueuedResponse<String> {
        val operation = CommandAndResponse(commandBuilder.get(key), QueuedResponse(RespType<*>::toStringNonNull))
        operations += operation
        return operation.response
    }

    override suspend fun del(key: String, vararg rest: String): QueuedResponse<Long> {
        val operation = CommandAndResponse(commandBuilder.del(key, *rest), QueuedResponse(RespType<*>::toLong))
        operations += operation
        return operation.response
    }

    override suspend fun ttl(key: String): QueuedResponse<Duration?> {
        val operation = CommandAndResponse(commandBuilder.ttl(key), QueuedResponse(RespType<*>::toDurationOrNull))
        operations += operation
        return operation.response
    }

    override suspend fun scriptLoad(script: String): QueuedResponse<String> {
        val operation = CommandAndResponse(commandBuilder.scriptLoad(script), QueuedResponse(RespType<*>::toStringNonNull))
        operations += operation
        return operation.response
    }

    override suspend fun evalSha(sha: String, keys: List<String>, args: List<String>): QueuedResponse<RespType<*>> {
        val operation = CommandAndResponse(commandBuilder.evalSha(sha, keys, args), QueuedResponse(RespType<*>::throwOnError))
        operations += operation
        return operation.response
    }

    override suspend fun eval(script: String, keys: List<String>, args: List<String>): QueuedResponse<RespType<*>> {
        val operation = CommandAndResponse(commandBuilder.eval(script, keys, args), QueuedResponse(RespType<*>::throwOnError))
        operations += operation
        return operation.response
    }

    override suspend fun incrBy(key: String, value: Long): QueuedResponse<Long> {
        val operation = CommandAndResponse(commandBuilder.incrBy(key, value), QueuedResponse(RespType<*>::toLong))
        operations += operation
        return operation.response
    }

    override suspend fun exists(key: String, vararg rest: String): QueuedResponse<Boolean> {
        val operation = CommandAndResponse(commandBuilder.exists(key, *rest), QueuedResponse(converter = { it.existenceToBoolean(setOf(key, *rest).size.toLong()) }))
        operations += operation
        return operation.response
    }

    override suspend fun expire(
        key: String,
        duration: Duration,
        expirationCondition: ExpirationCondition?
    ): QueuedResponse<Boolean> {
        val operation = CommandAndResponse(commandBuilder.expire(key, duration, expirationCondition), QueuedResponse(RespType<*>::integerToBoolean))
        operations += operation
        return operation.response
    }

    override suspend fun sadd(key: String, value: Any, vararg rest: Any): QueuedResponse<Long> {
        val operation = CommandAndResponse(commandBuilder.sadd(key, value, *rest), QueuedResponse(RespType<*>::toLong))
        operations += operation
        return operation.response
    }

    override suspend fun smembers(key: String): QueuedResponse<Set<String>> {
        val operation = CommandAndResponse(commandBuilder.smembers(key), QueuedResponse(RespType<*>::toStringSet))
        operations += operation
        return operation.response
    }

    override suspend fun sismember(key: String, member: Any): QueuedResponse<Boolean> {
        val operation = CommandAndResponse(commandBuilder.sismember(key, member), QueuedResponse(RespType<*>::integerToBoolean))
        operations += operation
        return operation.response
    }

    override suspend fun scard(key: String): QueuedResponse<Long> {
        val operation = CommandAndResponse(commandBuilder.scard(key), QueuedResponse(RespType<*>::toLong))
        operations += operation
        return operation.response
    }

    override suspend fun srem(key: String, member: Any, vararg rest: Any): QueuedResponse<Long> {
        val operation = CommandAndResponse(commandBuilder.srem(key, member, *rest), QueuedResponse(RespType<*>::toLong))
        operations += operation
        return operation.response
    }

    override suspend fun sintercard(key: String, vararg rest: String, limit: Int?): QueuedResponse<Long> {
        val operation = CommandAndResponse(commandBuilder.sintercard(key, rest = rest, limit = limit), QueuedResponse(RespType<*>::toLong))
        operations += operation
        return operation.response
    }

    override suspend fun sdiffstore(destination: String, key: String, vararg rest: String): QueuedResponse<Long> {
        val operation = CommandAndResponse(commandBuilder.sdiffstore(destination, key, *rest), QueuedResponse(RespType<*>::toLong))
        operations += operation
        return operation.response
    }

    override suspend fun sinterstore(destination: String, key: String, vararg rest: String): QueuedResponse<Long> {
        val operation = CommandAndResponse(commandBuilder.sinterstore(destination, key, *rest), QueuedResponse(RespType<*>::toLong))
        operations += operation
        return operation.response
    }

    override suspend fun sscan(
        key: String,
        cursor: Long,
        match: String?,
        count: Int?
    ): QueuedResponse<RedisCursor<String>> {
        val operation = CommandAndResponse(commandBuilder.sscan(key, cursor, match, count), QueuedResponse(RespType<*>::toStringCursor))
        operations += operation
        return operation.response
    }

    override suspend fun clientList(): QueuedResponse<List<RedisClient>> {
        val operation = CommandAndResponse(commandBuilder.clientList(), QueuedResponse(RespType<*>::toClientList))
        operations += operation
        return operation.response
    }

    override suspend fun ping(): QueuedResponse<Boolean> {
        val operation = CommandAndResponse(commandBuilder.ping(), QueuedResponse(RespType<*>::pongToBoolean))
        operations += operation
        return operation.response
    }
}