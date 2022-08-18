package xyz.haff.siths.client

import kotlinx.coroutines.delay
import xyz.haff.siths.common.RedisLockTimeoutException
import xyz.haff.siths.common.RedisScriptNotLoadedException
import xyz.haff.siths.common.buildLockKey
import xyz.haff.siths.scripts.RedisScript
import xyz.haff.siths.scripts.RedisScripts
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import java.util.*

// TODO: Implement Siths? Delegate it? To avoid using a context receiver
class SithsDSL(private val pool: SithsPool) {
    // TODO: This should be a context receiver when that API stabilizes
    val redis = PooledClientSiths(pool)

    /**
     * Tries to run script, and, if not loaded, loads it, then runs it again
     */
    suspend fun runScript(script: RedisScript, keys: List<String> = listOf(), args: List<String> = listOf()): RespType<*> {
        return pool.getConnection().use { conn ->
            with (StandaloneClientSiths(conn)) {
                try {
                    evalSha(script.sha, keys, args)
                } catch (e: RedisScriptNotLoadedException) { // TODO: Maybe we could pipeline these two commands so they happen in a single connection?
                    scriptLoad(script.code)
                    evalSha(script.sha, keys, args)
                }
            }
        }
    }

    // TODO: Maybe the following two should be offloaded to some RedisLock class?
    suspend fun acquireLock(
        lockName: String,
        acquireTimeout: Duration = 10.seconds,
        lockTimeout: Duration = 10.seconds,
    ): String {
        val identifier = UUID.randomUUID().toString()
        val endTime = System.currentTimeMillis() + acquireTimeout.inWholeMilliseconds

        // TODO: Function to run some code for some duration, in koy
        while (System.currentTimeMillis() < endTime) {
            if (runScript(RedisScripts.ACQUIRE_LOCK, listOf(buildLockKey(lockName)), listOf(lockTimeout.inWholeMilliseconds.toString(), identifier)).isOk()) {
                return identifier
            }
            delay(10)
        }

        throw RedisLockTimeoutException(lockName, acquireTimeout)
    }

    suspend fun releaseLock(lockName: String, identifier: String): Boolean
            = runScript(RedisScripts.RELEASE_LOCK, listOf(buildLockKey(lockName)), listOf(identifier)).isOk()

    suspend inline fun <T> withLock(lockName: String, timeout: Duration = 10.seconds, f: SithsDSL.() -> T): T {
        val lockIdentifier = acquireLock(lockName, timeout)

        return try {
            f()
        } finally {
            releaseLock(lockName, lockIdentifier)
        }
    }
}

inline fun <T> withRedis(pool: SithsPool, f: SithsDSL.() -> T): T {
    val dsl = SithsDSL(pool)
    return dsl.f()
}