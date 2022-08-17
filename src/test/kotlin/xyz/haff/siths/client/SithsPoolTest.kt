package xyz.haff.siths.client

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.extensions.install
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.testcontainers.TestContainerExtension
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import xyz.haff.siths.makeSithsPool
import xyz.haff.siths.suspended
import java.util.*

class SithsPoolTest : FunSpec({
    val container = install(TestContainerExtension("redis:7.0.4-alpine")) {
        withExposedPorts(6379)
    }

    test("pooling works correctly") {
        val pool = makeSithsPool(container)

        suspended(100) { i ->
            val randomValue = UUID.randomUUID().toString()
            pool.getConnection().use { conn -> conn.runCommand(RedisCommand("SET", "key:$i", randomValue)) }
            val retrievedValue = pool.getConnection().use { conn -> conn.runCommand(RedisCommand("GET", "key:$i")) }

            retrievedValue.value shouldBe randomValue
        }
    }

    // TODO: Use container
    xcontext("self-healing pool") {
        val host = "localhost"
        val port = 6379
        val pool = SithsPool(host = host, port = port, maxConnections = 1)
        val killedConnection = pool.getConnection()

        test("connection initially works (sanity check)") {
            killedConnection.use { it.runCommand(RedisCommand("PING")).value shouldBe "PONG" }
            pool.totalConnections shouldBe 1
        }

        test("calling the killed connection throws an exception") {
            // We kill the connection
            val killerConnection = StandaloneSithsConnection.open(host = host, port = port)
            println("Killer connection name: ${killerConnection.name}")
            println(killerConnection.runCommand(RedisCommand("INFO")))
            println(killerConnection.runCommand(RedisCommand("CLIENT", "KILL", killedConnection.name))) // TODO: Wont work! must use ID

            shouldThrow<BrokenRedisConnectionException> {
                killedConnection.use { it.runCommand(RedisCommand("PING")) }
            }
        }

        test("the connection is removed from the pool") {
            pool.totalConnections shouldBe 0
        }

        test("the pool creates a new one on request") {
            val newConnection = pool.getConnection()

            newConnection.name shouldNotBe killedConnection.name
            newConnection.use { it.runCommand(RedisCommand("PING")).value shouldBe "PONG" }
            pool.totalConnections shouldBe 1
        }
    }

})
