package xyz.haff.siths.client

import io.kotest.core.extensions.install
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.testcontainers.LifecycleMode
import io.kotest.extensions.testcontainers.TestContainerExtension
import io.kotest.matchers.shouldBe
import xyz.haff.siths.makeSithsPool
import xyz.haff.siths.scripts.RedisScript

class SithsDSLTest : FunSpec({
    val container = install(TestContainerExtension("redis:7.0.4-alpine", LifecycleMode.Root)) {
        withExposedPorts(6379)
    }

    test("loads and runs missing script") {
        // ARRANGE
        val pool = makeSithsPool(container)
        val script = RedisScript(code = """return 'Hello World!!?'""")

        // ACT
        val response = withRedis(pool) { runScript(script) }

        // ASSERT
        response shouldBe "Hello World!!?"
    }

})
