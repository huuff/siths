package xyz.haff.siths.client

import kotlinx.coroutines.runBlocking
import xyz.haff.siths.common.RedisUnexpectedRespResponseException
import xyz.haff.siths.common.handleUnexpectedRespResponse
import xyz.haff.siths.common.headAndTail
import xyz.haff.siths.common.randomUUID
import java.util.*

// TODO: I should test this all!
// TODO: I delete all temporary sets I make... but that's not enough, I should also set an expiration to make sure they
// eventually get removed in case the `del` is never executed due to some error. UPDATE: Not gonna work,
// maybe I should use a transaction
class SithsSet<T: Any>(
    private val sithsPool: SithsPool,
    private val name: String = "set:${UUID.randomUUID()}"
) : MutableSet<T> {
    private val sithsClient = PooledSithsClient(sithsPool)

    override fun add(element: T): Boolean = runBlocking { sithsClient.sadd(name, element) == 1L }

    // OPT: Prevent copying the array
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

    override fun iterator(): MutableIterator<T> {
        TODO("Not yet implemented")
    }

    override fun remove(element: T): Boolean = runBlocking { sithsClient.srem(name, element) != 0L }

    // TODO: I'm sure I can heavily dry removeAll, retainAll and containsAll, since they differ in one or two lines at most
    override fun removeAll(elements: Collection<T>): Boolean {
        val (otherSetHead, otherSetTail) = (elements.toSet() as Set<Any>).toTypedArray().headAndTail()
        val sizePriorToChange = this.size
        val pipelineResults = runBlocking {
            withRedis(sithsPool) {
                pipelined {
                    val otherSetKey = randomUUID()
                    sadd(otherSetKey, otherSetHead, *otherSetTail)
                    sdiffstore(this@SithsSet.name, this@SithsSet.name, otherSetKey)
                    del(otherSetKey)
                }
            }
        }

        return when (val sdiffstoreResponse = pipelineResults[1]) {
            is RespInteger -> sdiffstoreResponse.value.toInt() != sizePriorToChange
            else -> handleUnexpectedRespResponse(sdiffstoreResponse)
        }
    }

    override fun retainAll(elements: Collection<T>): Boolean {
        val (otherSetHead, otherSetTail) = (elements.toSet() as Set<Any>).toTypedArray().headAndTail()
        val sizePriorToChange = this.size
        val pipelineResults = runBlocking {
            withRedis(sithsPool) {
                pipelined {
                    val otherSetKey = randomUUID()
                    sadd(otherSetKey, otherSetHead, *otherSetTail)
                    sinterstore(this@SithsSet.name, this@SithsSet.name, otherSetKey)
                    del(otherSetKey)
                }
            }
        }

        return when (val sinterstoreResponse = pipelineResults[1]) {
            is RespInteger -> sinterstoreResponse.value.toInt() != sizePriorToChange
            else -> handleUnexpectedRespResponse(sinterstoreResponse)
        }
    }

    // TODO: Can I just cache this or manage it locally to avoid making a new roundtrip? retainAll and removeAll use them
    // outside of the pipeline, so at minimum I can move them into the pipeline (and thus, use scard only) but maybe I just
    // can manage the set size locally and make it very optimal to use. UPDATE: Obviously not possible, since this is
    // intended to be used as a distributed data structure, what if another client modifies it? I should just put it
    // in a pipeline
    override val size: Int
        get() = runBlocking { sithsClient.scard(name).toInt() }

    override fun contains(element: T): Boolean = runBlocking { sithsClient.sismember(name, element) }

    override fun containsAll(elements: Collection<T>): Boolean {
        val otherSet = (elements.toSet() as Set<Any>).toTypedArray()
        val (otherSetHead, otherSetTail) = otherSet.headAndTail()
        val pipelineResults = runBlocking {
            withRedis(sithsPool) {
                pipelined {
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
}