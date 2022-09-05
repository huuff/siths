package xyz.haff.siths.client

import kotlinx.coroutines.delay
import xyz.haff.koy.control.during
import xyz.haff.siths.common.RedisLockTimeoutException
import xyz.haff.siths.common.buildLockKey
import xyz.haff.siths.protocol.SithsConnectionPool
import xyz.haff.siths.scripts.RedisScripts
import java.util.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class SithsLock(
    val name: String,
    private val pool: SithsConnectionPool,
) {
    private var identifier: String? = null

    suspend fun acquire(acquireTimeout: Duration = 10.seconds, lockTimeout: Duration = 10.seconds) {
        val identifier = UUID.randomUUID().toString()

        during (acquireTimeout) {
            val lockAcquired = withRedis(pool) {
                runScript(
                    RedisScripts.ACQUIRE_LOCK,
                    listOf(buildLockKey(name)),
                    listOf(lockTimeout.inWholeMilliseconds.toString(), identifier)
                ).isOk()
            }
            if (lockAcquired) {
                this.identifier = identifier
                return
            }
            delay(10)
        }

        throw RedisLockTimeoutException(name, acquireTimeout)
    }

    suspend fun release(): Boolean = if (identifier != null) {
        withRedis(pool) {
            runScript(RedisScripts.RELEASE_LOCK, listOf(buildLockKey(name)), listOf(identifier!!)).isOk().also {
                identifier = null
            }
        }
    } else {
        throw RuntimeException("Trying to release a lock that hasn't been acquired!!")
    }
}