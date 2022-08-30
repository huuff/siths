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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import xyz.haff.koy.timed
import xyz.haff.siths.client.ExclusiveMode.NX
import xyz.haff.siths.client.ExclusiveMode.XX
import xyz.haff.siths.common.RedisScriptNotLoadedException
import xyz.haff.siths.common.headAndTail
import xyz.haff.siths.common.randomUUID
import xyz.haff.siths.makeRedisConnection
import xyz.haff.siths.makeSithsClient
import xyz.haff.siths.scripts.RedisScript
import kotlin.time.Duration.Companion.milliseconds
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
        val key = randomUUID()
        siths.set(key, "value")
        val value = siths.getOrNull(key)

        // ASSERT
        value shouldBe "value"
    }

    test("del deletes key") {
        // ARRANGE
        val futureDeletedKey1 = randomUUID()
        val futureDeletedKey2 = randomUUID()
        siths.set(futureDeletedKey1, "value")
        siths.set(futureDeletedKey2, "value")

        // SANITY CHECK
        siths.exists(futureDeletedKey1, futureDeletedKey2) shouldBe true

        // ACT
        siths.del(futureDeletedKey1, futureDeletedKey2) shouldBe 2

        // ASSERT
        siths.exists(futureDeletedKey1, futureDeletedKey2) shouldBe false
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

        // ASSERT
        siths.get(key) shouldBe value
    }

    test("incrby") {
        // ACT
        val key = randomUUID()
        siths.set(key, "0")
        val response = siths.incrBy(key, 1)

        // ASSERT
        response shouldBe 1
        siths.get(key) shouldBe "1"
    }

    test("incrByFloat") {
        // ARRANGE
        val key = randomUUID()
        siths.set(key, "1")

        // ACT
        val incremented = siths.incrByFloat(key, 3.33)

        // ASSERT
        incremented shouldBe 4.33
        siths.get(key) shouldBe "4.33"
    }

    context("set parameters") {
        context("SET ... (NX|XX)") {
            test("SET ... NX does not set if the key exists") {
                // ARRANGE
                val key = randomUUID()
                siths.set(key, "test1")

                // ACT
                siths.set(key, "test2", exclusiveMode = NX)

                // ASSERT
                siths.get(key) shouldBe "test1"
            }
            test("SET ... XX does not set if the key does not exist") {
                // ACT
                val key = randomUUID()
                siths.set(key, "testvalue", exclusiveMode = XX)

                // ASSERT
                siths.getOrNull(key) shouldBe null
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
            ttl shouldBeGreaterThanOrEqualTo 8.seconds
            ttl shouldBeLessThanOrEqualTo 10.seconds
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

            // ACT & ASSERT
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

            // ACT & ASSERT
            siths.smembers(set) shouldBe setOf("test1", "test2")
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

            // ACT & ASSERT
            siths.sintercard(set1, set2) shouldBe 1L
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

            // ACT & ASSERT
            siths.sdiff(set1, set2) shouldBe setOf("key1", "key2")
        }

        test("sinter") {
            // ARRANGE
            val set1 = randomUUID()
            siths.sadd(set1, "key1", "key2", "key3")
            val set2 = randomUUID()
            siths.sadd(set2, "key3")

            // ACT & ASSERT
            siths.sinter(set1, set2) shouldBe setOf("key3")
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
            siths.sunion(set1, set2) shouldBe setOf("key1", "key2", "key3")
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

            // ACT & ASSERT
            siths.smismember(set, "key1", "key2", "key3", "key4", "key5") shouldBe mapOf(
                "key1" to true,
                "key2" to false,
                "key3" to true,
                "key4" to false,
                "key5" to true,
            )
        }
    }

    context("lists") {
        test("lpush") {
            // ARRANGE
            val list = randomUUID()

            // ACT
            siths.rpush(list, "key1", "key2", "key3")

            // ASSERT
            siths.lrange(list, 0, -1) shouldBe listOf("key1", "key2", "key3")
        }

        test("llen") {
            // ARRANGE
            val list = randomUUID()
            siths.rpush(list, "key1", "key2", "key3")

            // ACT & ASSERT
            siths.llen(list) shouldBe 3
        }

        context("lpop and rpop") {
            test("single lpop") {
                // ARRANGE
                val list = randomUUID()
                siths.rpush(list, "key1", "key2", "key3")

                // ACT & ASSERT
                siths.lpop(list) shouldBe "key1"
            }

            test("rpop many") {
                // ARRANGE
                val list = randomUUID()
                siths.rpush(list, "key1", "key2", "key3")

                // ACT & ASSERT
                siths.rpop(list, 2) shouldBe listOf("key3", "key2")
            }
        }

        context("lpos") {
            test("without count") {
                // ARRANGE
                val list = randomUUID()
                siths.rpush(list, "v1", "v2", "v3", "v2")

                // ACT & ASSERT
                siths.lpos(list, "v2", rank = 2, maxlen = 1000) shouldBe 3
            }

            test("with count") {
                // ARRANGE
                val list = randomUUID()
                siths.rpush(list, "v1", "v2", "v3", "v2")

                // ACT & ASSERT
                siths.lpos(list, "v2", count = 2, maxlen = 1000) shouldBe listOf(1, 3)
            }
        }

        test("lset") {
            // ARRANGE
            val list = randomUUID()
            siths.rpush(list, "v1", "v2", "v3")

            // SANITY CHECK
            siths.lindex(list, 1) shouldBe "v2"

            // ACT
            val wasChanged = siths.lset(list, 1, "v999")

            // ASSERT
            wasChanged shouldBe true
            siths.lindex(list, 1) shouldBe "v999"
        }

        test("lmpop") {
            // ARRANGE
            val key1 = randomUUID()
            siths.rpush(key1, "v1", "v3", "v5")
            val key2 = randomUUID()

            // ACT
            val response = siths.lmpop(listOf(key1, key2), ListEnd.RIGHT, 2)

            // ASSERT
            response shouldNotBe null
            response!!
            response.source shouldBe key1
            response.data shouldBe listOf("v5", "v3")
        }

        test("ltrim") {
            // ARRANGE
            val key = randomUUID()
            siths.rpush(key, "v1", "v2", "v3", "v4", "v5")

            // ACT
            siths.ltrim(key, 1, 3)

            // ASSERT
            siths.lrange(key, 0, -1) shouldBe listOf("v2", "v3", "v4")
        }

        context("lpushx") {
            test("without pushing") {
                // ARRANGE
                val key = randomUUID()

                // ACT
                val listSize = siths.lpushx(key, "test")

                // ASSERT
                listSize shouldBe 0
                siths.exists(key) shouldBe false
            }

            test("actually pushing") {
                // ARRANGE
                val key = randomUUID()
                siths.rpush(key, "test1")

                // ACT
                val listSize = siths.lpushx(key, "test2")

                // ASSERT
                listSize shouldBe 2
                siths.lrange(key, 0, -1) shouldBe listOf("test2", "test1")
            }
        }

        test("lmove") {
            // ARRANGE
            val source = randomUUID()
            siths.rpush(source, "v1", "v3", "v5")
            val destination = randomUUID()
            siths.rpush(destination, "v2", "v4", "v6")

            // ACT
            val element = siths.lmove(source, destination, ListEnd.LEFT, ListEnd.RIGHT)

            // ASSERT
            element shouldBe "v1"
            siths.lrange(destination, 0, -1) shouldBe listOf("v2", "v4", "v6", "v1")
        }

        context("blocking") {
            test("brpop fail") {
                // ARRANGE
                val key = randomUUID()

                // ACT
                val popped = siths.brpop(listOf(key), 20.milliseconds)

                // ASSERT
                popped shouldBe null
            }

            test("brpop blocks") {
                // ARRANGE
                val key = randomUUID()

                // ACT
                val popped = async { timed { siths.brpop(listOf(key)) } }
                withContext(Dispatchers.Default) {
                    delay(10)
                    siths.lpush(key, "value")
                }

                // ASSERT
                val (result, time) = popped.await()
                result?.data shouldBe "value"
                result?.source shouldBe key
                time shouldBeGreaterThanOrEqualTo 10
            }
        }
    }

    test("ping") {
        // ACT & ASSERT
        siths.ping() shouldBe true
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

            // ACT & ASSERT
            siths.expire(key, 10.seconds, expirationCondition = ExpirationCondition.NX) shouldBe false
        }
    }

    test("persist") {
        // ARRANGE
        val key = randomUUID()
        siths.set(key, "value")
        siths.expire(key, 10.seconds)

        // SANITY CHECK
        siths.ttl(key) shouldNotBe null

        // ACT
        val wasPersisted = siths.persist(key)

        // ASSERT
        wasPersisted shouldBe true
        siths.ttl(key) shouldBe null
    }
})
