package xyz.haff.siths.dstructures

import io.kotest.core.extensions.install
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.testcontainers.TestContainerExtension
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import xyz.haff.siths.makeSithsClient
import xyz.haff.siths.makeSithsPool
import xyz.haff.siths.protocol.SithsConnectionPool

class SithsSetTest : FunSpec({
    val container = install(TestContainerExtension("redis:7.0.4-alpine")) {
        withExposedPorts(6379)
    }

    lateinit var pool: SithsConnectionPool

    beforeAny {
        pool = makeSithsPool(container)
    }

    test("can add elements") {
        // ARRANGE
        val set = SithsSet.ofStrings(pool)

        // ACT
        val wasModified1 = set.add("key1")
        val wasModified2 = set.add("key2")
        val wasModified3 = set.add("key1")

        // ASSERT
        set.size shouldBe 2
        ("key1" in set) shouldBe true
        ("key2" in set) shouldBe true
        wasModified1 shouldBe true
        wasModified2 shouldBe true
        wasModified3 shouldBe false
    }

    test("can remove elements") {
        // ARRANGE
        val set = SithsSet.ofStrings(pool)
        set += "key1"

        // SANITY CHECK
        ("key1" in set) shouldBe true
        set.size shouldBe 1

        // ACT
        val wasModified1 = set.remove("key1")
        val wasModified2 = set.remove("key1")

        // ASSERT
        ("key1" in set) shouldBe false
        set.size shouldBe 0
        wasModified1 shouldBe true
        wasModified2 shouldBe false
    }

    test("can add all") {
        // ARRANGE
        val set = SithsSet.ofStrings(pool)

        // ACT
        val wasModified = set.addAll(listOf("key1", "key2", "key3"))

        // ASSERT
        set.size shouldBe 3
        ("key1" in set) shouldBe true
        ("key2" in set) shouldBe true
        ("key3" in set) shouldBe true
        wasModified shouldBe true
    }

    test("can clear") {
        // ARRANGE
        val set = SithsSet.ofStrings(pool)
        set += "key1"

        // SANITY CHECK
        set.size shouldBe 1

        // ACT
        set.clear()

        // ASSERT
        set.size shouldBe 0
    }

    test("remove all") {
        // ARRANGE
        val set = SithsSet.ofStrings(pool)
        set.addAll(listOf("key1", "key2", "key3"))

        // ACT
        val wasModified = set.removeAll(listOf("key1", "key3"))

        // ASSERT
        wasModified shouldBe true
        set.size shouldBe 1
        ("key1" in set) shouldBe false
        ("key2" in set) shouldBe true
        ("key3" in set) shouldBe false
    }

    test("retain all") {
        // ARRANGE
        val set = SithsSet.ofStrings(pool)
        set.addAll(listOf("key1", "key2", "key3"))

        // ACT
        val wasModified = set.retainAll(listOf("key1", "key3"))

        // ASSERT
        wasModified shouldBe true
        set.size shouldBe 2
        ("key1" in set) shouldBe true
        ("key2" in set) shouldBe false
        ("key3" in set) shouldBe true
    }

    test("contains all") {
        // ARRANGE
        val set = SithsSet.ofStrings(pool)
        set.addAll(listOf("key1", "key2", "key3"))

        // ACT
        val containsAll = set.containsAll(listOf("key1", "key3"))

        // ASSERT
        containsAll shouldBe true
    }

    context("iterator") {
        test("correctly iterates collection") {
            // ARRANGE
            val set = SithsSet.ofStrings(pool)
            val expectedElements = (1..100).map { "value$it" }
            set.addAll(expectedElements)
            val iteratedElements = mutableListOf<String>()

            // ACT
            for (elem in set) {
                iteratedElements += elem
            }

            // ASSERT
            iteratedElements shouldContainExactlyInAnyOrder expectedElements
        }

        test("can remove") {
            // ARRANGE
            val set = SithsSet.ofStrings(pool)
            val client = makeSithsClient(container)
            val expectedElements = (1..20).map { "value$it" }
            set.addAll(expectedElements)

            // ACT
            val iterator = set.iterator()
            while (iterator.hasNext()) {
                if (iterator.next() == "value11") { iterator.remove() }
            }

            // ASSERT
            client.smembers(set.name) shouldBe (expectedElements.toSet() - "value11")
        }
    }
})
