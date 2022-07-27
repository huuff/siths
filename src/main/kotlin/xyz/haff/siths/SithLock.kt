package xyz.haff.siths

import redis.clients.jedis.Jedis
import redis.clients.jedis.exceptions.JedisException
import java.time.Clock
import java.time.Duration
import java.time.LocalDateTime
import java.util.*

class RedisLockTimeoutException(msg: String): RuntimeException(msg)

// TODO: TEST ALL TIMEOUTS
// TODO: Func to build lockName

@JvmOverloads
fun Jedis.acquireLock(
    lockName: String,
    acquireTimeout: Duration = Duration.ofSeconds(10),
    lockTimeout: Duration = Duration.ofSeconds(10),
    clock: Clock = Clock.systemDefaultZone()
): String {
    val identifier = UUID.randomUUID().toString()
    val endTime = LocalDateTime.now(clock) + acquireTimeout

    while (LocalDateTime.now(clock) < endTime) {

        val result = this.setWithParams("lock:$lockName", identifier, expiration = lockTimeout, notExistent = true)
        if (result == "OK") {
            return identifier
        } else if (this.ttl("lock:$lockName") == 0L) {
            this.pexpire("lock:$lockName", lockTimeout.toMillis())
        }
        Thread.sleep(1)
    }

    throw RedisLockTimeoutException("Timed out waiting for $lockName after $acquireTimeout")
}

fun Jedis.releaseLock(lockName: String, identifier: String): Boolean {
    val lockKey = "lock:$lockName"

    while (true) {
        try {
            watch(lockKey)
            if (get(lockKey) == identifier) {
                val transaction = multi()
                transaction.del(lockKey)
                transaction.exec()
                return true
            }
            unwatch()
            break
        } catch (e: JedisException) { // TODO: Find the correct exception
            // Ignore it and keep trying
        }
    }

    return false
}

@JvmOverloads
fun <T> Jedis.withLock(lockName: String, timeout: Duration = Duration.ofSeconds(10), f: Jedis.() -> T) : T {
    val lockIdentifier = acquireLock(lockName, timeout)
    val result = f()
    releaseLock(lockName, lockIdentifier)
    return result
}