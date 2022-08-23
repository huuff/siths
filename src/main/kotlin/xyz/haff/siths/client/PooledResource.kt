package xyz.haff.siths.client

enum class PoolStatus { FREE, BUSY }

interface PooledResource<out T>: AutoCloseable {
    val identifier: String
    val resource: T
    val pool: Pool<T, PooledResource<T>>
    var status: PoolStatus

    override fun close() = pool.release(identifier)
}