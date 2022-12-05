package xyz.haff.siths.client

import io.kotest.core.extensions.install
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.testcontainers.LifecycleMode
import io.kotest.extensions.testcontainers.TestContainerExtension
import io.kotest.matchers.shouldBe
import xyz.haff.siths.client.api.ZSetSithsImmediateClient
import xyz.haff.siths.common.randomUUID
import xyz.haff.siths.makeSithsClient

class ZSetSithsClientTest : FunSpec({
    val container = install(TestContainerExtension("redis:7.0.4-alpine", LifecycleMode.Root)) {
        withExposedPorts(6379)
    }
    lateinit var siths: ZSetSithsImmediateClient

    beforeAny {
        siths = makeSithsClient(container)
    }

    context("zrangeByScore") {
        test("zadd and zrangeByScore") {
            // ARRANGE
            val key = randomUUID()

            // ACT
            siths.zadd(key, 1.0 to "1", 2.0 to "2", 3.0 to "3", 4.0 to "4")
            val result = siths.zrangeByScore(key, 1.0, 2.0);

            // ASSERT
            result shouldBe setOf("1", "2")
        }

        test("zadd and zrangeByScoreWithScores") {
            // ARRANGE
            val key = randomUUID()

            // ACT
            siths.zadd(key, 1.0 to "1", 2.0 to "2", 3.0 to "3", 4.0 to "4")
            val result = siths.zrangeByScoreWithScores(key, 1.0, 2.0);

            // ASSERT
            result shouldBe listOf("1" to 1.0, "2" to 2.0)
        }
    }

    context("zrangeByRank") {
        test("zadd and zrangeByRank") {
            // ARRANGE
            val key = randomUUID()

            // ACT
            siths.zadd(key, 1.0 to "1", 2.0 to "2", 3.0 to "3", 4.0 to "4")
            val result = siths.zrangeByRank(key, 1, 2);

            // ASSERT
            result shouldBe setOf("2", "3")
        }

        test("zadd and zrangeByRankWithScores") {
            // ARRANGE
            val key = randomUUID()

            // ACT
            siths.zadd(key, 1.0 to "1", 2.0 to "2", 3.0 to "3", 4.0 to "4")
            val result = siths.zrangeByRankWithScores(key, 1, 2);

            // ASSERT
            result shouldBe listOf("2" to 2.0, "3" to 3.0)
        }
    }

    test("zremRangeByScore") {
        // ARRANGE
        val key = randomUUID()

        // ACT
        siths.zadd(key, 1.0 to "1", 2.0 to "2", 3.0 to "3", 4.0 to "4")
        siths.zremRangeByScore(key, 2.0, 3.0)

        // ASSERT
        siths.zrangeByRank(key, 0, -1) shouldBe setOf("1", "4")
    }
})