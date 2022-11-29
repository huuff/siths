package xyz.haff.siths

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.testcontainers.containers.GenericContainer
import xyz.haff.siths.client.StandaloneSithsClient
import xyz.haff.siths.client.api.SithsImmediateClient
import xyz.haff.siths.client.pooled.ManagedSithsClient
import xyz.haff.siths.command.RedisCommandBuilder
import xyz.haff.siths.protocol.RedisConnection
import xyz.haff.siths.protocol.SithsConnectionPool
import xyz.haff.siths.protocol.StandaloneSithsConnection
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

suspend fun <T> runInContainer(container: GenericContainer<*>, f: suspend SithsImmediateClient.() -> T): T {
    return StandaloneSithsConnection.open(makeRedisConnection(container)).use { connection ->
        val client = StandaloneSithsClient(connection = connection, commandBuilder = RedisCommandBuilder())
        client.f()
    }
}

suspend inline fun suspended(coroutineNumber: Int, crossinline f: suspend (coroutineIndex: Int) -> Unit) = coroutineScope {
    (0 until coroutineNumber).forEach {
        launch {
            f(it)
        }
    }
}