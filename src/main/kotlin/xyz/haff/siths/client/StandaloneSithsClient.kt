package xyz.haff.siths.client

import xyz.haff.siths.common.handleUnexpectedRespResponse
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class StandaloneSithsClient(
    private val connection: SithsConnection,
    private val commandBuilder: RedisCommandBuilder = RedisCommandBuilder()
) : SithsClient {

    override suspend fun set(key: String, value: Any, exclusiveMode: ExclusiveMode?, timeToLive: Duration?) {
        val command = commandBuilder.set(key, value, exclusiveMode, timeToLive)

        val response = connection.runCommand(command)

        if (response is RespError) {
            response.throwAsException()
        }
    }

    override suspend fun ttl(key: String): Duration? =
        when (val response = connection.runCommand(commandBuilder.ttl(key))) {
            is RespInteger -> if (response.value < 0) {
                null
            } else {
                response.value.milliseconds
            }
            else -> handleUnexpectedRespResponse(response)
        }

    override suspend fun del(key: String, vararg rest: String): Long
        = when (val response = connection.runCommand(commandBuilder.del(key, *rest))) {
            is RespInteger -> response.value
            else -> handleUnexpectedRespResponse(response)
        }

    override suspend fun exists(key: String, vararg rest: String): Boolean
        = when (val response = connection.runCommand(commandBuilder.exists(key, *rest))) {
            // XXX: Redis returns the number of DIFFERENT keys that exist. Since our client's semantics is `true` if all exist
            // and `false` otherwise, we count the number of different keys and compare it to the response
            is RespInteger -> response.value == setOf(key, *rest).size.toLong()
            else -> handleUnexpectedRespResponse(response)
        }

    override suspend fun getOrNull(key: String): String? =
        when (val response = connection.runCommand(commandBuilder.get(key))) {
            is RespBulkString -> response.value
            is RespNullResponse -> null
            else -> handleUnexpectedRespResponse(response)
        }

    override suspend fun get(key: String): String = getOrNull(key) ?: throw RuntimeException("Key $key does not exist!")

    override suspend fun scriptLoad(script: String): String =
        when (val response = connection.runCommand(commandBuilder.scriptLoad(script))) {
            is RespBulkString -> response.value
            is RespSimpleString -> response.value
            else -> handleUnexpectedRespResponse(response)
        }

    override suspend fun evalSha(sha: String, keys: List<String>, args: List<String>): RespType<*> =
        when (val response = connection.runCommand(commandBuilder.evalSha(sha, keys, args))) {
            is RespError -> response.throwAsException()
            else -> response
        }

    override suspend fun eval(script: String, keys: List<String>, args: List<String>): RespType<*> =
        when (val response = connection.runCommand(commandBuilder.eval(script, keys, args))) {
            is RespError -> response.throwAsException()
            else -> response
        }

    override suspend fun incrBy(key: String, value: Long): Long =
        when (val response = connection.runCommand(commandBuilder.incrBy(key, value))) {
            is RespInteger -> response.value
            else -> handleUnexpectedRespResponse(response)
        }

    override suspend fun sadd(key: String, value: Any, vararg rest: Any): Long =
        when (val response = connection.runCommand(commandBuilder.sadd(key, value, *rest))) {
            is RespInteger -> response.value
            else -> handleUnexpectedRespResponse(response)
        }

    override suspend fun smembers(key: String): Set<String> =
        when (val response = connection.runCommand(commandBuilder.smembers(key))) {
            is RespArray -> response.value.map {
                if (it is RespBulkString) {
                    it.value
                } else {
                    handleUnexpectedRespResponse(it)
                }
            }.toSet()
            else -> handleUnexpectedRespResponse(response)
        }

    override suspend fun scard(key: String): Long
        = when (val response = connection.runCommand(commandBuilder.scard(key))) {
            is RespInteger -> response.value
            else -> handleUnexpectedRespResponse(response)
        }

    override suspend fun srem(key: String, member: Any, vararg rest: Any): Long
        = when (val response = connection.runCommand(commandBuilder.srem(key, member, *rest))) {
            is RespInteger -> response.value
            else -> handleUnexpectedRespResponse(response)
        }

    override suspend fun sintercard(key: String, vararg rest: String, limit: Int?): Long =
        when (val response = connection.runCommand(commandBuilder.sintercard(key, rest = rest, limit = limit))) {
            is RespInteger -> response.value
            else -> handleUnexpectedRespResponse(response)
        }

    override suspend fun sdiffstore(destination: String, key: String, vararg rest: String): Long
        = when (val response = connection.runCommand(commandBuilder.sdiffstore(destination, key, *rest))) {
            is RespInteger -> response.value
            else -> handleUnexpectedRespResponse(response)
        }

    override suspend fun sinterstore(destination: String, key: String, vararg rest: String): Long
        = when (val response = connection.runCommand(commandBuilder.sinterstore(destination, key, *rest))) {
            is RespInteger -> response.value
            else -> handleUnexpectedRespResponse(response)
        }

    override suspend fun sismember(key: String, member: Any): Boolean =
        when (val response = connection.runCommand(commandBuilder.sismember(key, member))) {
            is RespInteger -> response.value == 1L
            else -> handleUnexpectedRespResponse(response)
        }

    @Suppress("UNCHECKED_CAST") // Not actually unchecked, but Kotlin is not smart enough to notice
    override suspend fun sscan(key: String, cursor: Long, match: String?, count: Int?): RedisCursor<String> {
        val response = connection.runCommand(commandBuilder.sscan(key, cursor, match, count))

        return when {
            (response is RespArray)
                    && (response.value[0] is RespBulkString)
                    && (response.value[1] is RespArray)
                    && (response.value[1] as RespArray).value.all { it is RespBulkString }
            -> {
                RedisCursor(
                    next = (response.value[0] as RespBulkString).value.toLong(),
                    contents = ((response.value[1] as RespArray).value as List<RespBulkString>).map { it.value }
                )
            }
            else -> handleUnexpectedRespResponse(response)
        }
    }

    override suspend fun clientList(): List<RedisClient> =
        when (val response = connection.runCommand(commandBuilder.clientList())) {
            is RespBulkString -> parseClientList(response.value)
            else -> handleUnexpectedRespResponse(response)
        }

    override suspend fun ping(): Boolean {
        val response = connection.runCommand(commandBuilder.ping())

        return when {
            (response is RespSimpleString) && (response.value == "PONG") -> true
            else -> handleUnexpectedRespResponse(response)
        }
    }
}