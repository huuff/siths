package xyz.haff.siths.client

import io.kotest.core.extensions.install
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.testcontainers.LifecycleMode
import io.kotest.extensions.testcontainers.TestContainerExtension
import io.kotest.matchers.shouldBe
import xyz.haff.siths.RedisScript

class SithsTest : FunSpec({
    val container = install(TestContainerExtension("redis:7.0.4-alpine", LifecycleMode.Root)) {
        withExposedPorts(6379)
    }

    test("can set and get a value") {
        // ARRANGE
        val siths = Siths(SithPool(container.host, container.firstMappedPort))

        // ACT
        siths.set("key", "value")
        val value = siths.getOrNull("key")

        // ASSERT
        value shouldBe "value"
    }

    test("correct handling when the value doesn't exist") {
        // ARRANGE
        val siths = Siths(SithPool(container.host, container.firstMappedPort))

        // ACT && ASSERT
        siths.getOrNull("non-existent") shouldBe null
    }

    test("correctly loads script") {
        // ARRANGE
        val siths = Siths(SithPool(container.host, container.firstMappedPort))
        val script = RedisScript(code = """return 'Hello World!' """)

        // ACT
        val returnedSha = siths.scriptLoad(script)

        // ASSERT
        returnedSha shouldBe script.sha
    }
})
