package xyz.haff.siths.client

import io.kotest.core.extensions.install
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.testcontainers.TestContainerExtension
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import xyz.haff.siths.suspended
import xyz.haff.siths.threaded
import java.util.*
import kotlin.coroutines.suspendCoroutine

class SithPoolTest : FunSpec({
    val container = install(TestContainerExtension("redis:7.0.4-alpine")) {
        withExposedPorts(6379)
    }

    test("pooling works correctly") {
        val pool = SithPool(host = container.host, port = container.firstMappedPort)

        suspended(100) { i ->
            val randomValue = UUID.randomUUID().toString()
            pool.pooled { command("SET key:$i $randomValue") }
            val retrievedValue = pool.pooled { command("GET key:$i") }

            retrievedValue.value shouldBe randomValue
        }
    }

})
