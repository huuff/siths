package xyz.haff.siths.benchmarks

import io.kotest.core.annotation.Ignored
import io.kotest.core.extensions.install
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.testcontainers.LifecycleMode
import io.kotest.extensions.testcontainers.TestContainerExtension
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import redis.clients.jedis.JedisPooled
import xyz.haff.siths.client.Siths
import xyz.haff.siths.makeSithsPool
import java.util.concurrent.atomic.AtomicInteger

@Ignored
class JedisVsSithsTest : FunSpec({
    val container = install(TestContainerExtension("redis:7.0.4-alpine", LifecycleMode.EveryTest)) {
        withExposedPorts(6379)
    }
    context("with many threads") {
        test("jedis performance") {
            val jedis = JedisPooled(container.host, container.firstMappedPort)
            val writes = AtomicInteger()
            val endTime = System.currentTimeMillis() + 60_000 // 10s

            runBlocking {
                repeat(10) {
                    launch {
                        while (System.currentTimeMillis() < endTime) {
                            val randomKey = (0..100_000_000).random()
                            val randomValue = (0..100_000_000).random()
                            jedis.set("key:$randomKey", randomValue.toString())
                            writes.incrementAndGet()
                        }
                    }
                }
            }

            println("Total writes: ${writes.get()}")
        }

        test("siths performance") {
            val siths = Siths(makeSithsPool(container))
            val writes = AtomicInteger()
            val endTime = System.currentTimeMillis() + 60_000 // 10s

            runBlocking {
                repeat(10) {
                    launch {
                        while (System.currentTimeMillis() < endTime) {
                            val randomKey = (0..100_000_000).random()
                            val randomValue = (0..100_000_000).random()
                            siths.set("key:$randomKey", randomValue.toString())
                            writes.incrementAndGet()
                        }
                    }
                }
            }

            println("Total writes: ${writes.get()}")
        }
    }

    context("single thread") {
        test("siths performance") {
            val siths = Siths(makeSithsPool(container))
            val writes = AtomicInteger()
            val endTime = System.currentTimeMillis() + 60_000 // 10s

            while (System.currentTimeMillis() < endTime) {
                val randomKey = (0..100_000_000).random()
                val randomValue = (0..100_000_000).random()
                siths.set("key:$randomKey", randomValue.toString())
                writes.incrementAndGet()
            }

            println("Total writes: ${writes.get()}")
        }

        test("jedis performance") {
            val jedis = JedisPooled(container.host, container.firstMappedPort)
            val writes = AtomicInteger()
            val endTime = System.currentTimeMillis() + 60_000 // 10s

            while (System.currentTimeMillis() < endTime) {
                val randomKey = (0..100_000_000).random()
                val randomValue = (0..100_000_000).random()
                jedis.set("key:$randomKey", randomValue.toString())
                writes.incrementAndGet()
            }

            println("Total writes: ${writes.get()}")
        }
    }
})