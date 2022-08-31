package xyz.haff.siths.client

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.extensions.install
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.testcontainers.TestContainerExtension
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import xyz.haff.siths.common.RedisBrokenConnectionException
import xyz.haff.siths.makeRedisConnection
import xyz.haff.siths.makeSithsPool
import xyz.haff.siths.pooling.ExhaustedPoolException
import xyz.haff.siths.command.RedisCommand
import xyz.haff.siths.protocol.SithsConnectionPool
import xyz.haff.siths.protocol.StandaloneSithsConnection
import xyz.haff.siths.suspended
import java.util.*
import kotlin.time.Duration.Companion.milliseconds

class SithsPoolTest : FunSpec({
    val container = install(TestContainerExtension("redis:7.0.4-alpine")) {
        withExposedPorts(6379)
    }

    test("pooling works correctly") {
        val pool = makeSithsPool(container)

        suspended(100) { i ->
            val randomValue = UUID.randomUUID().toString()
            pool.get().use { conn -> conn.runCommand(RedisCommand("SET", "key:$i", randomValue)) }
            val retrievedValue = pool.get().use { conn -> conn.runCommand(RedisCommand("GET", "key:$i")) }

            retrievedValue.value shouldBe randomValue
        }
    }

    test("can't go over the max number of connections") {
        // ARRANGE
        val pool = makeSithsPool(container, maxConnections = 3, acquireTimeout = 10.milliseconds)

        // ACT
        repeat(3) { pool.get() }

        // ASSERT
        shouldThrow<ExhaustedPoolException> { pool.get() }
        pool.currentResources shouldBe 3
    }

    context("self-healing pool") {
        val pool = SithsConnectionPool(makeRedisConnection(container), maxConnections = 1)
        val killedConnection = pool.get()

        test("connection initially works (sanity check)") {
            killedConnection.use { it.runCommand(RedisCommand("PING")).value shouldBe "PONG" }
            pool.currentResources shouldBe 1
        }

        test("calling the killed connection throws an exception") {
            // We kill the connection
            val killerConnection = StandaloneSithsConnection.open(makeRedisConnection(container))
            val clientListResponse = killerConnection.runCommand(RedisCommand("CLIENT", "LIST"))
            val connectedClients = parseClientList(clientListResponse.value as String)
            val idToKill = connectedClients.find { it.name == killedConnection.identifier }!!.id

            killerConnection.runCommand(RedisCommand("CLIENT", "KILL", "ID", idToKill))

            shouldThrow<RedisBrokenConnectionException> {
                killedConnection.use { it.runCommand(RedisCommand("PING")) }
            }
        }

        test("the connection is removed from the pool") {
            pool.currentResources shouldBe 0
        }

        test("the pool creates a new one on request") {
            val newConnection = pool.get()

            newConnection.identifier shouldNotBe killedConnection.identifier
            newConnection.use { it.runCommand(RedisCommand("PING")).value shouldBe "PONG" }
            pool.currentResources shouldBe 1
        }
    }

})
