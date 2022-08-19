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
        return addedCount.toInt() == uniqueElements.size
    }

    override fun clear() {
        runBlocking { sithsClient.del(name) }
    }

    override fun iterator(): MutableIterator<T> {
        TODO("Not yet implemented")
    }

    override fun remove(element: T): Boolean = runBlocking { sithsClient.srem(name, element) == 1L }

    override fun removeAll(elements: Collection<T>): Boolean {
        TODO("Not yet implemented")
    }

    override fun retainAll(elements: Collection<T>): Boolean {
        TODO("Not yet implemented")
    }

    override val size: Int
        get() = runBlocking { sithsClient.scard(name).toInt() }

    override fun contains(element: T): Boolean = runBlocking { sithsClient.sismember(name, element) }

    override fun containsAll(elements: Collection<T>): Boolean {
        val otherSet = elements.toSet()
        val pipelineResults = runBlocking {
            withRedis(sithsPool) {
                pipelined {
                    val temporarySetKey = randomUUID()
                    sadd(temporarySetKey, this@SithsSet.name, otherSet.stream().toArray())
                    sintercard(this@SithsSet.name, temporarySetKey, limit = otherSet.size)
                    del(temporarySetKey)
                }
            }
        }

        val sintercardResponse = pipelineResults[1]
        return when (sintercardResponse) {
            is RespInteger -> sintercardResponse.value.toInt() == otherSet.size
            is RespError -> sintercardResponse.throwAsException()
            else -> throw RedisUnexpectedRespResponse(sintercardResponse)
        }
    }

    override fun isEmpty(): Boolean = size == 0
}