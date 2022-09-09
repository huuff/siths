package xyz.haff.siths.client

import xyz.haff.siths.client.api.SithsImmediateClient
import xyz.haff.siths.client.pooled.ManagedSithsClient
import xyz.haff.siths.client.pooled.SithsClientPool
import xyz.haff.siths.common.RedisScriptNotLoadedException
import xyz.haff.siths.pipelining.PipelinedSithsClient
import xyz.haff.siths.pipelining.QueuedResponse
import xyz.haff.siths.protocol.RespType
import xyz.haff.siths.protocol.SithsConnectionPool
import xyz.haff.siths.scripts.RedisScript
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class SithsDSL(val pool: SithsConnectionPool) :
    SithsImmediateClient by ManagedSithsClient(pool = SithsClientPool(pool)) {

    /**
     * Tries to run script, and, if not loaded, loads it, then runs it again
     */
    suspend fun runScript(
        script: RedisScript,
        keys: List<String> = listOf(),
        args: List<String> = listOf()
    ): RespType<*> {
        return pool.get().use { conn ->
            with(StandaloneSithsClient(conn)) {
                try {
                    evalSha(script.sha, keys, args)
                } catch (e: RedisScriptNotLoadedException) {
                    pipelined {
                        scriptLoad(script.code)
                        evalSha(script.sha, keys, args)
                    }
                }
            }
        }
    }

    suspend inline fun <T> pipelined(f: PipelinedSithsClient.() -> QueuedResponse<T>): T {
        val pipelineBuilder = PipelinedSithsClient()
        val result = pipelineBuilder.f()
        pool.get().use { conn -> pipelineBuilder.exec(conn) }

        return result.get()
    }

    suspend inline fun <T> transactional(f: PipelinedSithsClient.() -> QueuedResponse<T>): T {
        val pipelineBuilder = PipelinedSithsClient()
        val result = pipelineBuilder.f()
        pool.get().use { conn -> pipelineBuilder.exec(conn, inTransaction = true) }

        return result.get()
    }

    suspend inline fun <T> withLock(lockName: String, timeout: Duration = 10.seconds, f: SithsDSL.() -> T): T {
        val lock = SithsLock(lockName, pool)
        lock.acquire(acquireTimeout = timeout)

        return try {
            f()
        } finally {
            lock.release()
        }
    }
}

inline fun <T> withRedis(pool: SithsConnectionPool, f: SithsDSL.() -> T): T {
    val dsl = SithsDSL(pool)
    return dsl.f()
}