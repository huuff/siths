package xyz.haff.siths.client

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.extensions.install
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.testcontainers.TestContainerExtension
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.*
import kotlinx.coroutines.delay
import xyz.haff.siths.common.RedisLockTimeoutException
import xyz.haff.siths.makeSithsPool
import xyz.haff.siths.protocol.RespNullResponse
import xyz.haff.siths.protocol.SithsConnectionPool
import xyz.haff.siths.runInContainer
import xyz.haff.siths.suspended
import java.util.*
import kotlin.time.Duration.Companion.milliseconds

class SithsLockTest : FunSpec({
    val container = install(TestContainerExtension("redis:7.0.4-alpine")) {
        withExposedPorts(6379)
    }

    afterEach {
        clearAllMocks()
    }

    test("execution gets interleaved without locking") {
        // ARRANGE
        val key = "key"
        // TODO: How come there's no setAny?
        runInContainer(container) { set(key, "0") }

        // ACT
        suspended(10) {
            makeSithsPool(container).get().use { redis ->
                val siths = StandaloneSithsClient(redis)
                val value = siths.getLong("key")
                delay(100)
                siths.set(key, (value+1).toString())
            }
        }

        // ASSERT
        runInContainer(container) { getLong(key) } shouldNotBe 10
    }

    test("execution is orderly with locking") {
        // ARRANGE
        val key = "key"
        runInContainer(container) { set(key, "0") }

        // ACT
        suspended(10) {
            withRedis(makeSithsPool(container)) {
                withLock("lock") {
                    val value = getLong(key)
                    delay(100)
                    set(key, (value+1).toString())
                }
            }
        }

        // ASSERT
        runInContainer(container) { getLong(key) } shouldBe 10
    }

    test("acquire times out if it cant acquire the lock") {
        // ARRANGE
        val fakePool = mockk<SithsConnectionPool>(relaxed = true) {
            coEvery { get().resource.runCommand(any()) } returns RespNullResponse
        }

        // ACT & ASSERT
        shouldThrow<RedisLockTimeoutException> {
            SithsLock("lock", fakePool).acquire(acquireTimeout = 10.milliseconds)
        }
    }
})
