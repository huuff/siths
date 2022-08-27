package xyz.haff.siths.client

import kotlinx.coroutines.runBlocking
import xyz.haff.siths.common.headAndTail
import xyz.haff.siths.common.randomUUID
import java.util.*

// TODO: Implement with new pipeline typesafe features!
class SithsSet<T : Any>(
    private val connectionPool: SithsConnectionPool,
    val name: String = "set:${UUID.randomUUID()}",
    private val serializer: (T) -> String,
    private val deserializer: (String) -> T
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

    override fun add(element: T): Boolean = runBlocking { client.sadd(name, element) == 1L }

    override fun addAll(elements: Collection<T>): Boolean {
        val (head, tail) = elements.map(serializer).toTypedArray().headAndTail()
        val addedCount = runBlocking { client.sadd(name, head, *tail) }
        return addedCount.toInt() != 0
    }

    override fun clear() {
        runBlocking { client.del(name) }
    }

    override fun iterator(): MutableIterator<T> = Iterator(runBlocking { client.sscan(name).map(deserializer) })

    override fun remove(element: T): Boolean = runBlocking { client.srem(name, element) != 0L }

    override fun removeAll(elements: Collection<T>): Boolean {
        val (otherSetHead, otherSetTail) = elements.map(serializer).toTypedArray().headAndTail()
        val pipelineResults = runBlocking {
            withRedis(connectionPool) {
                transactional {
                    val otherSetKey = randomUUID()
                    scard(this@SithsSet.name)
                    sadd(otherSetKey, otherSetHead, *otherSetTail)
                    sdiffstore(this@SithsSet.name, this@SithsSet.name, otherSetKey)
                    del(otherSetKey)
                }
            }
        }

        val sizePriorToChange = (pipelineResults[0] as RespInteger).value
        return when (val sdiffstoreResponse = pipelineResults[2]) {
            is RespInteger -> sdiffstoreResponse.value != sizePriorToChange
            else -> sdiffstoreResponse.handleAsUnexpected()
        }
    }

    override fun retainAll(elements: Collection<T>): Boolean {
        val (otherSetHead, otherSetTail) = elements.map(serializer).toTypedArray().headAndTail()
        return runBlocking {
            connectionPool.get().use { conn ->
                val pipeline = RedisPipelineBuilder(conn)
                val sizeBeforeChange = pipeline.scard(this@SithsSet.name)
                val otherSetKey = randomUUID()
                pipeline.sadd(otherSetKey, otherSetHead, *otherSetTail)
                val sizeAfterChange = pipeline.sinterstore(this@SithsSet.name, this@SithsSet.name, otherSetKey)
                pipeline.del(otherSetKey)
                pipeline.exec(inTransaction = true)

                return@use sizeBeforeChange != sizeAfterChange
            }
        }
    }

    override val size: Int
        get() = runBlocking { client.scard(name).toInt() }

    override fun contains(element: T): Boolean = runBlocking { client.sismember(name, element) }

    override fun containsAll(elements: Collection<T>): Boolean {
        val otherSet = elements.map(serializer).toTypedArray()
        val (otherSetHead, otherSetTail) = otherSet.headAndTail()
        return runBlocking {
            connectionPool.get().use { conn ->
                val pipeline = RedisPipelineBuilder(conn)
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

    inner class Iterator(
        private var lastCursorResult: RedisCursor<T>,
    ) : MutableIterator<T> {
        private var positionWithinLastCursor = 0
        private var lastResult: T? =
            null // XXX: Only to implement `remove`... it's hacky but all of my other options were too

        override fun hasNext(): Boolean =
            lastCursorResult.next != 0L || positionWithinLastCursor < lastCursorResult.contents.size

        override fun next(): T = lastCursorResult.contents[positionWithinLastCursor].also {
            lastResult = it
            positionWithinLastCursor++
            // XXX: Overflowed the last cursor, but there are more elements
            if (positionWithinLastCursor >= lastCursorResult.contents.size && hasNext()) {
                lastCursorResult = runBlocking { client.sscan(name, lastCursorResult.next).map(deserializer) }
                positionWithinLastCursor = 0
            }
        }

        override fun remove() {
            if (lastResult != null) {
                // XXX: Non-null assertion is correct, since if `lastResult` has changed, it must have changed to a non-null value
                // (see `next()`)
                runBlocking { client.srem(name, serializer(lastResult!!)) }
            }
        }
    }
}