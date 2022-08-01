package xyz.haff.siths

import io.kotest.core.extensions.install
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.testcontainers.TestContainerExtension
import io.kotest.matchers.shouldBe

class RedisScriptTest : FunSpec({
    val container = install(TestContainerExtension("redis:7.0.4-alpine")) {
        withExposedPorts(6379)
    }

    afterEach {
        poolFromContainer(container).resource.use { redis -> redis.flushAll() }
    }

    context("SHA tests") {
        test("one-liner SHA is correct") {
            poolFromContainer(container).resource.use { redis ->
                // ARRANGE
                val script = """return "Hello World!" """
                val scriptObject = RedisScript(code = script)

                // ACT
                val redisReturnedSha = redis.scriptLoad(script)

                // ASSERT
                scriptObject.sha shouldBe redisReturnedSha
            }
        }

        test("complex-er script SHA is correct") {
            poolFromContainer(container).resource.use { redis ->
                // ARRANGE
                val script = """
                    local world = "World"
                    return "Hello "..world.."!"
                """
                val scriptObject = RedisScript(code = script)

                // ACT
                val redisReturnedSha = redis.scriptLoad(script)

                // ASSERT
                scriptObject.sha shouldBe redisReturnedSha
            }
        }
    }

    test("script runs correctly, even if it wasn't previously loaded") {
        poolFromContainer(container).resource.use { redis ->
            // ARRANGE
            val scriptObject = RedisScript(code = """ return "Hello World!!!" """)

            // ACT
            val result = redis.runScript(scriptObject)

            // ASSERT
            result shouldBe "Hello World!!!"
        }
    }
})
