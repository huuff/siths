package xyz.haff.siths

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.extensions.install
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.testcontainers.TestContainerExtension
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import redis.clients.jedis.Jedis
import java.time.Duration

class SithLockTest : FunSpec({
    val container = install(TestContainerExtension("redis:5.0.3-alpine")) {
        withExposedPorts(6379)
    }

    afterEach {
        poolFromContainer(container).resource.use { jedis -> jedis.flushAll() }
        clearAllMocks()
    }

    test("execution gets interleaved without locking") {
        val values = mutableListOf<String>()
        val tasks = (1..10).map {
            Thread {
                poolFromContainer(container).resource.use { redis ->
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

    test("execution is orderly with locking") {
        val values = mutableListOf<String>()
        val tasks = (1..10).map {
            Thread {
                poolFromContainer(container).resource.use { redis ->
                    redis.withLock("lock") {
                        redis.incrBy("key", 1)
                        Thread.sleep(100)
                        redis.incrBy("key", -1)
                        values += redis["key"]
                    }
                }
            }
        }
        tasks.forEach { it.start() }
        tasks.forEach { it.join() }

        values.distinct() shouldBe listOf("0")
    }

    test("acquire times out if it cant acquire the lock") {
        // ARRANGE
        mockkStatic(Jedis::setWithParams)
        mockkStatic(Jedis::hasExpiration)

        val redis = mockk<Jedis> {
            every { setWithParams(any(), any(), any(), any()) } returns false // Can never set it
            every { hasExpiration(any()) } returns true // It always has some timeout
        }

        // ACT & ASSERT
        shouldThrow<RedisLockTimeoutException> { redis.acquireLock("lock", acquireTimeout = Duration.ofMillis(10)) }
    }
})
