package xyz.haff.siths.client

import io.kotest.core.extensions.install
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.testcontainers.LifecycleMode
import io.kotest.extensions.testcontainers.TestContainerExtension
import io.kotest.matchers.collections.shouldBeIn
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import xyz.haff.siths.client.api.HashSithsImmediateClient
import xyz.haff.siths.client.api.SithsImmediateClient
import xyz.haff.siths.common.headAndTail
import xyz.haff.siths.common.randomUUID
import xyz.haff.siths.makeSithsClient

class HashSithsClientTest : FunSpec({
    val container = install(TestContainerExtension("redis:7.0.4-alpine", LifecycleMode.Root)) {
        withExposedPorts(6379)
    }
    lateinit var siths: HashSithsImmediateClient

    beforeAny {
        siths = makeSithsClient(container)
    }

    test("hset and hget") {
        // ARRANGE
        val key = randomUUID()

        // ACT
        siths.hset(key, "field" to "value")
        val retrieved = siths.hget(key, "field")

        // ASSERT
        retrieved shouldBe "value"
    }

    test("hgetall") {
        // ARRANGE
        val key = randomUUID()
        siths.hset(key, "f1" to "v1", "f2" to "v2", "f3" to "v3")

        // ACT & ASSERT
        siths.hgetAll(key) shouldBe mapOf(
            "f1" to "v1",
            "f2" to "v2",
            "f3" to "v3"
        )
    }

    test("hkeys") {
        // ARRANGE
        val key = randomUUID()
        siths.hset(key, "f1" to "v1", "f2" to "v2", "f3" to "v3")

        // ACT & ASSERT
        siths.hkeys(key) shouldBe listOf("f1", "f2", "f3")
    }

    test("hvals") {
        // ARRANGE
        val key = randomUUID()
        siths.hset(key, "f1" to "v1", "f2" to "v2", "f3" to "v3")

        // ACT & ASSERT
        siths.hvals(key) shouldBe listOf("v1", "v2", "v3")
    }

    test("hexists") {
        // ARRANGE
        val key = randomUUID()
        siths.hset(key, "f1" to "v1", "f2" to "v2", "f3" to "v3")

        // ACT & ASSERT
        siths.hexists(key, "f1") shouldBe true
        siths.hexists(key, "nonexistent") shouldBe false
    }

    test("hincrby") {
        // ARRANGE
        val key = randomUUID()
        siths.hsetAny(key, "field" to 5)

        // ACT
        val response = siths.hincrBy(key, "field", 3)

        // ASSERT
        response shouldBe 8
        siths.hget(key, "field") shouldBe "8"
    }

    test("hincrbyfloat") {
        // ARRANGE
        val key = randomUUID()
        siths.hsetAny(key, "field" to 2.4)

        // ACT
        val response = siths.hincrByFloat(key, "field", 1.6)

        // ASSERT
        response shouldBe 4
        siths.hget(key, "field") shouldBe "4"
    }

    test("hmget") {
        // ARRANGE
        val key = randomUUID()
        siths.hset(key, "f1" to "v1", "f2" to "v2", "f3" to "v3")

        // ACT & ASSERT
        siths.hmget(key, "f1", "f3", "nonexistent") shouldBe mapOf(
            "f1" to "v1",
            "f3" to "v3"
        )
    }

    test("hlen") {
        // ARRANGE
        val key = randomUUID()
        siths.hset(key, "f1" to "v1", "f2" to "v2", "f3" to "v3")

        // ACT & ASSERT
        siths.hlen(key) shouldBe 3
    }

    test("hdel") {
        // ARRANGE
        val key = randomUUID()
        siths.hset(key, "f1" to "v1", "f2" to "v2", "f3" to "v3")

        // SANITY CHECK
        siths.hexists(key, "f2") shouldBe true

        // ACT
        val deletedAmount = siths.hdel(key, "f2")

        // ASSERT
        deletedAmount shouldBe 1
        siths.hexists(key, "f2") shouldBe false
    }

    test("hstrlen") {
        // ARRANGE
        val key = randomUUID()
        siths.hset(key, "field" to "test")

        // ACT & ASSERT
        siths.hstrLen(key, "field") shouldBe 4
    }

    context("hsetnx") {
        test("without setting anything") {
            // ARRANGE
            val key = randomUUID()
            siths.hset(key, "field" to "original")

            // ACT
            val wasChanged = siths.hsetnx(key, "field", "new")

            // ASSERT
            wasChanged shouldBe false
            siths.hget(key, "field") shouldBe "original"
        }

        test("actually setting") {
            // ARRANGE
            val key = randomUUID()
            siths.hset(key, "otherfield" to "value")

            // ACT
            val wasChanged = siths.hsetnx(key, "field", "new")

            // ASSERT
            wasChanged shouldBe true
            siths.hget(key, "field") shouldBe "new"
        }
    }

    context("hrandfield") {
        test("single element") {
            // ARRANGE
            val key = randomUUID()
            siths.hset(key, "f1" to "v1", "f2" to "v2", "f3" to "v3")

            // ACT & ASSERT
            siths.hrandField(key) shouldBeIn listOf("f1", "f2", "f3")
        }

        test("several elements") {
            // ARRANGE
            val key = randomUUID()
            siths.hset(key, "f1" to "v1", "f2" to "v2", "f3" to "v3")

            // ACT & ASSERT
            siths.hrandField(key, 3) shouldContainExactlyInAnyOrder listOf("f1", "f2", "f3")
        }

        test("with values") {
            // ARRANGE
            val key = randomUUID()
            siths.hset(key, "f1" to "v1", "f2" to "v2", "f3" to "v3")

            // ACT & ASSERT
            siths.hrandFieldWithValues(key, 3) shouldBe mapOf(
                "f1" to "v1",
                "f2" to "v2",
                "f3" to "v3"
            )
        }
    }

    test("hscan") {
        // ARRANGE
        val key = randomUUID()
        val pairs = (1..30).map { "f$it" to "v$it" }
        val (head, tail) = pairs.toTypedArray().headAndTail()
        siths.hset(key, head, *tail)
        val scannedPairs = mutableListOf<Pair<String, String>>()

        // ACT
        var cursor = siths.hscan(key, cursor = 0, match = "f*", count = 5)
        do {
            scannedPairs.addAll(cursor.contents)
            cursor = siths.hscan(key, cursor = cursor.next, match = "f*", count = 5)
        } while (cursor.next != 0L)

        // ASSERT
        scannedPairs shouldContainExactlyInAnyOrder pairs
    }
})