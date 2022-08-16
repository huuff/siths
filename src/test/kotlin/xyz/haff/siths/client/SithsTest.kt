package xyz.haff.siths.client

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.extensions.install
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.testcontainers.LifecycleMode
import io.kotest.extensions.testcontainers.TestContainerExtension
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.comparables.shouldBeGreaterThanOrEqualTo
import io.kotest.matchers.comparables.shouldBeLessThanOrEqualTo
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import xyz.haff.siths.makeSithsPool
import xyz.haff.siths.scripts.RedisScript
import xyz.haff.siths.client.ExclusiveMode.*
import java.time.Duration

class SithsTest : FunSpec({
    val container = install(TestContainerExtension("redis:7.0.4-alpine", LifecycleMode.Root)) {
        withExposedPorts(6379)
    }

    test("can set and get a value") {
        // ARRANGE
        val siths = PooledSiths(makeSithsPool(container))

        // ACT
        siths.set("key", "value")
        val value = siths.getOrNull("key")

        // ASSERT
        value shouldBe "value"
    }

    test("correct handling when the value doesn't exist") {
        // ARRANGE
        val siths = PooledSiths(makeSithsPool(container))

        // ACT && ASSERT
        siths.getOrNull("non-existent") shouldBe null
    }

    test("weird strings work as intended") {
        // ARRANGE
        val siths = PooledSiths(makeSithsPool(container))
        val key = """ as${'$'} d"f"2"""
        val value = """fd's2${'$'} """

        // ACT
        siths.set(key, value)
        val savedValue = siths.get(key)

        // ASSERT
        savedValue shouldBe value
    }

    context("set parameters") {
        context("SET ... (NX|XX)") {
            test("SET ... NX does not set if the key exists") {
                // ARRANGE
                val siths = PooledSiths(makeSithsPool(container))
                siths.set("nxkey", "test1")

                // ACT
                siths.set("nxkey", "test2", exclusiveMode = NX)

                // ASSERT
                siths.get("nxkey") shouldBe "test1"
            }
            test("SET ... XX does not set if the key does not exist") {
                // ARRANGE
                val siths = PooledSiths(makeSithsPool(container))

                // ACT
                siths.set("xxkey", "testvalue", exclusiveMode = XX)

                // ASSERT
                siths.getOrNull("xxkey") shouldBe null
            }
        }

        test("timeToLive sets ttl") {
            // ARRANGE
            val siths = PooledSiths(makeSithsPool(container))

            // ACT
            siths.set("ttledkey", "ttlvalue", timeToLive = Duration.ofSeconds(10))
            val ttl = siths.ttl("ttledkey")

            // ASSERT
            ttl shouldNotBe null
            ttl!!
            // XXX: Assuming no more than two seconds pass between setting and checking...
            // TODO: Can I make a generic `shouldBeInRange` kotest matcher?
            ttl shouldBeGreaterThanOrEqualTo  Duration.ofSeconds(8)
            ttl shouldBeLessThanOrEqualTo  Duration.ofSeconds(10)
        }
    }

    context("scripts") {
        test("correctly loads script") {
            // ARRANGE
            val siths = PooledSiths(makeSithsPool(container))
            val script = RedisScript(code = """return 'Hello World!' """)

            // ACT
            val returnedSha = siths.scriptLoad(script.code)

            // ASSERT
            returnedSha shouldBe script.sha
        }

        test("correctly runs script") {
            // ARRANGE
            val siths = PooledSiths(makeSithsPool(container))
            val script = RedisScript(code = """return 'Hello World!' """)
            val sha = siths.scriptLoad(script.code)

            // ACT
            val response = siths.evalSha(sha)

            // ASSERT
            response.value shouldBe "Hello World!"
        }

        test("fails when script doesn't exist") {
            // ARRANGE
            val siths = PooledSiths(makeSithsPool(container))

            // ACT & ASSERT
           shouldThrow<RedisScriptNotLoadedException> {
               siths.evalSha("b16b7ff836ae87a150204570d9d82178ece81c8e")
           }
        }
    }
})
