package xyz.haff.siths

import redis.clients.jedis.Jedis
import java.time.Clock
import java.time.Duration
import java.time.LocalDateTime
import java.util.*

/**
 * KEYS[1]: Lock key
 * ARGV[1]: Expiration time in milliseconds
 * ARGV[2]: Lock identifier
 */
private val acquireLockScript = RedisScript("""
    if redis.call("exists", KEYS[1]) == 0 then
        return redis.call("psetex", KEYS[1], unpack(ARGV))
    end
""".trimIndent())

/**
 * KEYS[1]: Lock key
 * ARGV[1]: Lock identifier
 */
private val releaseLockScript = RedisScript("""
    if redis.call("get", KEYS[1]) == ARGV[1] then
        return redis.call("del", KEYS[1]) or true
    end
""".trimIndent())

class RedisLockTimeoutException(msg: String) : RuntimeException(msg)

private fun buildLockKey(lockName: String) = "lock$lockName"

@JvmOverloads
fun Jedis.acquireLock(
    lockName: String,
    acquireTimeout: Duration = Duration.ofSeconds(10),
    lockTimeout: Duration = Duration.ofSeconds(10),
    clock: Clock = Clock.systemDefaultZone()
): String {
    val identifier = UUID.randomUUID().toString()
    val endTime = LocalDateTime.now(clock) + acquireTimeout

    // TODO: Function to run some code for some duration, in koy
    while (LocalDateTime.now(clock) < endTime) {
        if (runScript(acquireLockScript, listOf(buildLockKey(lockName)), listOf(lockTimeout.toMillis().toString(), identifier)) == "OK") {
            return identifier
        }
        Thread.sleep(1)
    }

    throw RedisLockTimeoutException("Timed out waiting for $lockName after $acquireTimeout")
}

fun Jedis.releaseLock(lockName: String, identifier: String): Boolean
    = runScript(releaseLockScript, listOf(buildLockKey(lockName)), listOf(identifier)) == "OK"

@JvmOverloads
inline fun <T> Jedis.withLock(lockName: String, timeout: Duration = Duration.ofSeconds(10), f: Jedis.() -> T): T {
    val lockIdentifier = acquireLock(lockName, timeout)

    return try {
        f()
    } finally {
        releaseLock(lockName, lockIdentifier)
    }
}