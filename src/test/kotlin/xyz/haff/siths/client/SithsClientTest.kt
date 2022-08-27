package xyz.haff.siths.client

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.extensions.install
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.testcontainers.LifecycleMode
import io.kotest.extensions.testcontainers.TestContainerExtension
import io.kotest.matchers.collections.shouldBeIn
import io.kotest.matchers.comparables.shouldBeGreaterThanOrEqualTo
import io.kotest.matchers.comparables.shouldBeLessThanOrEqualTo
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import xyz.haff.siths.scripts.RedisScript
import xyz.haff.siths.client.ExclusiveMode.*
import xyz.haff.siths.common.RedisScriptNotLoadedException
import xyz.haff.siths.common.headAndTail
import xyz.haff.siths.common.randomUUID
import xyz.haff.siths.makeRedisConnection
import xyz.haff.siths.makeSithsClient
import kotlin.time.Duration.Companion.seconds

class SithsClientTest : FunSpec({
    val container = install(TestContainerExtension("redis:7.0.4-alpine", LifecycleMode.Root)) {
        withExposedPorts(6379)
    }
    lateinit var siths: SithsClient

    beforeAny {
        siths = makeSithsClient(container)
    }

    test("can set and get a value") {
        // ACT
        siths.set("key", "value")
        val value = siths.getOrNull("key")

        // ASSERT
        value shouldBe "value"
    }

    test("del deletes key") {
        // ARRANGE
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
        StandaloneSithsConnection.open(makeRedisConnection(container)).use { connection ->
            // ARRANGE
            @Suppress("NAME_SHADOWING") val siths = StandaloneSithsClient(connection)

            // ACT
            val clients = siths.clientList()

            // ASSERT
            clients.find { it.name == connection.identifier } shouldNotBe null
        }
    }


    test("correct handling when the value doesn't exist") {
        // ACT && ASSERT
        siths.getOrNull("non-existent") shouldBe null
    }

    test("weird strings work as intended") {
        // ARRANGE
        val key = """ as${'$'} d"f"2"""
        val value = """fd's2${'$'} """

        // ACT
        siths.set(key, value)
        val savedValue = siths.get(key)

        // ASSERT
        savedValue shouldBe value
    }

    test("incrBy works") {
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
                siths.set("nxkey", "test1")

                // ACT
                siths.set("nxkey", "test2", exclusiveMode = NX)

                // ASSERT
                siths.get("nxkey") shouldBe "test1"
            }
            test("SET ... XX does not set if the key does not exist") {
                // ACT
                siths.set("xxkey", "testvalue", exclusiveMode = XX)

                // ASSERT
                siths.getOrNull("xxkey") shouldBe null
            }
        }

        test("timeToLive sets ttl") {
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
            // ACT
            val response = siths.eval("return 'Hello World!'")

            // ASSERT
            response.value shouldBe "Hello World!"
        }

        test("correctly loads script") {
            val script = RedisScript(code = """return 'Hello World!' """)

            // ACT
            val returnedSha = siths.scriptLoad(script.code)

            // ASSERT
            returnedSha shouldBe script.sha
        }

        test("correctly runs script") {
            val script = RedisScript(code = """return 'Hello World!' """)
            val sha = siths.scriptLoad(script.code)

            // ACT
            val response = siths.evalSha(sha)

            // ASSERT
            response.value shouldBe "Hello World!"
        }

        test("fails when script doesn't exist") {
            // ACT & ASSERT
           shouldThrow<RedisScriptNotLoadedException> {
               siths.evalSha("b16b7ff836ae87a150204570d9d82178ece81c8e")
           }
        }
    }

    context("sets") {
        test("we can add to the set") {
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
            val set = randomUUID();
            siths.sadd(set, "test1", "test2")

            // ACT
            val members = siths.smembers(set)

            // ASSERT
            members shouldBe setOf("test1", "test2")
        }

        test("we can remove an element") {
            // ARRANGE
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

        test("sdiff") {
            // ARRANGE
            val set1 = randomUUID()
            siths.sadd(set1, "key1", "key2", "key3")
            val set2 = randomUUID()
            siths.sadd(set2, "key3")

            // ACT
            val difference = siths.sdiff(set1, set2)

            // ASSERT
            difference shouldBe setOf("key1", "key2")
        }

        test("sinter") {
            // ARRANGE
            val set1 = randomUUID()
            siths.sadd(set1, "key1", "key2", "key3")
            val set2 = randomUUID()
            siths.sadd(set2, "key3")

            // ACT
            val intersection = siths.sinter(set1, set2)

            // ASSERT
            intersection shouldBe setOf("key3")
        }

        test("smove") {
            // ARRANGE
            val set1 = randomUUID()
            siths.sadd(set1, "key1", "key2", "key3")
            val set2 = randomUUID()
            siths.sadd(set2, "key3")

            // ACT
            val wasMoved = siths.smove(set1, set2, "key1")

            // ASSERT
            wasMoved shouldBe true
            siths.smembers(set1) shouldBe setOf("key2", "key3")
            siths.smembers(set2) shouldBe setOf("key1", "key3")
        }

        test("spop") {
            // ARRANGE
            val set = randomUUID()
            val members = setOf("key1", "key2", "key3")
            val (head, tail) = members.toTypedArray().headAndTail()
            siths.sadd(set, head, *tail)

            // ACT
            val poppedElement = siths.spop(set)

            // ASSERT
            poppedElement shouldNotBe null
            poppedElement!!
            poppedElement shouldBeIn members
            siths.scard(set) shouldBe 2
            siths.sismember(set, poppedElement) shouldBe false
        }

        test("srandmember") {
            // ARRANGE
            val set = randomUUID()
            siths.sadd(set, "key1", "key2", "key3")

            // ACT
            val randomMembers = siths.srandmember(set, count = 2)

            // ASSERT
            randomMembers.forEach {
                it shouldBeIn siths.smembers(set)
            }
        }

        test("sunion") {
            // ARRANGE
            val set1 = randomUUID()
            siths.sadd(set1, "key1", "key2")
            val set2 = randomUUID()
            siths.sadd(set2, "key3")

            // ACT
            val union = siths.sunion(set1, set2)

            // ASSERT
            union shouldBe setOf("key1", "key2", "key3")
        }

        test("sunionstore") {
            // ARRANGE
            val set1 = randomUUID()
            siths.sadd(set1, "key1", "key2")
            val set2 = randomUUID()
            siths.sadd(set2, "key3")
            val destination = randomUUID()

            // ACT
            val unionCard = siths.sunionstore(destination, set1, set2)

            // ASSERT
            unionCard shouldBe 3
            siths.smembers(destination) shouldBe setOf("key1", "key2", "key3")
        }

        test("smismember") {
            // ARRANGE
            val set = randomUUID()
            siths.sadd(set, "key1", "key3", "key5")

            // ACT
            val membershipMap = siths.smismember(set, "key1", "key2", "key3", "key4", "key5")

            // ASSERT
            membershipMap shouldBe mapOf(
                "key1" to true,
                "key2" to false,
                "key3" to true,
                "key4" to false,
                "key5" to true,
            )
        }
    }

    // TODO: Use rpush for tests? This way the assertions are easier to understand
    context("lists") {
        test("lpush") {
            // ARRANGE
            val list = randomUUID()

            // ACT
            siths.lpush(list, "key1", "key2", "key3")

            // ASSERT
            siths.lrange(list, 0, -1) shouldBe listOf("key3", "key2", "key1")
        }

        test("llen") {
            // ARRANGE
            val list = randomUUID()
            siths.lpush(list, "key1", "key2", "key3")

            // ACT
            val length = siths.llen(list)

            // ASSERT
            length shouldBe 3
        }

        context("lpop and rpop") {
            test("single lpop") {
                // ARRANGE
                val list = randomUUID()
                siths.lpush(list, "key1", "key2", "key3")

                // ACT
                val popped = siths.lpop(list)

                // ASSERT
                popped shouldBe "key3"
            }

            test("rpop many") {
                // ARRANGE
                val list = randomUUID()
                siths.lpush(list, "key1", "key2", "key3")

                // ACT
                val popped = siths.rpop(list, 2)

                // ASSERT
                popped shouldBe listOf("key3", "key2")
            }
        }

        context("lpos") {
            test("without count") {
                // ARRANGE
                val list = randomUUID()
                siths.rpush(list, "v1", "v2", "v3", "v2")

                // ACT
                val idx = siths.lpos(list, "v2", rank = 2, maxlen = 1000)

                // ASSERT
                idx shouldBe 3
            }

            test("with count") {
                // ARRANGE
                val list = randomUUID()
                siths.rpush(list, "v1", "v2", "v3", "v2")

                // ACT
                val idxs = siths.lpos(list, "v2", count = 2, maxlen = 1000)

                // ASSERT
                idxs shouldBe listOf(1, 3)
            }
        }
    }

    test("ping") {
        // ACT
        val response = siths.ping()

        // ASSERT
        response shouldBe true
    }

    context("expire") {
        test("correctly sets expiration") {
            // ARRANGE
            val key = randomUUID()
            siths.set(key, "value")

            // ACT
            val commandWorked = siths.expire(key, 10.seconds)

            // ASSERT
            commandWorked shouldBe true
            val expiration = siths.ttl(key)!!
            expiration shouldBeLessThanOrEqualTo 10.seconds
            expiration shouldBeGreaterThanOrEqualTo 8.seconds
        }

        test("expiration condition") {
            // ARRANGE
            val key = randomUUID()
            siths.set(key, "value", timeToLive = 10.seconds)

            // ACT
            val commandWorked = siths.expire(key, 10.seconds, expirationCondition = ExpirationCondition.NX)

            // ASSERT
            commandWorked shouldBe false
        }
    }
})
