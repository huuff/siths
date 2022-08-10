package xyz.haff.siths.jedis

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
import xyz.haff.siths.client.RedisLockTimeoutException
import xyz.haff.siths.makeJedisPool
import xyz.haff.siths.threaded
import java.time.Duration
import java.util.*

class JedisLockTest : FunSpec({
    val container = install(TestContainerExtension("redis:7.0.4-alpine")) {
        withExposedPorts(6379)
    }

    afterEach {
        makeJedisPool(container).resource.use { jedis -> jedis.flushAll() }
        clearAllMocks()
    }

    test("execution gets interleaved without locking") {
        val values = Collections.synchronizedList(mutableListOf<String>())

        threaded(10) {
            makeJedisPool(container).resource.use { redis ->
                redis.incrBy("key", 1)
                Thread.sleep(100)
                redis.incrBy("key", -1)
                values += redis["key"]
            }
        }

        values.distinct().size shouldNotBe 1
    }

    test("execution is orderly with locking") {
        val values = Collections.synchronizedList(mutableListOf<String>())

        threaded(10) {
            makeJedisPool(container).resource.use { redis ->
                redis.withLock("lock") {
                    redis.incrBy("key", 1)
                    Thread.sleep(100)
                    redis.incrBy("key", -1)
                    values += redis["key"]
                }
            }
        }

        values.distinct() shouldBe listOf("0")
    }

    test("acquire times out if it cant acquire the lock") {
        // ARRANGE
        mockkStatic(Jedis::runScript)

        val redis = mockk<Jedis> {
            every { runScript(any(), any(), any()) } returns null
        }

        // ACT & ASSERT
        shouldThrow<RedisLockTimeoutException> { redis.acquireLock("lock", acquireTimeout = Duration.ofMillis(10)) }
    }
})
