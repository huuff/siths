package xyz.haff.siths.client

import io.kotest.core.extensions.install
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.testcontainers.TestContainerExtension
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import xyz.haff.siths.threaded
import java.util.*

class SithPoolTest : FunSpec({
    val container = install(TestContainerExtension("redis:7.0.4-alpine")) {
        withExposedPorts(6379)
    }

    test("pooling works correctly") {
        val pool = SithPool(host = container.host, port = container.firstMappedPort)

        threaded(100) { i ->
            val randomValue = UUID.randomUUID().toString()
            runBlocking { pool.pooled { command("SET key:$i $randomValue") } }
            val retrievedValue = runBlocking { pool.pooled { command("GET key:$i") } }

            retrievedValue shouldBe randomValue
        }
    }

})
