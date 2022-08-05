package xyz.haff.siths.client

import xyz.haff.siths.RedisScript

class Siths(
    private val pool: SithPool,
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
    suspend fun scriptLoad(script: RedisScript): String =
        when (val response = pool.pooled { command("SCRIPT LOAD \"${script.code}\"") }) {
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
}