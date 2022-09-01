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
})