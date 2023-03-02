package xyz.haff.siths.pooling

import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class ExhaustedPoolException(maxResources: Int) : RuntimeException("This pool has all $maxResources resources busy")
class PoolHealingException(e: ConcurrentModificationException) :
    RuntimeException("Failed healing pool because it was modified concurrently", e)

class DefaultPool<ResourceType, PooledResourceType : PooledResource<ResourceType>>(
    private val acquireTimeout: Duration = 10.seconds,
    private val maxResources: Int = 10,
    private val createNewResource: suspend (Pool<ResourceType, PooledResourceType>) -> PooledResourceType,
) : Pool<ResourceType, PooledResourceType> {
    private var resources = mapOf<String, PooledResourceType>()
    private val mutex = Mutex()

    override val currentResources get() = resources.values.count { it.status != PoolStatus.BROKEN }

    override suspend fun get(): PooledResourceType {
        val deadline = System.currentTimeMillis() + acquireTimeout.inWholeMilliseconds

        while (System.currentTimeMillis() < deadline) {
            mutex.withLock {
                // First, clean all broken resources
                try {
                    resources = resources.filter { it.value.status != PoolStatus.BROKEN }
                } catch (e: ConcurrentModificationException) {
                    throw PoolHealingException(e)
                }


                val freeConnection = resources.values.find { it.status == PoolStatus.FREE }
                if (freeConnection != null) {
                    freeConnection.status = PoolStatus.BUSY
                    return freeConnection
                } else if (currentResources < maxResources) {
                    val connection = createNewResource(this)
                    connection.status = PoolStatus.BUSY
                    resources = resources + (connection.identifier to connection)
                    return connection
                }
            }
            delay(10L)
        }

        throw ExhaustedPoolException(maxResources)
    }

    // TODO: Should maybe be suspend and called in mutex
    override fun release(resourceIdentifier: String) {
        resources[resourceIdentifier]?.let {
            if (it.status == PoolStatus.BUSY) {
                it.status = PoolStatus.FREE
            }
        }
    }
}