package xyz.haff.siths.client

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.ktor.network.sockets.*
import xyz.haff.siths.protocol.RedisClient
import kotlin.time.Duration.Companion.seconds

class RedisClientTest : FunSpec({

    test("correctly parses string") {
        // ARRANGE
        val clientListResponse = "id=18 addr=172.17.0.1:56132 laddr=172.17.0.2:6379 fd=9 name= age=25639 idle=0 flags=N db=0 sub=0 psub=0 ssub=0 multi=-1 qbuf=13 qbuf-free=20461 argv-mem=10 multi-mem=0 rbs=1024 rbp=0 obl=0 oll=0 omem=0 tot-mem=22298 events=r cmd=client|list user=default redir=-1 resp=2"

        // ACT
        val client = RedisClient.fromString(clientListResponse)

        // ASSERT
        client shouldBe RedisClient(
            id = "18",
            addr = InetSocketAddress("172.17.0.1", 56132),
            laddr = InetSocketAddress("172.17.0.2", 6379),
            fd = 9,
            name = null,
            age = 25639.seconds,
            idle = 0.seconds,
            flags = "N",
            db = 0,
            sub = 0,
            psub = 0,
            ssub = 0,
            multi = -1,
            qbuf = 13,
            qbufFree = 20461,
            argvMem = 10,
            multiMem = 0,
            rbs = 1024,
            rbp = 0,
            obl = 0,
            oll = 0,
            omem = 0,
            totMem = 22298,
            events = "r",
            cmd = "client|list",
            user = "default",
            redir = -1,
            resp = 2,
        )
    }

})
