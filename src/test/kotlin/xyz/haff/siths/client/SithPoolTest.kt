package xyz.haff.siths.client

import io.kotest.core.extensions.install
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.testcontainers.TestContainerExtension
import io.kotest.matchers.shouldBe
import xyz.haff.siths.makeSithsPool
import xyz.haff.siths.suspended
import java.util.*

class SithPoolTest : FunSpec({
    val container = install(TestContainerExtension("redis:7.0.4-alpine")) {
        withExposedPorts(6379)
    }

    test("pooling works correctly") {
        val pool = makeSithsPool(container)

        suspended(100) { i ->
            val randomValue = UUID.randomUUID().toString()
            pool.getConnection().use { conn -> conn.command("SET key:$i $randomValue") }
            val retrievedValue = pool.getConnection().use { conn -> conn.command("GET key:$i") }

            retrievedValue.value shouldBe randomValue
        }
    }

})
