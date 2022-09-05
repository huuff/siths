package xyz.haff.siths.scripts

import io.kotest.core.extensions.install
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.testcontainers.LifecycleMode
import io.kotest.extensions.testcontainers.TestContainerExtension
import io.kotest.matchers.shouldBe
import xyz.haff.siths.client.api.SithsImmediateClient
import xyz.haff.siths.common.randomUUID
import xyz.haff.siths.makeSithsClient

class RedisScriptsTest : FunSpec({
    val container = install(TestContainerExtension("redis:7.0.4-alpine", LifecycleMode.Root)) {
        withExposedPorts(6379)
    }
    lateinit var siths: SithsImmediateClient

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

    test("list inter store") {
        // ARRANGE
        val list1 = randomUUID()
        siths.rpush(list1, "v1", "v2", "v3", "v4", "v5")
        val list2 = randomUUID()
        siths.rpush(list2, "v2", "v3")

        // ACT
        val response = siths.eval(RedisScripts.LIST_INTER_STORE.code, keys = listOf(list1, list2))

        // ASSERT
        response.value shouldBe 1
        siths.lrange(list1, 0, -1) shouldBe listOf("v2", "v3")
    }

    test("delete by pattern") {
        // ARRANGE
        val keyToDelete1 = "delete:${randomUUID()}"
        siths.set(keyToDelete1, "anyvalue")
        val keyToDelete2 = "delete:${randomUUID()}"
        siths.set(keyToDelete2, "anyvalue")
        val keyNotToDelete = "nodelete:${randomUUID()}"
        siths.set(keyNotToDelete, "anyvalue")

        // SANITY CHECK
        siths.exists(keyToDelete1, keyToDelete2, keyNotToDelete) shouldBe true
        val sizeBeforeDeletion = siths.dbSize()

        // ACT
        siths.eval(RedisScripts.PDEL.code, listOf(), listOf("delete:*"))

        // ASSERT
        siths.exists(keyToDelete1, keyToDelete2) shouldBe false
        siths.exists(keyNotToDelete) shouldBe true
        val sizeAfterDeletion = siths.dbSize()
        (sizeBeforeDeletion - sizeAfterDeletion) shouldBe 2
    }
})