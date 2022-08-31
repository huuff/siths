package xyz.haff.siths.client

import io.kotest.core.extensions.install
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.testcontainers.LifecycleMode
import io.kotest.extensions.testcontainers.TestContainerExtension
import io.kotest.matchers.comparables.shouldBeGreaterThanOrEqualTo
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import xyz.haff.koy.timed
import xyz.haff.siths.common.randomUUID
import xyz.haff.siths.makeSithsClient
import xyz.haff.siths.option.ListEnd
import kotlin.time.Duration.Companion.milliseconds

class ListSithsClientTest : FunSpec({
    val container = install(TestContainerExtension("redis:7.0.4-alpine", LifecycleMode.Root)) {
        withExposedPorts(6379)
    }
    lateinit var siths: SithsClient

    beforeAny {
        siths = makeSithsClient(container)
    }

    test("lpush") {
        // ARRANGE
        val list = randomUUID()

        // ACT
        siths.rpush(list, "key1", "key2", "key3")

        // ASSERT
        siths.lrange(list, 0, -1) shouldBe listOf("key1", "key2", "key3")
    }

    test("llen") {
        // ARRANGE
        val list = randomUUID()
        siths.rpush(list, "key1", "key2", "key3")

        // ACT & ASSERT
        siths.llen(list) shouldBe 3
    }

    context("lpop and rpop") {
        test("single lpop") {
            // ARRANGE
            val list = randomUUID()
            siths.rpush(list, "key1", "key2", "key3")

            // ACT & ASSERT
            siths.lpop(list) shouldBe "key1"
        }

        test("rpop many") {
            // ARRANGE
            val list = randomUUID()
            siths.rpush(list, "key1", "key2", "key3")

            // ACT & ASSERT
            siths.rpop(list, 2) shouldBe listOf("key3", "key2")
        }
    }

    context("lpos") {
        test("without count") {
            // ARRANGE
            val list = randomUUID()
            siths.rpush(list, "v1", "v2", "v3", "v2")

            // ACT & ASSERT
            siths.lpos(list, "v2", rank = 2, maxlen = 1000) shouldBe 3
        }

        test("with count") {
            // ARRANGE
            val list = randomUUID()
            siths.rpush(list, "v1", "v2", "v3", "v2")

            // ACT & ASSERT
            siths.lpos(list, "v2", count = 2, maxlen = 1000) shouldBe listOf(1, 3)
        }
    }

    test("lset") {
        // ARRANGE
        val list = randomUUID()
        siths.rpush(list, "v1", "v2", "v3")

        // SANITY CHECK
        siths.lindex(list, 1) shouldBe "v2"

        // ACT
        val wasChanged = siths.lset(list, 1, "v999")

        // ASSERT
        wasChanged shouldBe true
        siths.lindex(list, 1) shouldBe "v999"
    }

    context("lmpop") {
        test("two lists") {
            // ARRANGE
            val key1 = randomUUID()
            siths.rpush(key1, "v1", "v3", "v5")
            val key2 = randomUUID()

            // ACT
            val response = siths.lmpop(listOf(key1, key2), ListEnd.RIGHT, 2)

            // ASSERT
            response shouldNotBe null
            response!!
            response.source shouldBe key1
            response.data shouldBe listOf("v5", "v3")
        }

        test("a single list") {
            // ARRANGE
            val key = randomUUID()
            siths.rpush(key, "v1", "v3", "v5")

            // ACT && ASSERT
            siths.lmpop(key, ListEnd.LEFT, 2) shouldBe listOf("v1", "v3")
        }
    }

    test("ltrim") {
        // ARRANGE
        val key = randomUUID()
        siths.rpush(key, "v1", "v2", "v3", "v4", "v5")

        // ACT
        siths.ltrim(key, 1, 3)

        // ASSERT
        siths.lrange(key, 0, -1) shouldBe listOf("v2", "v3", "v4")
    }

    context("lpushx") {
        test("without pushing") {
            // ARRANGE
            val key = randomUUID()

            // ACT
            val listSize = siths.lpushx(key, "test")

            // ASSERT
            listSize shouldBe 0
            siths.exists(key) shouldBe false
        }

        test("actually pushing") {
            // ARRANGE
            val key = randomUUID()
            siths.rpush(key, "test1")

            // ACT
            val listSize = siths.lpushx(key, "test2")

            // ASSERT
            listSize shouldBe 2
            siths.lrange(key, 0, -1) shouldBe listOf("test2", "test1")
        }
    }

    test("lmove") {
        // ARRANGE
        val source = randomUUID()
        siths.rpush(source, "v1", "v3", "v5")
        val destination = randomUUID()
        siths.rpush(destination, "v2", "v4", "v6")

        // ACT
        val element = siths.lmove(source, destination, ListEnd.LEFT, ListEnd.RIGHT)

        // ASSERT
        element shouldBe "v1"
        siths.lrange(destination, 0, -1) shouldBe listOf("v2", "v4", "v6", "v1")
    }

    context("blocking") {
        test("brpop fail") {
            // ARRANGE
            val key = randomUUID()

            // ACT
            val popped = siths.brpop(key, 20.milliseconds)

            // ASSERT
            popped shouldBe null
        }

        test("brpop blocks") {
            // ARRANGE
            val key = randomUUID()

            // ACT
            val popped = async { timed { siths.brpop(key) } }
            withContext(Dispatchers.Default) {
                delay(10)
                siths.lpush(key, "value")
            }

            // ASSERT
            val (result, time) = popped.await()
            result shouldBe "value"
            time shouldBeGreaterThanOrEqualTo 10
        }
    }
})