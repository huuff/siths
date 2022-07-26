package xyz.haff.siths

import io.kotest.core.extensions.install
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.testcontainers.TestContainerExtension
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import redis.clients.jedis.JedisPool
import java.time.Duration
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.Future

class SithLockKtTest : FunSpec({
    val container = install(TestContainerExtension("redis:5.0.3-alpine")) {
        startupAttempts = 1 // TODO: Try to remove it
        withExposedPorts(6379)
    }

    // TODO: Flush db between tests
    // TODO: Assert something?
    test("without locking") {
        val tasks = (1..10).map {
            Thread {
                JedisPool(container.host, container.firstMappedPort).resource.use { redis ->
                    // ACT
                    redis.incrBy("key", 1)
                    println(redis["key"])
                    Thread.sleep(100)
                    redis.incrBy("key", -1)
                }
            }
        }
        tasks.forEach { it.start() }
        tasks.forEach { it.join() }
    }

    test("with locking") {
        val tasks = (1..10).map {
            Thread {
                JedisPool(container.host, container.firstMappedPort).resource.use { redis ->
                    val lockIdentifier = redis.acquireLock("lock")
                    redis.incrBy("key", 1)
                    println(redis["key"])
                    Thread.sleep(100)
                    redis.incrBy("key", -1)
                    redis.releaseLock("lock", lockIdentifier)
                }
            }
        }
        tasks.forEach { it.start() }
        tasks.forEach { it.join() }
    }
})
