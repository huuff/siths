package xyz.haff.siths.client

import xyz.haff.siths.scripts.RedisScript

// TODO: This somewhere else?
private fun escape(string: String) = string.replace("\"", "\\\"")

@JvmInline
value class StandaloneSiths(
    private val connection: SithsConnection,
): Siths {

    override suspend fun set(key: String, value: String) {
        val response = connection.command("SET \"${escape(key)}\" \"${escape(value)}\"")

        if (response is RespError) {
            response.throwAsException()
        }
    }

    // TODO: What about getting ints? Or other types
    override suspend fun getOrNull(key: String): String? = when (val response = connection.command("GET \"${escape(key)}\"")) {
        is RespBulkString -> response.value
        is RespNullResponse -> null
        is RespError -> response.throwAsException()
        else -> throw RuntimeException("Unknown response $response")
    }

    override suspend fun get(key: String): String = getOrNull(key) ?: throw RuntimeException("Key $key does not exist!")

    override suspend fun scriptLoad(script: String): String =
        when (val response = connection.command("SCRIPT LOAD \"${escape(script)}\"")) {
            is RespBulkString -> response.value
            is RespSimpleString -> response.value
            is RespError -> response.throwAsException()
            else -> throw RuntimeException("Unknown response $response")
        }

    // TODO: What about getting ints? Or other types
    // TODO: Escape strings here
    override suspend fun evalSha(sha: String, keys: List<String>, args: List<String>): String =
        when (val response = connection.command("EVALSHA $sha ${keys.size} ${keys.joinToString(separator = " ")} ${args.joinToString(separator = " ")}")) {
            is RespBulkString -> response.value
            is RespSimpleString -> response.value
            is RespError -> response.throwAsException()
            else -> throw RuntimeException("Unknown response $response")
        }

    /**
     * Tries to run script, and, if not loaded, loads it, then runs it again
     */
    override suspend fun runScript(script: RedisScript, keys: List<String>, args: List<String>): String {
        return try {
            evalSha(script.sha, keys, args)
        } catch (e: RedisScriptNotLoadedException) {
            scriptLoad(script.code)
            evalSha(script.sha, keys, args)
        }
    }
}