package xyz.haff.siths.client

import kotlinx.coroutines.runBlocking
import xyz.haff.siths.common.headAndTail
import xyz.haff.siths.common.randomUUID
import xyz.haff.siths.scripts.RedisScript
import xyz.haff.siths.scripts.RedisScripts

// TODO: Try to find the redis error for a non-existent index and convert it to IndexOutOfBoundsException?
class SithsList<T : Any>(
    private val connectionPool: SithsConnectionPool,
    val name: String = "list:${randomUUID()}",
    private val serialize: (T) -> String,
    private val deserialize: (String) -> T
) : MutableList<T> {
    private val client = ManagedSithsClient(connectionPool)

    companion object {
        @JvmStatic
        fun ofStrings(connectionPool: SithsConnectionPool, name: String = "list:${randomUUID()}") = SithsList(
            connectionPool = connectionPool,
            name = name,
            serialize = { it },
            deserialize = { it },
        )
    }

    override val size: Int
        get() = runBlocking { client.llen(name) }.toInt()

    override fun contains(element: T): Boolean
        = runBlocking { client.lpos(name, serialize(element)) != null }

    override fun containsAll(elements: Collection<T>): Boolean
        = runBlocking {
            connectionPool.get().use { conn ->
                val pipeline = RedisPipelineBuilder(conn)
                val responses = elements.map { elem -> pipeline.lpos(name, serialize(elem)) }
                pipeline.exec(inTransaction = true)
                return@use responses.map { it.get() }.all { it != null }
            }
    }

    override fun get(index: Int): T = runBlocking {
        client.lindex(name, index)
            ?.let(deserialize)
            ?: throw IndexOutOfBoundsException(index)
    }

    override fun indexOf(element: T): Int
        = runBlocking { client.lpos(name, serialize(element))?.toInt() ?: -1 }

    override fun isEmpty(): Boolean = this.size == 0

    override fun iterator(): MutableIterator<T> {
        TODO("Not yet implemented")
    }

    override fun lastIndexOf(element: T): Int
        = runBlocking { client.lpos(name, serialize(element), rank = -1)?.toInt() ?: -1 }

    override fun add(element: T): Boolean {
        return runBlocking {
            connectionPool.get().use { conn ->
                val pipeline = RedisPipelineBuilder(conn)
                val sizePriorToUpdate = pipeline.llen(name)
                val sizeAfterUpdate = pipeline.rpush(name, serialize(element))
                pipeline.exec(inTransaction = true)
                sizePriorToUpdate.get() != sizeAfterUpdate.get()
            }
        }
    }

    override fun add(index: Int, element: T) {
        runBlocking {
            withRedis(connectionPool) {
                runScript(RedisScripts.LIST_INSERT_AT, keys = listOf(name), args = listOf(index.toString(), serialize(element)))
            }
        }
    }

    override fun addAll(index: Int, elements: Collection<T>): Boolean {
        runBlocking {
            withRedis(connectionPool) {
                runScript(RedisScripts.LIST_INSERT_AT, keys = listOf(name), args = listOf(index.toString()) + elements.toList().map(serialize))
            }
        }

        // XXX: A little hacky... returns whether the passed collection wasn't empty, since only in that case would the list be unchanged
        // (all others, such as index non-existent, should fail with an error)
        return elements.isNotEmpty()
    }

    override fun addAll(elements: Collection<T>): Boolean {
        val (head, tail) = elements.map(serialize).toTypedArray().headAndTail()

        return runBlocking {
            connectionPool.get().use { conn ->
                val pipeline = RedisPipelineBuilder(conn)
                val sizePriorToUpdate = pipeline.llen(name)
                val sizeAfterUpdate = pipeline.rpush(name, head, *tail)
                pipeline.exec(inTransaction = true)
                sizePriorToUpdate.get() != sizeAfterUpdate.get()
            }
        }
    }

    override fun clear() {
        runBlocking { client.del(name) }
    }

    override fun listIterator(): MutableListIterator<T> {
        TODO("Not yet implemented")
    }

    override fun listIterator(index: Int): MutableListIterator<T> {
        TODO("Not yet implemented")
    }

    override fun remove(element: T): Boolean {
        return runBlocking { client.lrem(name, serialize(element), count = 1 ) == 1L}
    }

    override fun removeAll(elements: Collection<T>): Boolean = runBlocking {
            connectionPool.get().use { conn ->
                val pipeline = RedisPipelineBuilder(conn)
                val responses = elements.map {
                    pipeline.lrem(name, serialize(it), count = 0)
                }
                pipeline.exec(inTransaction = true)
                responses.asSequence().map { it.get() }.reduce(Long::plus) != 0L
            }
        }

    override fun removeAt(index: Int): T = runBlocking {
        connectionPool.get().use { conn ->
            val pipeline = RedisPipelineBuilder(conn)
            val removedElement = pipeline.lindex(name, index)
            val removeMarker = randomUUID()
            pipeline.lset(name, index, removeMarker)
            pipeline.lrem(name, removeMarker, count = 1)
            pipeline.exec(inTransaction = true)
            return@use deserialize(removedElement.get() ?: throw IndexOutOfBoundsException(index))
        }
    }

    override fun retainAll(elements: Collection<T>): Boolean {
        TODO("Not yet implemented")
    }

    override fun set(index: Int, element: T): T = runBlocking {
        connectionPool.get().use { conn ->
            val pipeline = RedisPipelineBuilder(conn)
            val previousElement = pipeline.lindex(name, index)
            pipeline.lset(name, index, serialize(element))
            pipeline.exec(inTransaction = true)
            return@use deserialize(previousElement.get() ?: throw IndexOutOfBoundsException(index))
        }
    }

    /**
     * Note that this returns a new Kotlin list, not backed by redis!
     */
    override fun subList(fromIndex: Int, toIndex: Int): MutableList<T> {
        return runBlocking { client.lrange(name, fromIndex, toIndex - 1) }.map(deserialize).toMutableList()
    }
}