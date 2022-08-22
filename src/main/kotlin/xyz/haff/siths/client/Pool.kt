package xyz.haff.siths.client

interface Pool<T> {

    suspend fun get(): PooledResource<T>
    fun release(resourceIdentifier: String)
    fun remove(resourceIdentifier: String)
}