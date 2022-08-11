package xyz.haff.siths.client

import io.kotest.core.extensions.install
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.testcontainers.LifecycleMode
import io.kotest.extensions.testcontainers.TestContainerExtension
import io.kotest.matchers.shouldBe
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.util.*
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
        connection.command("SET key value")
        val value = connection.command("GET key")

        // ASSERT
        value.value shouldBe "value"
    }

    test("can send a byte buffer") {
        // ARRANGE
        val selectorManager = SelectorManager(Dispatchers.IO)
        val socket = aSocket(selectorManager).tcp().connect(container.host, container.firstMappedPort)
        val sendChannel = socket.openWriteChannel(autoFlush = false)
        val receiveChannel = socket.openReadChannel()

        // ACT
        val command = "PING\r\n".toByteArray(Charsets.UTF_8)
        val message = ByteBuffer.allocateDirect(command.size).apply { put(command) }
        sendChannel.writeFully(message.flip())
        sendChannel.flush()
        receiveChannel.awaitContent()
        val response = receiveChannel.readUTF8Line()

        // ASSERT
        response shouldBe "+PONG"
    }
})