package xyz.haff.siths.pooling

import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class ExhaustedPoolException(maxResources: Int) : RuntimeException("This pool has all $maxResources resources busy")

class DefaultPool<ResourceType, PooledResourceType: PooledResource<ResourceType>>(
    private val acquireTimeout: Duration = 10.seconds,
    private val maxResources: Int = 10,
    private val createNewResource: suspend (Pool<ResourceType, PooledResourceType>) -> PooledResourceType,
): Pool<ResourceType, PooledResourceType> {
    private val resources = mutableMapOf<String, PooledResourceType>()
    private val mutex = Mutex()

    override val currentResources get() = resources.values.count { it.status != PoolStatus.BROKEN }
    
    override suspend fun get(): PooledResourceType {
        // First, clean all broken resources
        resources.forEach { (key, value) -> if (value.status == PoolStatus.BROKEN) { remove(key) } }

        val deadline = System.currentTimeMillis() + acquireTimeout.inWholeMilliseconds

        while (System.currentTimeMillis() < deadline) {
            mutex.withLock {
                if (resources.values.any { it.status == PoolStatus.FREE }) {
                    val connection = resources.values.find { it.status == PoolStatus.FREE }!!
                    connection.status = PoolStatus.BUSY
                    return connection
                } else {
                    if (currentResources < maxResources) {
                        val connection = createNewResource(this)
                        connection.status = PoolStatus.BUSY
                        resources[connection.identifier] = connection
                        return connection
                    }
                }
            }
            delay(10L)
        }

        throw ExhaustedPoolException(maxResources)
    }

    override fun release(resourceIdentifier: String) {
        resources[resourceIdentifier]?.let {
            if (it.status == PoolStatus.BUSY) {
                it.status = PoolStatus.FREE
            }
        }
    }

    override fun remove(resourceIdentifier: String) {
        resources -= resourceIdentifier
    }
}