package xyz.haff.siths.client

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.extensions.install
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.testcontainers.TestContainerExtension
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.*
import redis.clients.jedis.Jedis
import xyz.haff.siths.jedis.acquireLock
import xyz.haff.siths.jedis.runScript
import xyz.haff.siths.makeSithsPool
import xyz.haff.siths.suspended
import java.time.Duration
import java.util.*


class SithsLockTest : FunSpec({
    val container = install(TestContainerExtension("redis:7.0.4-alpine")) {
        withExposedPorts(6379)
    }

    afterEach {
        clearAllMocks()
    }

    test("execution gets interleaved without locking") {
        val values = Collections.synchronizedList(mutableListOf<String>())

        suspended(10) {
            makeSithsPool(container).getConnection().use { redis ->
                val siths = StandaloneSiths(redis)
                siths.incrBy("key", 1)
                Thread.sleep(100)
                siths.incrBy("key", -1)
                values += siths.get("key")
            }
        }

        values.distinct().size shouldNotBe 1
    }

    test("execution is orderly with locking") {
        val values = Collections.synchronizedList(mutableListOf<String>())

        suspended(10) {
            withRedis(makeSithsPool(container)) {
                withLock("lock") {
                    redis.incrBy("key", 1)
                    Thread.sleep(100)
                    redis.incrBy("key", -1)
                    values += redis.get("key")
                }
            }
        }

        values.distinct() shouldBe listOf("0")
    }

    test("acquire times out if it cant acquire the lock") {
        // ARRANGE
        val fakePool = mockk<SithsPool>(relaxed = true) {
            coEvery { getConnection().command(any()) } returns RespNullResponse
        }

        // ACT & ASSERT
        shouldThrow<RedisLockTimeoutException> {
            withRedis(fakePool) {
                acquireLock("lock", acquireTimeout = Duration.ofMillis(10))
            }
        }
    }
})