package xyz.haff.siths.command

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import xyz.haff.siths.option.ExistenceCondition
import kotlin.time.Duration.Companion.seconds

class RedisCommandBuilderTest : FunSpec({
    val commandBuilder = RedisCommandBuilder()

    test("set") {
        val command = commandBuilder.set("key", "value", existenceCondition = ExistenceCondition.NX, timeToLive = 1.seconds)

        command shouldBe RedisCommand("SET", "key", "value", "NX", "PX", "1000")
    }

    test("sintercard") {
        val command = commandBuilder.sinterCard("key1", "key2", "key3", limit = 10)

        command shouldBe RedisCommand("SINTERCARD", "3", "key1", "key2", "key3", "LIMIT", 10)
    }

    test("sscan") {
        val command = commandBuilder.sscan("key", cursor = 73L, match = "pattern*", count = 25)

        command shouldBe RedisCommand("SSCAN", "key", "73", "MATCH", "pattern*", "COUNT", "25")
    }

    test("zadd") {
        val command = commandBuilder.zadd("key", 1.0 to "1", 2.0 to "2", 3.0 to "3", 4.0 to "4")

        command shouldBe RedisCommand("ZADD", "key", "1.0", "1", "2.0", "2", "3.0", "3", "4.0", "4")
    }

})
