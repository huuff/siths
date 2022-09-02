package xyz.haff.siths.client

import io.kotest.core.extensions.install
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.testcontainers.LifecycleMode
import io.kotest.extensions.testcontainers.TestContainerExtension
import io.kotest.matchers.shouldBe
import xyz.haff.siths.client.api.SithsImmediateClient
import xyz.haff.siths.common.randomUUID
import xyz.haff.siths.makeSithsClient

class HashSithsClientTest : FunSpec({
    val container = install(TestContainerExtension("redis:7.0.4-alpine", LifecycleMode.Root)) {
        withExposedPorts(6379)
    }
    lateinit var siths: SithsImmediateClient

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
        siths.hgetall(key) shouldBe mapOf(
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
        val response = siths.hincrby(key, "field", 3)

        // ASSERT
        response shouldBe 8
        siths.hget(key, "field") shouldBe "8"
    }

    test("hincrbyfloat") {
        // ARRANGE
        val key = randomUUID()
        siths.hsetAny(key, "field" to 2.4)

        // ACT
        val response = siths.hincrbyfloat(key, "field", 1.6)

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
})