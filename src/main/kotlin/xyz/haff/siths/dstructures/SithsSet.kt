package xyz.haff.siths.dstructures

import kotlinx.coroutines.runBlocking
import xyz.haff.siths.client.pooled.ManagedSithsClient
import xyz.haff.siths.pipelining.SithsPipelinedClient
import xyz.haff.siths.client.pooled.SithsClientPool
import xyz.haff.siths.common.headAndTail
import xyz.haff.siths.common.randomUUID
import xyz.haff.siths.protocol.RedisCursor
import xyz.haff.siths.protocol.SithsConnectionPool
import java.util.*

class SithsSet<T : Any>(
    private val connectionPool: SithsConnectionPool,
    val name: String = "set:${UUID.randomUUID()}",
    private val serialize: (T) -> String,
    private val deserialize: (String) -> T
) : MutableSet<T> {
    private val client = ManagedSithsClient(pool = SithsClientPool(connectionPool))

    companion object {
        @JvmStatic
        fun ofStrings(
            sithsConnectionPool: SithsConnectionPool,
            name: String = "set:${UUID.randomUUID()}"
        ): SithsSet<String> =
            SithsSet(connectionPool = sithsConnectionPool, name = name, { it }, { it })
    }

    override fun add(element: T): Boolean = runBlocking { client.sadd(name, serialize(element)) == 1L }

    override fun addAll(elements: Collection<T>): Boolean {
        val (head, tail) = elements.map(serialize).toTypedArray().headAndTail()
        val addedCount = runBlocking { client.sadd(name, head, *tail) }
        return addedCount.toInt() != 0
    }

    override fun clear() {
        runBlocking { client.del(name) }
    }

    override fun iterator(): MutableIterator<T> = Iterator(runBlocking { client.sscan(name).map(deserialize) })

    override fun remove(element: T): Boolean = runBlocking { client.srem(name, serialize(element)) != 0L }

    override fun removeAll(elements: Collection<T>): Boolean {
        val (otherSetHead, otherSetTail) = elements.map(serialize).toTypedArray().headAndTail()
        return runBlocking {
            connectionPool.get().use { conn ->
                val otherSetKey = randomUUID()
                val pipeline = SithsPipelinedClient(conn)
                val sizeBeforeChange = pipeline.scard(this@SithsSet.name)
                pipeline.sadd(otherSetKey, otherSetHead, *otherSetTail)
                val sizeAfterChange = pipeline.sdiffstore(this@SithsSet.name, this@SithsSet.name, otherSetKey)
                pipeline.del(otherSetKey)
                pipeline.exec(inTransaction = true)

                return@use sizeBeforeChange.get() != sizeAfterChange.get()
            }
        }
    }

    override fun retainAll(elements: Collection<T>): Boolean {
        val (otherSetHead, otherSetTail) = elements.map(serialize).toTypedArray().headAndTail()
        return runBlocking {
            connectionPool.get().use { conn ->
                val pipeline = SithsPipelinedClient(conn)
                val sizeBeforeChange = pipeline.scard(this@SithsSet.name)
                val otherSetKey = randomUUID()
                pipeline.sadd(otherSetKey, otherSetHead, *otherSetTail)
                val sizeAfterChange = pipeline.sinterstore(this@SithsSet.name, this@SithsSet.name, otherSetKey)
                pipeline.del(otherSetKey)
                pipeline.exec(inTransaction = true)

                return@use sizeBeforeChange.get() != sizeAfterChange.get()
            }
        }
    }

    override val size: Int
        get() = runBlocking { client.scard(name).toInt() }

    override fun contains(element: T): Boolean = runBlocking { client.sismember(name, serialize(element)) }

    override fun containsAll(elements: Collection<T>): Boolean {
        val otherSet = elements.map(serialize).toTypedArray()
        val (otherSetHead, otherSetTail) = otherSet.headAndTail()
        return runBlocking {
            connectionPool.get().use { conn ->
                val pipeline = SithsPipelinedClient(conn)
                val temporalSetKey = randomUUID()
                pipeline.sadd(temporalSetKey, otherSetHead, *otherSetTail)
                val intersectionCardinality =
                    pipeline.sintercard(this@SithsSet.name, temporalSetKey, limit = otherSet.size)
                pipeline.del(temporalSetKey)
                pipeline.exec(inTransaction = true)
                return@use intersectionCardinality.get().toInt() == otherSet.size
            }
        }
    }

    override fun isEmpty(): Boolean = size == 0
}