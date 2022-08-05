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
            val connection = pool.getConnection()
            val randomValue = UUID.randomUUID().toString()
            val siths = Siths(connection)
            runBlocking { siths.set("key:$i", randomValue) }
            val retrievedValue = runBlocking { siths.get("key:$i") }

            retrievedValue shouldBe randomValue
            pool.releaseConnection(connection)
        }
    }

})
