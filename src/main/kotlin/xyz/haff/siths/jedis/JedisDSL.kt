package xyz.haff.siths.jedis

import redis.clients.jedis.Jedis
import redis.clients.jedis.Transaction
import redis.clients.jedis.exceptions.JedisNoScriptException
import redis.clients.jedis.params.SetParams
import xyz.haff.siths.scripts.RedisScript
import java.time.Duration

// TODO: Test these

fun Jedis.setWithParams(
    key: String,
    value: String,
    expiration: Duration? = null,
    notExistent: Boolean = false,
): Boolean {
    val params = SetParams()
    if (expiration != null) {
        params.px(expiration.toMillis())
    }

    if (notExistent) {
        params.nx()
    }

    return this.set(key, value, params) == "OK"
}

fun Jedis.hasExpiration(key: String) = ttl(key) != 0L

fun Jedis.setExpiration(key: String, expiration: Duration) {
    pexpire(key, expiration.toMillis())
}

inline fun Jedis.withMulti(f: Transaction.() -> Any): List<Any> {
    val transaction = multi()
    transaction.f()
    return transaction.exec()
}

inline fun <T> Jedis.withWatch(key: String, f: Jedis.() -> T): T {
    watch(key)
    val result = f()
    unwatch()

    return result
}

/**
 * Runs a Redis Lua script optimistically: Tries to run it, and if it isn't present, loads it.
 */
fun Jedis.runScript(script: RedisScript, keys: List<String> = listOf(), args: List<String> = listOf()): Any? {
    return try {
        evalsha(script.sha, keys, args)
    } catch (e: JedisNoScriptException) {
        scriptLoad(script.code)
        evalsha(script.sha, keys, args)
    }
}