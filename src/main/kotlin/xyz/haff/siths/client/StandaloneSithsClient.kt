package xyz.haff.siths.client

import xyz.haff.siths.common.RedisUnexpectedRespResponse
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class StandaloneSithsClient(
    private val connection: SithsConnection,
    private val commandBuilder: RedisCommandBuilder = RedisCommandBuilder()
): SithsClient {

    override suspend fun set(key: String, value: Any, exclusiveMode: ExclusiveMode?, timeToLive: Duration?) {
        val command = commandBuilder.set(key, value, exclusiveMode, timeToLive)

        val response = connection.runCommand(command)

        if (response is RespError) {
            response.throwAsException()
        }
    }

    override suspend fun ttl(key: String): Duration? = when (val response = connection.runCommand(commandBuilder.ttl(key))) {
            is RespInteger -> if (response.value < 0) { null } else { response.value.milliseconds }
            is RespError -> response.throwAsException()
            else -> throw RedisUnexpectedRespResponse(response)
        }

    override suspend fun getOrNull(key: String): String? = when (val response = connection.runCommand(commandBuilder.get(key))) {
        is RespBulkString -> response.value
        is RespNullResponse -> null
        is RespError -> response.throwAsException()
        else -> throw RedisUnexpectedRespResponse(response)
    }

    override suspend fun get(key: String): String = getOrNull(key) ?: throw RuntimeException("Key $key does not exist!")

    override suspend fun scriptLoad(script: String): String =
        when (val response = connection.runCommand(commandBuilder.scriptLoad(script))) {
            is RespBulkString -> response.value
            is RespSimpleString -> response.value
            is RespError -> response.throwAsException()
            else -> throw RedisUnexpectedRespResponse(response)
        }

    override suspend fun evalSha(sha: String, keys: List<String>, args: List<String>): RespType<*> =
        when (val response = connection.runCommand(commandBuilder.evalSha(sha, keys, args))) {
            is RespError -> response.throwAsException()
            else -> response
        }

    override suspend fun incrBy(key: String, value: Long) = when(val response = connection.runCommand(commandBuilder.incrBy(key, value))) {
        is RespInteger -> response.value
        else -> throw RedisUnexpectedRespResponse(response)
    }

    override suspend fun clientList(): List<RedisClient> = when(val response = connection.runCommand(commandBuilder.clientList())) {
        is RespBulkString -> parseClientList(response.value)
        else -> throw RedisUnexpectedRespResponse(response)
    }
}