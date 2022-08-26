package xyz.haff.siths.client

import io.kotest.core.extensions.install
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.testcontainers.TestContainerExtension
import io.kotest.matchers.shouldBe
import xyz.haff.siths.makeSithsPool

class SithsListTest : FunSpec({
    val container = install(TestContainerExtension("redis:7.0.4-alpine")) {
        withExposedPorts(6379)
    }

    test("can add elements") {
        // ARRANGE
        val list = SithsList.ofStrings(makeSithsPool(container))

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
        val list = SithsList.ofStrings(makeSithsPool(container))

        // ACT & ASSERT
        list.isEmpty() shouldBe true
    }

    test("get") {
        // ARRANGE
        val list = SithsList.ofStrings(makeSithsPool(container))
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
        val list = SithsList.ofStrings(makeSithsPool(container))

        // ACT
        val wasModified = list.addAll(listOf("v1", "v2", "v3"))

        // ASSERT
        list.subList(0, list.size) shouldBe listOf("v1", "v2", "v3")
    }

})
