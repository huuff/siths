package xyz.haff.siths.dstructures

import io.kotest.core.extensions.install
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.testcontainers.TestContainerExtension
import io.kotest.matchers.shouldBe
import xyz.haff.siths.makeSithsPool
import xyz.haff.siths.protocol.SithsConnectionPool

class SithsMapTest : FunSpec({
    val container = install(TestContainerExtension("redis:7.0.4-alpine")) {
        withExposedPorts(6379)
    }
    lateinit var pool: SithsConnectionPool

    beforeAny {
        pool = makeSithsPool(container)
    }

    test("put and get") {
        // ARRANGE
        val map = SithsMap.ofStrings(pool)

        // ACT
        val previousElem = map.put("key", "value")
        val newElem = map["key"]

        // ASSERT
        previousElem shouldBe null
        newElem shouldBe "value"
    }

    test("size and isEmpty") {
        // ARRANGE
        val map = SithsMap.ofStrings(pool)
        map["key1"] = "value1"
        map["key2"] = "value2"

        // ACT & ASSERT
        map.isEmpty() shouldBe false
        map.size shouldBe 2
    }

    test("containsKey") {
        // ARRANGE
        val map = SithsMap.ofStrings(pool)
        map["key1"] = "value1"
        map["key2"] = "value2"
        map["key3"] = "value3"

        // ACT & ASSERT
        map.containsKey("key2") shouldBe true
        map.containsKey("nonexistent") shouldBe false
    }

    test("containsValue") {
        // ARRANGE
        val map = SithsMap.ofStrings(pool)
        map["key1"] = "value1"
        map["key2"] = "value2"
        map["key3"] = "value3"

        // ACT & ASSERT
        map.containsValue("value2") shouldBe true
        map.containsValue("nonexistent") shouldBe false
    }

    test("keys") {
        // ARRANGE
        val map = SithsMap.ofStrings(pool)
        map["key1"] = "value1"
        map["key2"] = "value2"
        map["key3"] = "value3"

        // ACT & ASSERT
        map.keys shouldBe setOf("key1", "key2", "key3")
    }

    test("values") {
        // ARRANGE
        val map = SithsMap.ofStrings(pool)
        map["key1"] = "value1"
        map["key2"] = "value2"
        map["key3"] = "value3"

        // ACT & ASSERT
        map.values shouldBe setOf("value1", "value2", "value3")
    }

    test("entries") {
        // ARRANGE
        val map = SithsMap.ofStrings(pool)
        map["key1"] = "value1"
        map["key2"] = "value2"
        map["key3"] = "value3"

        // ACT
        val entriesAsPairs = map.entries.map { (key, value) -> key to value }.toSet()

        // ASSERT
        entriesAsPairs shouldBe setOf("key1" to "value1", "key2" to "value2", "key3" to "value3")
    }

    test("putAll") {
        // ARRANGE
        val map = SithsMap.ofStrings(pool)

        // ACT
        map.putAll(mapOf(
            "key1" to "value1",
            "key2" to "value2",
            "key3" to "value3"
        ))

        // ASSERT
        val entriesAsPairs = map.entries.map { (key, value) -> key to value }.toSet()
        entriesAsPairs shouldBe setOf("key1" to "value1", "key2" to "value2", "key3" to "value3")
    }

    test("remove") {
        // ARRANGE
        val map = SithsMap.ofStrings(pool)
        map["key1"] = "value1"
        map["key2"] = "value2"
        map["key3"] = "value3"

        // SANITY CHECK
        map.containsKey("key2") shouldBe true

        // ACT
        val removed = map.remove("key2")

        // ASSERT
        removed shouldBe "value2"
        map.containsKey("key2") shouldBe false
    }

})
