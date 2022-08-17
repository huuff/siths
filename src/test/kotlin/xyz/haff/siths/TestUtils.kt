package xyz.haff.siths

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.testcontainers.containers.GenericContainer
import redis.clients.jedis.JedisPool
import xyz.haff.siths.client.SithsPool

fun makeJedisPool(container: GenericContainer<*>) = JedisPool(container.host, container.firstMappedPort)
fun makeSithsPool(container: GenericContainer<*>, maxConnections: Int = 10)
    = SithsPool(container.host, container.firstMappedPort, maxConnections = maxConnections)

/**
 * For when I want to test with containers deployed on my machine instead of testcontainers, so I can use MONITOR and
 * inspect it closely
 */
fun makeLocalSithsPool() = SithsPool("localhost", 6379)


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