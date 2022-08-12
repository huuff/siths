package xyz.haff.siths.client

import xyz.haff.siths.common.escape
import java.time.Duration

@JvmInline
value class StandaloneSiths(
    private val connection: SithsConnection,
): Siths {

    override suspend fun set(key: String, value: String, exclusiveMode: ExclusiveMode?, timeToLive: Duration?) {
        val exclusiveModeString = exclusiveMode?.name ?: ""
        val ttlString = timeToLive?.let { "PX ${timeToLive.toMillis()}" } ?: ""

        val response = connection.command("SET \"${key.escape()}\" \"${value.escape()}\" $exclusiveModeString $ttlString" )

        if (response is RespError) {
            response.throwAsException()
        }
    }

    override suspend fun ttl(key: String): Duration? = when (val response = connection.command("PTTL \"${key.escape()}\"")) {
            is RespInteger -> if (response.value < 0) { null } else { Duration.ofMillis(response.value) }
            is RespError -> response.throwAsException()
            else -> throw UnexpectedRespResponse(response)
        }

    // TODO: What about getting ints? Or other types
    override suspend fun getOrNull(key: String): String? = when (val response = connection.command("GET \"${key.escape()}\"")) {
        is RespBulkString -> response.value
        is RespNullResponse -> null
        is RespError -> response.throwAsException()
        else -> throw UnexpectedRespResponse(response)
    }

    override suspend fun get(key: String): String = getOrNull(key) ?: throw RuntimeException("Key $key does not exist!")

    // TODO: This won't work for scripts with double quotes
    override suspend fun scriptLoad(script: String): String =
        when (val response = connection.command("SCRIPT LOAD \"${script.escape()}\"")) {
            is RespBulkString -> response.value
            is RespSimpleString -> response.value
            is RespError -> response.throwAsException()
            else -> throw UnexpectedRespResponse(response)
        }

    // TODO: Escape strings here
    override suspend fun evalSha(sha: String, keys: List<String>, args: List<String>): RespType<*> =
        when (val response = connection.command("EVALSHA $sha ${keys.size} ${keys.joinToString(separator = " ")} ${args.joinToString(separator = " ")}")) {
            is RespError -> response.throwAsException()
            else -> response
        }

    // TODO: Test
    override suspend fun incrBy(key: String, value: Long) = when(val response = connection.command("INCRBY \"${key.escape()}\" $value")) {
        is RespInteger -> response.value
        else -> throw UnexpectedRespResponse(response)
    }
}