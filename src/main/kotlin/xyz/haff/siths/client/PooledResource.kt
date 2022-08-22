package xyz.haff.siths.client

enum class PoolStatus { FREE, BUSY }

interface PooledResource<T>: AutoCloseable {
    val identifier: String
    val resource: T
    val pool: Pool<T>
    var status: PoolStatus

    override fun close() = pool.release(identifier)
}