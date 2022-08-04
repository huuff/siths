package xyz.haff.siths.client

class Siths(
    private val connection: SithConnection,
) {

    suspend fun set(key: String, value: String) {
        // TODO: Some escaping to prevent injection
        val response = connection.command("SET $key $value")

        if (response is RespError) { response.throwAsException() }
    }

    suspend fun getOrNull(key: String): String? =  when (val response = connection.command("GET $key")) {
            is RespBulkString -> response.value
            is RespNullResponse -> null
            is RespError -> response.throwAsException()
            else -> throw RuntimeException("Unknown response $response")
    }

    suspend fun get(key: String): String = getOrNull(key) ?: throw RuntimeException("Key $key does not exist!")
}