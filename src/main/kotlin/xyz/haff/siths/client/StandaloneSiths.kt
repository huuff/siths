package xyz.haff.siths.client

import java.time.Duration

@JvmInline
value class StandaloneSiths(
    private val connection: SithsConnection,
): Siths {

    override suspend fun set(key: String, value: String, exclusiveMode: ExclusiveMode?, timeToLive: Duration?) {
        val exclusiveModeSubCommand = exclusiveMode?.name?.let { RedisCommand(it) } ?: RedisCommand()
        val ttlSubCommand = timeToLive?.let { RedisCommand("PX", timeToLive.toMillis().toString()) } ?: RedisCommand()

        val response = connection.command(RedisCommand("SET", key, value) + exclusiveModeSubCommand + ttlSubCommand)

        if (response is RespError) {
            response.throwAsException()
        }
    }

    override suspend fun ttl(key: String): Duration? = when (val response = connection.command(RedisCommand("PTTL", key))) {
            is RespInteger -> if (response.value < 0) { null } else { Duration.ofMillis(response.value) }
            is RespError -> response.throwAsException()
            else -> throw UnexpectedRespResponse(response)
        }

    override suspend fun getOrNull(key: String): String? = when (val response = connection.command(RedisCommand("GET", key))) {
        is RespBulkString -> response.value
        is RespNullResponse -> null
        is RespError -> response.throwAsException()
        else -> throw UnexpectedRespResponse(response)
    }

    override suspend fun get(key: String): String = getOrNull(key) ?: throw RuntimeException("Key $key does not exist!")

    override suspend fun scriptLoad(script: String): String =
        when (val response = connection.command(RedisCommand("SCRIPT", "LOAD", script))) {
            is RespBulkString -> response.value
            is RespSimpleString -> response.value
            is RespError -> response.throwAsException()
            else -> throw UnexpectedRespResponse(response)
        }

    override suspend fun evalSha(sha: String, keys: List<String>, args: List<String>): RespType<*> =
        when (val response = connection.command(RedisCommand("EVALSHA", sha, keys.size.toString(), *keys.toTypedArray(), *args.toTypedArray()))) {
            is RespError -> response.throwAsException()
            else -> response
        }

    // TODO: Test
    override suspend fun incrBy(key: String, value: Long) = when(val response = connection.command(RedisCommand("INCRBY", key, value.toString()))) {
        is RespInteger -> response.value
        else -> throw UnexpectedRespResponse(response)
    }
}