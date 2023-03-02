package xyz.haff.siths

import io.kotest.core.extensions.install
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.testcontainers.LifecycleMode
import io.kotest.extensions.testcontainers.TestContainerExtension
import io.mockk.InternalPlatformDsl.toStr
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import xyz.haff.siths.client.SithsDSL
import kotlin.time.Duration.Companion.seconds

/**
 * I'm finding broken connections being reused on highly concurrent scenarios... this test allows me to check that, but
 * it's obviously not suitable to be always run
 */
class HighConcurrencyTest : FunSpec({
    val container = install(TestContainerExtension("redis:7.0.4-alpine", LifecycleMode.Root)) {
        withExposedPorts(6379)
    }

    xtest("high concurrency") {
        val pool = makeSithsPool(container = container, acquireTimeout = 10.seconds)
        val siths = SithsDSL(pool)

        repeat((0..1000L).count()) { batch ->
            repeat((0L..500L).count()) {
                launch(Dispatchers.IO) {
                    val random = (1..100).random()
                    when ((1..10).random()) {
                        1 -> siths.set("key$random", random.toString())
                        2 -> siths.getOrNull("key$random")
                        in 3..8 -> siths.lpush("key${random * 121}", random.toStr())
                        else -> siths.rpop("key${random * 121}")
                    }
                }
            }
            println("Submitted batch $batch")
            delay(1)
        }

    }

})