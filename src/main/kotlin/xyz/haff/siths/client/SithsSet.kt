package xyz.haff.siths.client

import kotlinx.coroutines.runBlocking
import xyz.haff.siths.common.RedisUnexpectedRespResponse
import xyz.haff.siths.common.randomUUID
import java.util.*

// TODO: I should test this all!
// TODO: I delete all temporary sets I make... but that's not enough, I should also set an expiration to make sure they
// eventually get removed in case the `del` is never executed due to some error
class SithsSet<T: Any>(
    private val sithsPool: SithsPool,
    private val name: String = "set:${UUID.randomUUID()}"
) : MutableSet<T> {
    private val sithsClient = PooledSithsClient(sithsPool)

    override fun add(element: T): Boolean = runBlocking { sithsClient.sadd(name, element) == 1L }

    override fun addAll(elements: Collection<T>): Boolean {
        val uniqueElements = elements.toSet().stream().toArray()
        val addedCount = runBlocking {
            sithsClient.sadd(name, uniqueElements[0], uniqueElements.slice(1 until uniqueElements.size))
        }
        return addedCount.toInt() != 0
    }

    override fun clear() {
        runBlocking { sithsClient.del(name) }
    }

    override fun iterator(): MutableIterator<T> {
        TODO("Not yet implemented")
    }

    override fun remove(element: T): Boolean = runBlocking { sithsClient.srem(name, element) != 0L }

    override fun removeAll(elements: Collection<T>): Boolean {
        val otherSet = elements.stream().toArray()
        val pipelineResults = runBlocking {
            withRedis(sithsPool) {
                pipelined {
                    val otherSetKey = randomUUID()
                    sadd(otherSetKey, otherSet[0], otherSet.slice(1 until otherSet.size))
                    sdiffstore(this@SithsSet.name, this@SithsSet.name, otherSetKey)
                    del(otherSetKey)
                }
            }
        }

        return when (val sdiffstoreResponse = pipelineResults[1]) {
            is RespInteger -> sdiffstoreResponse.value.toInt() != 0
            is RespError -> sdiffstoreResponse.throwAsException()
            else -> throw RedisUnexpectedRespResponse(sdiffstoreResponse)
        }
    }

    override fun retainAll(elements: Collection<T>): Boolean {
        TODO("Not yet implemented")
    }

    override val size: Int
        get() = runBlocking { sithsClient.scard(name).toInt() }

    override fun contains(element: T): Boolean = runBlocking { sithsClient.sismember(name, element) }

    override fun containsAll(elements: Collection<T>): Boolean {
        val otherSet = elements.toSet().stream().toArray()
        val pipelineResults = runBlocking {
            withRedis(sithsPool) {
                pipelined {
                    val temporarySetKey = randomUUID()
                    sadd(temporarySetKey, otherSet[0], otherSet.slice(1 until otherSet.size))
                    sintercard(this@SithsSet.name, temporarySetKey, limit = otherSet.size)
                    del(temporarySetKey)
                }
            }
        }

        return when (val sintercardResponse = pipelineResults[1]) {
            is RespInteger -> sintercardResponse.value.toInt() == otherSet.size
            is RespError -> sintercardResponse.throwAsException()
            else -> throw RedisUnexpectedRespResponse(sintercardResponse)
        }
    }

    override fun isEmpty(): Boolean = size == 0
}