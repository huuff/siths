package xyz.haff.siths.scripts

import io.kotest.core.extensions.install
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.testcontainers.LifecycleMode
import io.kotest.extensions.testcontainers.TestContainerExtension
import io.kotest.matchers.shouldBe
import xyz.haff.siths.client.api.SithsClient
import xyz.haff.siths.common.randomUUID
import xyz.haff.siths.makeSithsClient

class RedisScriptsTest : FunSpec({
    val container = install(TestContainerExtension("redis:7.0.4-alpine", LifecycleMode.Root)) {
        withExposedPorts(6379)
    }
    lateinit var siths: SithsClient

    beforeAny {
        siths = makeSithsClient(container)
    }

    test("list insert at") {
        // ARRANGE
        val list = randomUUID()
        siths.rpush(list, "v1", "v2", "v3")

        // ACT
        siths.eval(RedisScripts.LIST_INSERT_AT.code, keys = listOf(list), args = listOf("1", "v999"))

        // ASSERT
        siths.lrange(list, 0, -1) shouldBe listOf("v1", "v2", "v999", "v3")
    }

    test("list retain all") {
        // ARRANGE
        val list1 = randomUUID()
        siths.rpush(list1, "v1", "v2", "v3", "v4", "v5")
        val list2 = randomUUID()
        siths.rpush(list2, "v2", "v3")

        // ACT
        val response = siths.eval(RedisScripts.LIST_RETAIN_ALL.code, keys = listOf(list1, list2))

        // ASSERT
        response.value shouldBe 1
        siths.lrange(list1, 0, -1) shouldBe listOf("v2", "v3")
    }
})