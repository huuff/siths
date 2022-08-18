package xyz.haff.siths.client

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.extensions.install
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.testcontainers.LifecycleMode
import io.kotest.extensions.testcontainers.TestContainerExtension
import io.kotest.matchers.shouldBe
import xyz.haff.siths.common.RedisAuthException
import xyz.haff.siths.common.RedisException

class AuthenticationTest : FunSpec({
    val password = "password"
    val container = install(TestContainerExtension("redis:7.0.4-alpine", LifecycleMode.Spec)) {
        withExposedPorts(6379)
        withCommand("redis-server", "--requirepass $password")
    }

    test("can't start an unauthenticated connection") {
        val error = shouldThrow<RedisException> {
            StandaloneSithsConnection.open(host = container.host, port = container.firstMappedPort)
        }

        error.type shouldBe "NOAUTH"
    }

    test("an authenticated connection works fine") {
        // ARRANGE
        val connection = StandaloneSithsConnection.open(host = container.host, port = container.firstMappedPort, password = password)

        // ACT & ASSERT
        connection.runCommand(RedisCommand("PING")).value shouldBe "PONG"
    }
})