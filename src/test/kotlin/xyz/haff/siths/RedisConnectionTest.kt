package xyz.haff.siths

import io.kotest.core.extensions.install
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.testcontainers.TestContainerExtension
import io.kotest.matchers.shouldBe
import redis.clients.jedis.JedisPool

class RedisConnectionTest : FunSpec({
    val container = install(TestContainerExtension("redis:7.0.4-alpine")) {
        withExposedPorts(6379)
    }

    test("we can set and get a value") {
        poolFromContainer(container).resource.use { redis ->
            // ACT
            redis["key"] = "value"

            // ASSERT
            redis["key"] shouldBe "value"
        }
    }
})