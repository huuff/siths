package xyz.haff.siths.client

import io.kotest.core.extensions.install
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.testcontainers.LifecycleMode
import io.kotest.extensions.testcontainers.TestContainerExtension
import io.kotest.matchers.shouldBe
import xyz.haff.siths.RedisScript
import xyz.haff.siths.makeSithsPool

class SithsTest : FunSpec({
    val container = install(TestContainerExtension("redis:7.0.4-alpine", LifecycleMode.Root)) {
        withExposedPorts(6379)
    }

    test("can set and get a value") {
        // ARRANGE
        val siths = Siths(makeSithsPool(container))

        // ACT
        siths.set("key", "value")
        val value = siths.getOrNull("key")

        // ASSERT
        value shouldBe "value"
    }

    test("correct handling when the value doesn't exist") {
        // ARRANGE
        val siths = Siths(makeSithsPool(container))

        // ACT && ASSERT
        siths.getOrNull("non-existent") shouldBe null
    }

    // TODO: Test nonexistent script (in evalsha)
    context("scripts") {
        test("correctly loads script") {
            // ARRANGE
            val siths = Siths(makeSithsPool(container))
            val script = RedisScript(code = """return 'Hello World!' """)

            // ACT
            val returnedSha = siths.scriptLoad(script)

            // ASSERT
            returnedSha shouldBe script.sha
        }

        test("correctly runs script") {
            // ARRANGE
            val siths = Siths(makeSithsPool(container))
            val script = RedisScript(code = """return 'Hello World!' """)
            val sha = siths.scriptLoad(script)

            // ACT
            val response = siths.evalSha(sha)

            // ASSERT
            response shouldBe "Hello World!"
        }
    }
})
