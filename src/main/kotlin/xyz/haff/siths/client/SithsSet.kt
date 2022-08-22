package xyz.haff.siths.client

import kotlinx.coroutines.runBlocking
import xyz.haff.siths.common.handleUnexpectedRespResponse
import xyz.haff.siths.common.headAndTail
import xyz.haff.siths.common.randomUUID
import java.util.*

class SithsSet<T : Any>(
    private val sithsPool: SithsPool,
    val name: String = "set:${UUID.randomUUID()}",
    private val serializer: (T) -> String,
    private val deserializer: (String) -> T
) : MutableSet<T> {
    private val sithsClient = ManagedSithsClient(sithsPool)

    companion object {
        fun ofStrings(sithsPool: SithsPool, name: String = "set:${UUID.randomUUID()}"): SithsSet<String> =
            SithsSet(sithsPool = sithsPool, name = name, { it }, { it })
    }

    override fun add(element: T): Boolean = runBlocking { sithsClient.sadd(name, element) == 1L }

    override fun addAll(elements: Collection<T>): Boolean {
        val (head, tail) = (elements.toSet() as Set<Any>).toTypedArray().headAndTail()
        val addedCount = runBlocking {
            sithsClient.sadd(name, head, *tail)
        }
        return addedCount.toInt() != 0
    }

    override fun clear() {
        runBlocking { sithsClient.del(name) }
    }

    override fun iterator(): MutableIterator<T> = Iterator(runBlocking { sithsClient.sscan(name).map(deserializer) })

    override fun remove(element: T): Boolean = runBlocking { sithsClient.srem(name, element) != 0L }

    override fun removeAll(elements: Collection<T>): Boolean {
        val (otherSetHead, otherSetTail) = (elements.toSet() as Set<Any>).toTypedArray().headAndTail()
        val pipelineResults = runBlocking {
            withRedis(sithsPool) {
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
            else -> handleUnexpectedRespResponse(sdiffstoreResponse)
        }
    }

    override fun retainAll(elements: Collection<T>): Boolean {
        val (otherSetHead, otherSetTail) = (elements.toSet() as Set<Any>).toTypedArray().headAndTail()
        val pipelineResults = runBlocking {
            withRedis(sithsPool) {
                transactional {
                    val otherSetKey = randomUUID()
                    scard(this@SithsSet.name)
                    sadd(otherSetKey, otherSetHead, *otherSetTail)
                    sinterstore(this@SithsSet.name, this@SithsSet.name, otherSetKey)
                    del(otherSetKey)
                }
            }
        }

        val sizePriorToChange = (pipelineResults[0] as RespInteger).value
        return when (val sinterstoreResponse = pipelineResults[2]) {
            is RespInteger -> sinterstoreResponse.value != sizePriorToChange
            else -> handleUnexpectedRespResponse(sinterstoreResponse)
        }
    }

    override val size: Int
        get() = runBlocking { sithsClient.scard(name).toInt() }

    override fun contains(element: T): Boolean = runBlocking { sithsClient.sismember(name, element) }

    override fun containsAll(elements: Collection<T>): Boolean {
        val otherSet = (elements.toSet() as Set<Any>).toTypedArray()
        val (otherSetHead, otherSetTail) = otherSet.headAndTail()
        val pipelineResults = runBlocking {
            withRedis(sithsPool) {
                transactional {
                    val temporarySetKey = randomUUID()
                    sadd(temporarySetKey, otherSetHead, *otherSetTail)
                    sintercard(this@SithsSet.name, temporarySetKey, limit = otherSet.size)
                    del(temporarySetKey)
                }
            }
        }

        return when (val sintercardResponse = pipelineResults[1]) {
            is RespInteger -> sintercardResponse.value.toInt() == otherSet.size
            else -> handleUnexpectedRespResponse(sintercardResponse)
        }
    }

    override fun isEmpty(): Boolean = size == 0

    inner class Iterator(
        private var lastCursorResult: RedisCursor<T>,
    ) : MutableIterator<T> {
        private var positionWithinLastCursor = 0
        private var lastResult: T? = null // XXX: Only to implement `remove`... it's hacky but all of my other options were too

        override fun hasNext(): Boolean =
            lastCursorResult.next != 0L || positionWithinLastCursor < lastCursorResult.contents.size

        override fun next(): T = lastCursorResult.contents[positionWithinLastCursor].also {
            lastResult = it
            positionWithinLastCursor++
            // XXX: Overflowed the last cursor, but there are more elements
            if (positionWithinLastCursor >= lastCursorResult.contents.size && hasNext()) {
                lastCursorResult = runBlocking { sithsClient.sscan(name, lastCursorResult.next).map(deserializer) }
                positionWithinLastCursor = 0
            }
        }

        override fun remove() {
            if (lastResult != null) {
                // XXX: Non-null assertion is correct, since if `lastResult` has changed, it must have changed to a non-null value
                // (see `next()`)
                runBlocking { sithsClient.srem(name, serializer(lastResult!!)) }
            }
        }
    }
}