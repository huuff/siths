package xyz.haff.siths.client

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.extensions.install
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.testcontainers.LifecycleMode
import io.kotest.extensions.testcontainers.TestContainerExtension
import io.kotest.matchers.comparables.shouldBeGreaterThanOrEqualTo
import io.kotest.matchers.comparables.shouldBeLessThanOrEqualTo
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import xyz.haff.siths.makeSithsPool
import xyz.haff.siths.scripts.RedisScript
import xyz.haff.siths.client.ExclusiveMode.*
import xyz.haff.siths.common.RedisScriptNotLoadedException
import xyz.haff.siths.common.randomUUID
import xyz.haff.siths.makeSithsClient
import kotlin.time.Duration.Companion.seconds

class SithsClientTest : FunSpec({
    val container = install(TestContainerExtension("redis:7.0.4-alpine", LifecycleMode.Root)) {
        withExposedPorts(6379)
    }

    test("can set and get a value") {
        // ARRANGE
        val siths = PooledSithsClient(makeSithsPool(container))

        // ACT
        siths.set("key", "value")
        val value = siths.getOrNull("key")

        // ASSERT
        value shouldBe "value"
    }

    test("del deletes key") {
        // ARRANGE
        val siths = PooledSithsClient(makeSithsPool(container))
        siths.set("future-deleted-key1", "value")
        siths.set("future-deleted-key2", "value")

        // SANITY CHECK
        siths.exists("future-deleted-key1", "future-deleted-key2") shouldBe true

        // ACT
        siths.del("future-deleted-key1", "future-deleted-key2") shouldBe 2

        // ASSERT
        siths.exists("future-deleted-key1", "future-deleted-key2") shouldBe false
    }

    test("clientList contains current connection") {
        StandaloneSithsConnection.open(host = container.host, port = container.firstMappedPort).use { connection ->
            // ARRANGE
            val siths = StandaloneSithsClient(connection)

            // ACT
            val clients = siths.clientList()

            // ASSERT
            clients.find { it.name == connection.identifier } shouldNotBe null
        }
    }


    test("correct handling when the value doesn't exist") {
        // ARRANGE
        val siths = PooledSithsClient(makeSithsPool(container))

        // ACT && ASSERT
        siths.getOrNull("non-existent") shouldBe null
    }

    test("weird strings work as intended") {
        // ARRANGE
        val siths = PooledSithsClient(makeSithsPool(container))
        val key = """ as${'$'} d"f"2"""
        val value = """fd's2${'$'} """

        // ACT
        siths.set(key, value)
        val savedValue = siths.get(key)

        // ASSERT
        savedValue shouldBe value
    }

    test("incrBy works") {
        // ARRANGE
        val siths = PooledSithsClient(makeSithsPool(container))

        // ACT
        siths.set("incremented-key", 0)
        val response = siths.incrBy("incremented-key", 1)

        // ASSERT
        response shouldBe 1
        siths.get("incremented-key") shouldBe "1"
    }

    context("set parameters") {
        context("SET ... (NX|XX)") {
            test("SET ... NX does not set if the key exists") {
                // ARRANGE
                val siths = PooledSithsClient(makeSithsPool(container))
                siths.set("nxkey", "test1")

                // ACT
                siths.set("nxkey", "test2", exclusiveMode = NX)

                // ASSERT
                siths.get("nxkey") shouldBe "test1"
            }
            test("SET ... XX does not set if the key does not exist") {
                // ARRANGE
                val siths = PooledSithsClient(makeSithsPool(container))

                // ACT
                siths.set("xxkey", "testvalue", exclusiveMode = XX)

                // ASSERT
                siths.getOrNull("xxkey") shouldBe null
            }
        }

        test("timeToLive sets ttl") {
            // ARRANGE
            val siths = PooledSithsClient(makeSithsPool(container))

            // ACT
            siths.set("ttledkey", "ttlvalue", timeToLive = 10.seconds)
            val ttl = siths.ttl("ttledkey")

            // ASSERT
            ttl shouldNotBe null
            ttl!!
            // XXX: Assuming no more than two seconds pass between setting and checking...
            // TODO: Can I make a generic `shouldBeInRange` kotest matcher?
            ttl shouldBeGreaterThanOrEqualTo  8.seconds
            ttl shouldBeLessThanOrEqualTo  10.seconds
        }
    }

    context("scripts") {
        test("correctly evals script") {
            // ARRANGE
            val siths = PooledSithsClient(makeSithsPool(container))

            // ACT
            val response = siths.eval("return 'Hello World!'")

            // ASSERT
            response.value shouldBe "Hello World!"
        }

        test("correctly loads script") {
            // ARRANGE
            val siths = PooledSithsClient(makeSithsPool(container))
            val script = RedisScript(code = """return 'Hello World!' """)

            // ACT
            val returnedSha = siths.scriptLoad(script.code)

            // ASSERT
            returnedSha shouldBe script.sha
        }

        test("correctly runs script") {
            // ARRANGE
            val siths = PooledSithsClient(makeSithsPool(container))
            val script = RedisScript(code = """return 'Hello World!' """)
            val sha = siths.scriptLoad(script.code)

            // ACT
            val response = siths.evalSha(sha)

            // ASSERT
            response.value shouldBe "Hello World!"
        }

        test("fails when script doesn't exist") {
            // ARRANGE
            val siths = PooledSithsClient(makeSithsPool(container))

            // ACT & ASSERT
           shouldThrow<RedisScriptNotLoadedException> {
               siths.evalSha("b16b7ff836ae87a150204570d9d82178ece81c8e")
           }
        }
    }

    context("sets") {
        test("we can add to the set") {
            // ARRANGE
            val siths = makeSithsClient(container)
            val set = randomUUID();

            // ACT
            val added = siths.sadd(set, "test1", "test2")

            // ASSERT
            added shouldBe 2
            siths.sismember(set, "test1") shouldBe true
            siths.sismember(set, "test2") shouldBe true
        }

        test("we can get all members") {
            // ARRANGE
            val siths = makeSithsClient(container)
            val set = randomUUID();
            siths.sadd(set, "test1", "test2")

            // ACT
            val members = siths.smembers(set)

            // ASSERT
            members shouldBe setOf("test1", "test2")
        }

        test("we can remove an element") {
            // ARRANGE
            val siths = makeSithsClient(container)
            val set = randomUUID();
            siths.sadd(set, "test1", "test2")

            // SANITY CHECK
            siths.sismember(set, "test1") shouldBe true

            // ACT
            val removed = siths.srem(set, "test1")

            // ASSERT
            removed shouldBe 1L
            siths.sismember(set, "test1") shouldBe false
        }

        test("sintercard") {
            // ARRANGE
            val siths = makeSithsClient(container)
            val set1 = randomUUID()
            val set2 = randomUUID()
            siths.sadd(set1, "key1", "key2")
            siths.sadd(set2, "key2", "key3")

            // ACT
            val intersection = siths.sintercard(set1, set2)

            // ASSERT
            intersection shouldBe 1L
        }

        test("sdiffstore") {
            // ARRANGE
            val siths = makeSithsClient(container)
            val operand1 = randomUUID()
            val operand2 = randomUUID()
            val destination = randomUUID()
            siths.sadd(operand1, "key1", "key2", "key3")
            siths.sadd(operand2, "key1", "key3")

            // ACT
            val elementNumber = siths.sdiffstore(destination, operand1, operand2)

            // ASSERT
            elementNumber shouldBe 1L
            siths.smembers(destination) shouldBe setOf("key2")
        }

        test("sinterstore") {
            // ARRANGE
            val siths = makeSithsClient(container)
            val operand1 = randomUUID()
            val operand2 = randomUUID()
            val destination = randomUUID()
            siths.sadd(operand1, "key1", "key2", "key3")
            siths.sadd(operand2, "key1", "key3")

            // ACT
            val elementNumber = siths.sinterstore(destination, operand1, operand2)

            // ASSERT
            elementNumber shouldBe 2L
            siths.smembers(destination) shouldBe setOf("key1", "key3")
        }

        test("sscan") {
            // ARRANGE
            val siths = makeSithsClient(container)
            val set = randomUUID()
            val valuesToAdd = (1..15).map { "value$it" }
            siths.sadd(set, "unincluded-value", *valuesToAdd.toTypedArray())

            // ACT
            val result1 = siths.sscan(set, 0, match = "value*", count = 6)
            val result2 = siths.sscan(set, result1.next, match = "value*", count = 6)
            val result3 = siths.sscan(set, result2.next, match = "value*", count = 6)

            // ASSERT
            (result1.contents + result2.contents + result3.contents).toSet() shouldBe valuesToAdd.toSet()
        }
    }

    test("ping") {
        // ARRANGE
        val siths = makeSithsClient(container)

        // ACT
        val response = siths.ping()

        // ASSERT
        response shouldBe true
    }
})
