package xyz.haff.siths.client

import kotlinx.coroutines.runBlocking
import xyz.haff.siths.common.headAndTail
import xyz.haff.siths.common.randomUUID

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
                sizePriorToUpdate != sizeAfterUpdate
            }
        }
    }

    override fun add(index: Int, element: T) {
        // TODO: A bit complex but I must do it, check https://stackoverflow.com/questions/21692456/insert-value-by-index-in-redis-list
        TODO("Not yet implemented")
    }

    override fun addAll(index: Int, elements: Collection<T>): Boolean {
        TODO("Not yet implemented")
    }

    override fun addAll(elements: Collection<T>): Boolean {
        val (head, tail) = elements.map(serialize).toTypedArray().headAndTail()

        return runBlocking {
            connectionPool.get().use { conn ->
                val pipeline = RedisPipelineBuilder(conn)
                val sizePriorToUpdate = pipeline.llen(name)
                val sizeAfterUpdate = pipeline.rpush(name, head, *tail)
                pipeline.exec(inTransaction = true)
                sizePriorToUpdate != sizeAfterUpdate
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

    override fun removeAll(elements: Collection<T>): Boolean {
        return runBlocking {
            connectionPool.get().use { conn ->
                val pipeline = RedisPipelineBuilder(conn)
                val responses = elements.map {
                    pipeline.lrem(name, serialize(it), count = 0)
                }
                pipeline.exec(inTransaction = true)
                responses.asSequence().map { it.get() }.reduce(Long::plus) != 0L
            }
        }
    }

    override fun removeAt(index: Int): T {
        TODO("Not yet implemented")
    }

    override fun retainAll(elements: Collection<T>): Boolean {
        TODO("Not yet implemented")
    }

    // TODO: Test
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