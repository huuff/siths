package xyz.haff.siths.client

import kotlinx.coroutines.delay
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

// TODO: Somewhere else?
class ExhaustedPoolException(maxResources: Int) : RuntimeException("This pool has all $maxResources resources busy")

// TODO: Make it thread-safe... @Synchronized?
class DefaultPool<ResourceType, PooledResourceType: PooledResource<ResourceType>>(
    private val acquireTimeout: Duration = 10.seconds,
    private val maxResources: Int = 10,
    private val createNewResource: suspend (Pool<ResourceType, PooledResourceType>) -> PooledResourceType,
): Pool<ResourceType, PooledResourceType> {
    private val resources = mutableMapOf<String, PooledResourceType>()

    override val currentResources get() = resources.size
    
    override suspend fun get(): PooledResourceType {
        val deadline = System.currentTimeMillis() + acquireTimeout.inWholeMilliseconds

        while (System.currentTimeMillis() < deadline) {
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
                } else {
                    delay(10)
                }
            }
        }

        throw ExhaustedPoolException(maxResources)
    }

    override fun release(resourceIdentifier: String) {
        resources[resourceIdentifier]?.status = PoolStatus.FREE
    }

    override fun remove(resourceIdentifier: String) {
        resources -= resourceIdentifier
    }
}