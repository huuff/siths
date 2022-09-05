package xyz.haff.siths

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.testcontainers.containers.GenericContainer
import xyz.haff.siths.client.pooled.ManagedSithsClient
import xyz.haff.siths.protocol.RedisConnection
import xyz.haff.siths.protocol.SithsConnectionPool
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

fun makeSithsClient(container: GenericContainer<*>) = ManagedSithsClient(makeSithsPool(container))

fun makeRedisConnection(container: GenericContainer<*>, password: String? = null) = RedisConnection(
    host = container.host,
    port = container.firstMappedPort,
    password = password,
)

fun makeSithsPool(container: GenericContainer<*>, maxConnections: Int = 10, acquireTimeout: Duration = 10.seconds)
    = SithsConnectionPool(redisConnection = makeRedisConnection(container), maxConnections = maxConnections, acquireTimeout = acquireTimeout)

/**
 * For when I want to test with containers deployed on my machine instead of testcontainers, so I can use MONITOR and
 * inspect it closely
 */
fun makeLocalSithsPool() = SithsConnectionPool(RedisConnection(host = "localhost", port = 6379))


inline fun threaded(threadNumber: Int, crossinline f: (threadIndex: Int) -> Unit) = (0 until threadNumber).map { threadIndex ->
    Thread {
        f(threadIndex)
    }
}
    .onEach { it.start() }
    .onEach { it.join() }

suspend inline fun suspended(coroutineNumber: Int, crossinline f: suspend (coroutineIndex: Int) -> Unit) = coroutineScope {
    (0 until coroutineNumber).forEach {
        launch {
            f(it)
        }
    }
}