package xyz.haff.siths

import org.apache.commons.codec.digest.DigestUtils
import redis.clients.jedis.Jedis
import redis.clients.jedis.exceptions.JedisNoScriptException

/**
 * Runs a Redis Lua script optimistically: Tries to run it, and if it isn't present, loads it.
 */
fun Jedis.runScript(script: RedisScript, keys: List<String> = listOf(), args: List<String> = listOf()): Any? {
    return try {
        evalsha(script.sha)
    } catch (e: JedisNoScriptException) {
        scriptLoad(script.code)
        evalsha(script.sha, keys, args)
    }
}

data class RedisScript(
    val code: String,
) {
    val sha: String = DigestUtils.sha1Hex(code)
}