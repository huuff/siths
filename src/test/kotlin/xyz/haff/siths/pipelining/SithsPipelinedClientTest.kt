package xyz.haff.siths.pipelining

import io.kotest.core.extensions.install
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.testcontainers.LifecycleMode
import io.kotest.extensions.testcontainers.TestContainerExtension
import io.kotest.matchers.comparables.shouldBeGreaterThanOrEqualTo
import io.kotest.matchers.comparables.shouldBeLessThanOrEqualTo
import io.kotest.matchers.shouldBe
import xyz.haff.siths.common.randomUUID
import xyz.haff.siths.makeSithsPool
import kotlin.time.Duration.Companion.seconds

class SithsPipelinedClientTest : FunSpec({
    val container = install(TestContainerExtension("redis:7.0.4-alpine", LifecycleMode.Root)) {
        withExposedPorts(6379)
    }

    test("can set and get") {
        // ARRANGE
        val connection = makeSithsPool(container).get()
        val pipeline = PipelinedSithsClient(connection)
        val key = randomUUID()

        // ACT
        val set = pipeline.set(key, "value")
        val get = pipeline.get(key)
        pipeline.exec()

        // ASSERT
        set.get() shouldBe Unit
        get.get() shouldBe "value"

        // TEARDOWN
        connection.close()
    }

    test("all kinds of operations") {
        // ARRANGE
        val connection = makeSithsPool(container).get()
        val pipeline = PipelinedSithsClient(connection)
        val key = randomUUID()

        // ACT
        val set = pipeline.set(key, "1")
        val incr = pipeline.incrBy(key, 1)
        val get = pipeline.get(key)
        val expire = pipeline.expire(key, 10.seconds)
        val ttl = pipeline.ttl(key)
        val del = pipeline.del(key)
        val exists = pipeline.exists(key)
        pipeline.exec()

        // ASSERT
        set.get() shouldBe Unit
        incr.get() shouldBe 2
        get.get() shouldBe "2"
        expire.get() shouldBe true

        ttl.get()!! shouldBeGreaterThanOrEqualTo 8.seconds
        ttl.get()!! shouldBeLessThanOrEqualTo 10.seconds

        del.get() shouldBe 1
        exists.get() shouldBe false

        // TEARDOWN
        connection.close()
    }

})
