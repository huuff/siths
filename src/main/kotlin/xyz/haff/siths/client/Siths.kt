package xyz.haff.siths.client

import redis.clients.jedis.exceptions.JedisNoScriptException
import xyz.haff.siths.scripts.RedisScript

class Siths(
    private val pool: SithsPool,
) {

    suspend fun set(key: String, value: String) {
        // TODO: Some escaping to prevent injection
        // TODO: What about keys with spaces?
        val response = pool.pooled { command("SET $key $value") }

        if (response is RespError) {
            response.throwAsException()
        }
    }

    // TODO: What about getting ints? Or other types
    // TODO: What about keys with spaces?
    suspend fun getOrNull(key: String): String? = when (val response = pool.pooled { command("GET $key") }) {
        is RespBulkString -> response.value
        is RespNullResponse -> null
        is RespError -> response.throwAsException()
        else -> throw RuntimeException("Unknown response $response")
    }

    suspend fun get(key: String): String = getOrNull(key) ?: throw RuntimeException("Key $key does not exist!")

    // TODO: What about scripts with double quotes?
    suspend fun scriptLoad(script: String): String =
        when (val response = pool.pooled { command("SCRIPT LOAD \"$script\"") }) {
            is RespBulkString -> response.value
            is RespSimpleString -> response.value
            is RespError -> response.throwAsException()
            else -> throw RuntimeException("Unknown response $response")
        }

    // TODO: What about getting ints? Or other types
    suspend fun evalSha(sha: String, keys: List<String> = listOf(), args: List<String> = listOf()): String =
        when (val response = pool.pooled { command("EVALSHA $sha ${keys.size} ${keys.joinToString(separator = " ")} ${args.joinToString(separator = " ")}") }) {
            is RespBulkString -> response.value
            is RespSimpleString -> response.value
            is RespError -> response.throwAsException()
            else -> throw RuntimeException("Unknown response $response")
        }

    /**
     * Tries to run script, and, if not loaded, loads it, then runs it again
     */
    suspend fun runScript(script: RedisScript, keys: List<String> = listOf(), args: List<String> = listOf()): String {
        return try {
            evalSha(script.sha, keys, args)
        } catch (e: RedisScriptNotLoadedException) {
            scriptLoad(script.code)
            evalSha(script.sha, keys, args)
        }
    }
}