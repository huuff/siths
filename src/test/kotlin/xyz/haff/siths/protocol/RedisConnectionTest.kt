package xyz.haff.siths.protocol

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

class RedisConnectionTest : FunSpec({
    val container = install(TestContainerExtension("redis:7.0.4-alpine", LifecycleMode.Root)) {
        withExposedPorts(6379)
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