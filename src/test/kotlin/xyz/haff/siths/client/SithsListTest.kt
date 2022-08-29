package xyz.haff.siths.client

import io.kotest.core.extensions.install
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.testcontainers.TestContainerExtension
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import xyz.haff.siths.makeSithsPool

class SithsListTest : FunSpec({
    val container = install(TestContainerExtension("redis:7.0.4-alpine")) {
        withExposedPorts(6379)
    }
    lateinit var pool: SithsConnectionPool

    beforeAny {
        pool = makeSithsPool(container)
    }

    test("can add elements") {
        // ARRANGE
        val list = SithsList.ofStrings(pool)

        // ACT
        val listModified1 = list.add("v1")
        val listModified2 = list.add("v2")

        // ASSERT
        listModified1 shouldBe true
        listModified2 shouldBe true
        list.size shouldBe 2
        list.subList(0, list.size) shouldBe listOf("v1", "v2")
    }

    test("isEmpty") {
        // ARRANGE
        val list = SithsList.ofStrings(pool)

        // ACT & ASSERT
        list.isEmpty() shouldBe true
    }

    test("get") {
        // ARRANGE
        val list = SithsList.ofStrings(pool)
        list += "v1"
        list += "v2"
        list += "v3"

        // ACT
        val secondElement = list[1]

        // ASSERT
        secondElement shouldBe "v2"
    }

    test("addAll") {
        // ARRANGE
        val list = SithsList.ofStrings(pool)

        // ACT
        val wasModified = list.addAll(listOf("v1", "v2", "v3"))

        // ASSERT
        wasModified shouldBe true
        list.subList(0, list.size) shouldBe listOf("v1", "v2", "v3")
    }

    test("remove") {
        // ARRANGE
        val list = SithsList.ofStrings(pool)
        list.addAll(listOf("v1", "v2", "v3"))

        // ACT
        val wasModified = list.remove("v2")

        // ASSERT
        wasModified shouldBe true
        list.subList(0, list.size) shouldBe listOf("v1", "v3")
    }

    context("removeAll") {
        test("actually removing") {
            // ARRANGE
            val list = SithsList.ofStrings(pool)
            list.addAll(listOf("v1", "v2", "v3"))

            // ACT
            val wasModified = list.removeAll(listOf("v1", "v2"))

            // ASSERT
            wasModified shouldBe true
            list.subList(0, list.size) shouldBe listOf("v3")
        }

        test("without removing") {
            // ARRANGE
            val list = SithsList.ofStrings(pool)
            list.addAll(listOf("v1", "v2", "v3"))

            // ACT
            val wasModified = list.removeAll(listOf("v4", "v5", "v8"))

            // ASSERT
            wasModified shouldBe false
            list.subList(0, list.size) shouldBe listOf("v1", "v2", "v3")
        }
    }

    test("contains") {
        // ARRANGE
        val list = SithsList.ofStrings(pool)
        list.addAll(listOf("v1", "v2", "v3"))

        // ACT & ASSERT
        ("v1" in list) shouldBe true
        ("v8" in list) shouldBe false
    }

    test("indexOf") {
        // ARRANGE
        val list = SithsList.ofStrings(pool)
        list.addAll(listOf("v1", "v2", "v3"))

        // ACT & ASSERT
        list.indexOf("v2") shouldBe 1
        list.indexOf("v8") shouldBe -1
    }

    test("lastIndexOf") {
        // ARRANGE
        val list = SithsList.ofStrings(pool)
        list.addAll(listOf("v1", "v2", "v1", "v3"))

        // ACT & ASSERT
        list.lastIndexOf("v1") shouldBe 2
        list.lastIndexOf("v2") shouldBe 1
        list.lastIndexOf("v8") shouldBe -1
    }

    test("contains all") {
        // ARRANGE
        val list = SithsList.ofStrings(pool)
        list.addAll(listOf("v1", "v2", "v3", "v4"))

        // ACT & ASSERT
        list.containsAll(listOf("v1", "v3")) shouldBe true
        list.containsAll(listOf("v1", "v2", "v5")) shouldBe false
    }

    test("set") {
        // ARRANGE
        val list = SithsList.ofStrings(pool)
        list.addAll(listOf("v1", "v2", "v3"))

        // ACT
        val previousElement = list.set(1, "v999")

        // ASSERT
        previousElement shouldBe "v2"
        list[1] shouldBe "v999"
    }

    test("removeAt") {
        // ARRANGE
        val list = SithsList.ofStrings(pool)
        list.addAll(listOf("v1", "v2", "v3", "v4"))

        // ACT
        val removedElement = list.removeAt(2)

        // ASSERT
        removedElement shouldBe "v3"
        list.subList(0, list.size) shouldBe listOf("v1", "v2", "v4")
    }

    test("add at index") {
        // ARRANGE
        val list = SithsList.ofStrings(pool)
        list.addAll(listOf("v1", "v2", "v3", "v4"))

        // ACT
        list.add(2, "v999")

        // ASSERT
        list.subList(0, list.size) shouldBe listOf("v1", "v2", "v3", "v999", "v4")
    }

    test("addAll at index") {
        // ARRANGE
        val list = SithsList.ofStrings(pool)
        list.addAll(listOf("v1", "v2", "v3", "v4"))

        // ACT
        list.addAll(2, listOf("v997", "v998", "v999"))

        // ASSERT
        list.subList(0, list.size) shouldBe listOf("v1", "v2", "v3", "v997", "v998", "v999", "v4")
    }

    context("iterator") {
        test("correctly iterated") {
            // ARRANGE
            val list = SithsList.ofStrings(pool)
            val elements = (1..100).map { "v$it" }
            list.addAll(elements)

            // ACT
            val iteratedElements = mutableListOf<String>()
            for (elem in list.iterator()) {
                iteratedElements += elem
            }

            // ASSERT
            iteratedElements shouldBe elements
        }

        test("can remove elements") {
            // ARRANGE
            val list = SithsList.ofStrings(pool)
            val elements = (1..100).map { it.toString() }
            list.addAll(elements)

            // ACT
            val iterator = list.iterator()
            while (iterator.hasNext()) {
                val value = iterator.next()
                if (value.toInt() % 3 == 0)
                    iterator.remove() // Remove multiples of 3
            }

            // ASSERT
            list.subList(0, list.size) shouldBe elements.filter { it.toInt() % 3 != 0 }
        }
    }

    test("retainAll") {
        // ARRANGE
        val list = SithsList.ofStrings(pool)
        list.addAll(listOf("v1", "v2", "v3", "v4", "v5"))

        // ACT
        val wasChanged = list.retainAll(listOf("v2", "v3"))

        // ASSERT
        wasChanged shouldBe true
        list.subList(0, list.size) shouldBe listOf("v2", "v3")
    }

    context("listIterator") {
        test("correctly iterates backwards") {
            // ARRANGE
            val list = SithsList.ofStrings(pool, maxCursorSize = 5)
            val elements = (1..15).map { it.toString() }
            list.addAll(elements)
            val iterator = list.listIterator()
            val retrievedElements = mutableListOf<String>()

            // advance iterator to the end
            while (iterator.hasNext()) iterator.next()

            // ACT
            while (iterator.hasPrevious()) {
                val previous = iterator.previous()
                retrievedElements += previous
            }

            // ASSERT
            retrievedElements shouldBe elements.reversed()
        }

        test("add") {
            // ARRANGE
            val list = SithsList.ofStrings(pool, maxCursorSize = 2)
            val elements = (1..5).map { it.toString() }
            list.addAll(elements)
            val iterator = list.listIterator()

            // ACT
            while (iterator.hasNext()) {
                val elem = iterator.next()
                val elemAsNumber = elem.toIntOrNull()
                if ((elemAsNumber != null) && elemAsNumber % 2 == 0)
                    iterator.add("EVEN") // Adds EVEN after every even number
            }

            // ASSERT
            list.subList(0, list.size) shouldBe listOf("1", "2", "EVEN", "3", "4", "EVEN", "5")
        }

        test("set") {
            // ARRANGE
            val list = SithsList.ofStrings(pool, maxCursorSize = 2)
            val elements = (1..5).map { it.toString() }
            list.addAll(elements)
            val iterator = list.listIterator()

            // ACT
            while (iterator.hasNext()) {
                val elem = iterator.next()
                if (elem == "3")
                    iterator.set("THREE")
            }

            // ASSERT
            list.subList(0, list.size) shouldBe listOf("1", "2", "THREE", "4", "5")
        }
    }

})
