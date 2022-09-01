package xyz.haff.siths.client

import kotlinx.coroutines.delay
import xyz.haff.siths.client.pooled.ManagedSithsClient
import xyz.haff.siths.client.pooled.SithsClientPool
import xyz.haff.siths.common.*
import xyz.haff.siths.common.buildLockKey
import xyz.haff.siths.pipelining.SithsPipelinedClient
import xyz.haff.siths.protocol.RespType
import xyz.haff.siths.protocol.SithsConnectionPool
import xyz.haff.siths.scripts.RedisScript
import xyz.haff.siths.scripts.RedisScripts
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import java.util.*

// TODO: Implement Siths? Delegate it? To avoid using a context receiver
class SithsDSL(val pool: SithsConnectionPool) {
    val redis = ManagedSithsClient(pool = SithsClientPool(pool))

    /**
     * Tries to run script, and, if not loaded, loads it, then runs it again
     */
    // TODO: Likely I'll need some way to run this in a pipeline with more commands! For example, see my SithsList.retainAll implementation
    suspend fun runScript(script: RedisScript, keys: List<String> = listOf(), args: List<String> = listOf()): RespType<*> {
        return pool.get().use { conn ->
            with (StandaloneSithsClient(conn)) {
                try {
                    evalSha(script.sha, keys, args)
                } catch (e: RedisScriptNotLoadedException) {
                    pipelined {
                        scriptLoad(script.code)
                        evalSha(script.sha, keys, args)
                    }[1]
                }
            }
        }
    }

    // TODO: hmm this api seems unfriendly... the return type is too low-level
    suspend inline fun pipelined(f: SithsPipelinedClient.() -> Unit): List<RespType<*>> {
        return pool.get().use { connection ->
            val pipelineBuilder = SithsPipelinedClient(connection)
            pipelineBuilder.f()
            pipelineBuilder.exec()
        }
    }
    suspend inline fun transactional(f: SithsPipelinedClient.() -> Unit): List<RespType<*>> {
        return pool.get().use { connection ->
            val pipelineBuilder = SithsPipelinedClient(connection)
            pipelineBuilder.f()
            pipelineBuilder.exec(inTransaction = true)
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

inline fun <T> withRedis(pool: SithsConnectionPool, f: SithsDSL.() -> T): T {
    val dsl = SithsDSL(pool)
    return dsl.f()
}