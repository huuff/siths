package xyz.haff.siths.client

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import xyz.haff.siths.protocol.RedisCommand

class RedisCommandTest : FunSpec({

    test("to RESP") {
        RedisCommand("AUTH", "FLIBBERTIGIBBETS").toResp() shouldBe "*2\r\n$4\r\nAUTH\r\n$16\r\nFLIBBERTIGIBBETS\r\n"
    }

})
