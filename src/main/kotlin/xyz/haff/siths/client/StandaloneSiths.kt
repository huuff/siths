package xyz.haff.siths.client

import xyz.haff.siths.common.RedisUnexpectedRespResponse
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

@JvmInline
value class StandaloneSiths(
    private val connection: SithsConnection,
): Siths {

    override suspend fun set(key: String, value: Any, exclusiveMode: ExclusiveMode?, timeToLive: Duration?) {
        val ttlSubCommand = timeToLive?.let { RedisCommand("PX", timeToLive.inWholeMilliseconds.toString()) } ?: RedisCommand()

        val response = connection.runCommand(RedisCommand("SET", key, value.toString(), exclusiveMode?.name) + ttlSubCommand)

        if (response is RespError) {
            response.throwAsException()
        }
    }

    override suspend fun ttl(key: String): Duration? = when (val response = connection.runCommand(RedisCommand("PTTL", key))) {
            is RespInteger -> if (response.value < 0) { null } else { response.value.milliseconds }
            is RespError -> response.throwAsException()
            else -> throw RedisUnexpectedRespResponse(response)
        }

    override suspend fun getOrNull(key: String): String? = when (val response = connection.runCommand(RedisCommand("GET", key))) {
        is RespBulkString -> response.value
        is RespNullResponse -> null
        is RespError -> response.throwAsException()
        else -> throw RedisUnexpectedRespResponse(response)
    }

    override suspend fun get(key: String): String = getOrNull(key) ?: throw RuntimeException("Key $key does not exist!")

    override suspend fun scriptLoad(script: String): String =
        when (val response = connection.runCommand(RedisCommand("SCRIPT", "LOAD", script))) {
            is RespBulkString -> response.value
            is RespSimpleString -> response.value
            is RespError -> response.throwAsException()
            else -> throw RedisUnexpectedRespResponse(response)
        }

    override suspend fun evalSha(sha: String, keys: List<String>, args: List<String>): RespType<*> =
        when (val response = connection.runCommand(RedisCommand("EVALSHA", sha, keys.size.toString(), *keys.toTypedArray(), *args.toTypedArray()))) {
            is RespError -> response.throwAsException()
            else -> response
        }

    override suspend fun incrBy(key: String, value: Long) = when(val response = connection.runCommand(RedisCommand("INCRBY", key, value.toString()))) {
        is RespInteger -> response.value
        else -> throw RedisUnexpectedRespResponse(response)
    }

    override suspend fun clientList(): List<RedisClient> = when(val response = connection.runCommand(RedisCommand("CLIENT", "LIST"))) {
        is RespBulkString -> parseClientList(response.value)
        else -> throw RedisUnexpectedRespResponse(response)
    }
}