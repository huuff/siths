package xyz.haff.siths.client

/**
 * A thin wrapper over a plain redis connection. Things it does:
 *  * Provide a discoverable entrypoint for available functions
 *  * Escape the commands that are sent to Redis
 *  * Convert the responses to the appropriate Kotlin types
 */
interface Siths {
    suspend fun set(key: String, value: String): Unit
    suspend fun getOrNull(key: String): String?
    suspend fun get(key: String): String
    suspend fun scriptLoad(script: String): String
    suspend fun evalSha(sha: String, keys: List<String> = listOf(), args: List<String> = listOf()): RespType<*>
    suspend fun incrBy(key: String, value: Long): Long
}