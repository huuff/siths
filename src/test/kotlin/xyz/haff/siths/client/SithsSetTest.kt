package xyz.haff.siths.client

import io.kotest.core.extensions.install
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.testcontainers.TestContainerExtension
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import xyz.haff.siths.makeSithsPool

class SithsSetTest : FunSpec({
    val container = install(TestContainerExtension("redis:7.0.4-alpine")) {
        withExposedPorts(6379)
    }

    test("can add elements") {
        // ARRANGE
        val set = SithsSet<String>(sithsPool = makeSithsPool(container))

        // ACT
        set += "key1"
        set += "key2"

        // ASSERT
        set.size shouldBe 2
        ("key1" in set) shouldBe true
        ("key2" in set) shouldBe true
    }

    test("can remove elements") {
        // ARRANGE
        val set = SithsSet<String>(sithsPool = makeSithsPool(container))
        set += "key1"

        // SANITY CHECK
        ("key1" in set) shouldBe true
        set.size shouldBe 1

        // ACT
        set -= "key1"

        // ASSERT
        ("key1" in set) shouldBe false
        set.size shouldBe 0
    }

})
