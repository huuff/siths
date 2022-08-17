package xyz.haff.siths.client

import io.kotest.core.extensions.install
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.testcontainers.LifecycleMode
import io.kotest.extensions.testcontainers.TestContainerExtension
import io.kotest.matchers.shouldBe
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import java.nio.ByteBuffer

class SithsConnectionTest : FunSpec({
    val container = install(TestContainerExtension("redis:7.0.4-alpine", LifecycleMode.Root)) {
        withExposedPorts(6379)
    }

    test("can set and get a value") {
        // ARRANGE
        val connection = StandaloneSithsConnection.open(container.host, container.firstMappedPort)

        // ACT
        connection.runCommand(RedisCommand("SET", "key", "value"))
        val value = connection.runCommand(RedisCommand("GET", "key"))

        // ASSERT
        value.value shouldBe "value"
    }

    test("can pipeline commands") {
        // ARRANGE
        val connection = StandaloneSithsConnection.open(container.host, container.firstMappedPort)
        val pipeline = RedisPipeline(
            RedisCommand("PING"),
            RedisCommand("SET", "pipeline-key", "pipeline-value"),
            RedisCommand("PING"),
            RedisCommand("GET", "pipeline-key"),
        )

        // ACT
        val response = connection.runPipeline(pipeline)

        // ASSERT
        response shouldBe listOf(
            RespSimpleString("PONG"),
            RespSimpleString("OK"),
            RespSimpleString("PONG"),
            RespBulkString("pipeline-value"),
        )
    }
})