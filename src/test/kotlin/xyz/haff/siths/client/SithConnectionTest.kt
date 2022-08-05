package xyz.haff.siths.client

import io.kotest.core.extensions.install
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.testcontainers.LifecycleMode
import io.kotest.extensions.testcontainers.TestContainerExtension
import io.kotest.matchers.shouldBe

class SithConnectionTest : FunSpec({
    val container = install(TestContainerExtension("redis:7.0.4-alpine", LifecycleMode.Root)) {
        withExposedPorts(6379)
    }

    test("can set and get a value") {
        // ARRANGE
        val connection = SithsConnection.open(container.host, container.firstMappedPort)

        // ACT
        connection.command("SET key value")
        val value = connection.command("GET key")

        // ASSERT
        value.value shouldBe "value"
    }
})