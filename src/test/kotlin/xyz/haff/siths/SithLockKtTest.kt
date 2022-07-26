package xyz.haff.siths

import io.kotest.core.extensions.install
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.testcontainers.TestContainerExtension
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
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

    afterEach {
        poolFromContainer(container).resource.use { jedis -> jedis.flushAll() }
    }

    test("without locking") {
        val values = mutableListOf<String>()
        val tasks = (1..10).map {
            Thread {
                poolFromContainer(container).resource.use { redis ->
                    // ACT
                    redis.incrBy("key", 1)
                    Thread.sleep(100)
                    redis.incrBy("key", -1)
                    values += redis["key"]
                }
            }
        }
        tasks.forEach { it.start() }
        tasks.forEach { it.join() }

        values.distinct().size shouldNotBe 1
    }

    test("with locking") {
        val values = mutableListOf<String>()
        val tasks = (1..10).map {
            Thread {
                poolFromContainer(container).resource.use { redis ->
                    val lockIdentifier = redis.acquireLock("lock")
                    redis.incrBy("key", 1)
                    Thread.sleep(100)
                    redis.incrBy("key", -1)
                    values += redis["key"]
                    redis.releaseLock("lock", lockIdentifier)
                }
            }
        }
        tasks.forEach { it.start() }
        tasks.forEach { it.join() }

        values.distinct() shouldBe listOf("0")
    }
})
