package xyz.haff.siths.client

import xyz.haff.siths.scripts.RedisScript

class SithsDSL(private val pool: SithsPool) {

    /**
     * Tries to run script, and, if not loaded, loads it, then runs it again
     */
    suspend fun runScript(script: RedisScript, keys: List<String> = listOf(), args: List<String> = listOf()): String {
        return pool.getConnection().use { conn ->
            with (StandaloneSiths(conn)) {
                try {
                    evalSha(script.sha, keys, args)
                } catch (e: RedisScriptNotLoadedException) { // TODO: Maybe we could pipeline these two commands so they happen in a single connection?
                    scriptLoad(script.code)
                    evalSha(script.sha, keys, args)
                }
            }
        }
    }
}

inline fun <T> withRedis(pool: SithsPool, f: SithsDSL.() -> T): T {
    val dsl = SithsDSL(pool)
    return dsl.f()
}