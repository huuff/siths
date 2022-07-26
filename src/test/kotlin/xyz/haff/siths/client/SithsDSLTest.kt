package xyz.haff.siths.client

import io.kotest.core.extensions.install
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.testcontainers.LifecycleMode
import io.kotest.extensions.testcontainers.TestContainerExtension
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import xyz.haff.siths.common.randomUUID
import xyz.haff.siths.makeSithsPool
import xyz.haff.siths.protocol.RespBulkString
import xyz.haff.siths.protocol.RespInteger
import xyz.haff.siths.protocol.RespSimpleString
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
        response.value shouldBe "Hello World!!?"
    }

    test("correctly pipelines") {
        // ARRANGE
        val pool = makeSithsPool(container)
        val key = randomUUID()

        // ACT
        val pipelineResult = withRedis(pool) {
            pipelined {
                set(key, "0")
                incrBy(key, 1)
                incrBy(key, 1)
                getLong(key)
            }
        }

        // ASSERT
        pipelineResult shouldBe 2
    }

    test("correctly makes transaction") {
        // ARRANGE
        val pool = makeSithsPool(container)
        val key = randomUUID()

        // ACT
        val pipelineResult = withRedis(pool) {
            transactional {
                set(key, "0")
                incrBy(key, 1)
                incrBy(key, 1)
                getLong(key)
            }
        }

        // ASSERT
        pipelineResult shouldBe 2
    }
})
