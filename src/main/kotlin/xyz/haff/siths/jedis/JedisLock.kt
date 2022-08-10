package xyz.haff.siths.jedis

import redis.clients.jedis.Jedis
import xyz.haff.siths.scripts.RedisScripts
import java.time.Clock
import java.time.Duration
import java.time.LocalDateTime
import java.util.*

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
        if (runScript(RedisScripts.ACQUIRE_LOCK, listOf(buildLockKey(lockName)), listOf(lockTimeout.toMillis().toString(), identifier)) == "OK") {
            return identifier
        }
        Thread.sleep(1)
    }

    throw RedisLockTimeoutException("Timed out waiting for $lockName after $acquireTimeout")
}

fun Jedis.releaseLock(lockName: String, identifier: String): Boolean
    = runScript(RedisScripts.RELEASE_LOCK, listOf(buildLockKey(lockName)), listOf(identifier)) == "OK"

@JvmOverloads
inline fun <T> Jedis.withLock(lockName: String, timeout: Duration = Duration.ofSeconds(10), f: Jedis.() -> T): T {
    val lockIdentifier = acquireLock(lockName, timeout)

    return try {
        f()
    } finally {
        releaseLock(lockName, lockIdentifier)
    }
}