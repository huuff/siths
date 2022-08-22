package xyz.haff.siths.client

interface Pool<out ResourceType, out PooledResourceType: PooledResource<ResourceType> > {

    val currentResources: Int
    suspend fun get(): PooledResourceType
    fun release(resourceIdentifier: String)
    fun remove(resourceIdentifier: String)
}