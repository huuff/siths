package xyz.haff.siths.pooling

interface Pool<out ResourceType, out PooledResourceType: PooledResource<ResourceType>> {

    val currentResources: Int
    suspend fun get(): PooledResourceType
    fun release(resourceIdentifier: String)
}