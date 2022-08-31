package xyz.haff.siths.client

import io.kotest.core.extensions.install
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.testcontainers.LifecycleMode
import io.kotest.extensions.testcontainers.TestContainerExtension
import io.kotest.matchers.collections.shouldBeIn
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import xyz.haff.siths.client.api.SithsClient
import xyz.haff.siths.common.headAndTail
import xyz.haff.siths.common.randomUUID
import xyz.haff.siths.makeSithsClient

class SetSithsClientTest : FunSpec({
    val container = install(TestContainerExtension("redis:7.0.4-alpine", LifecycleMode.Root)) {
        withExposedPorts(6379)
    }
    lateinit var siths: SithsClient

    beforeAny {
        siths = makeSithsClient(container)
    }


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
})