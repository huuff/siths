package xyz.haff.siths.client

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import xyz.haff.siths.option.ExclusiveMode
import xyz.haff.siths.command.RedisCommand
import xyz.haff.siths.command.RedisCommandBuilder
import kotlin.time.Duration.Companion.seconds

class RedisCommandBuilderTest : FunSpec({
    val commandBuilder = RedisCommandBuilder()

    test("set") {
        val command = commandBuilder.set("key", "value", exclusiveMode = ExclusiveMode.NX, timeToLive = 1.seconds)

        command shouldBe RedisCommand("SET", "key", "value", "NX", "PX", "1000")
    }

    test("sintercard") {
        val command = commandBuilder.sintercard("key1", "key2", "key3", limit = 10)

        command shouldBe RedisCommand("SINTERCARD", "3", "key1", "key2", "key3", "LIMIT", 10)
    }

    test("sscan") {
        val command = commandBuilder.sscan("key", cursor = 73L, match = "pattern*", count = 25)

        command shouldBe RedisCommand("SSCAN", "key", "73", "MATCH", "pattern*", "COUNT", "25")
    }

})
