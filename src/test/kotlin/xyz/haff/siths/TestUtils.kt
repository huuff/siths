package xyz.haff.siths

import org.testcontainers.containers.GenericContainer
import redis.clients.jedis.JedisPool

fun poolFromContainer(container: GenericContainer<Nothing>) = JedisPool(container.host, container.firstMappedPort)

inline fun threaded(threadNumber: Int, crossinline f: (threadIndex: Int) -> Unit) = (0 until threadNumber).map { threadIndex ->
    Thread {
        f(threadIndex)
    }
}
    .onEach { it.start() }
    .onEach { it.join() }