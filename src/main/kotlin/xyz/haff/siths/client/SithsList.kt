package xyz.haff.siths.client

import kotlinx.coroutines.runBlocking
import xyz.haff.siths.common.randomUUID
import java.util.*

class SithsList<T: Any>(
    private val connectionPool: SithsConnectionPool,
    val name: String = "list:${randomUUID()}",
    private val serializer: (T) -> String,
    private val deserializer: (String) -> T
): MutableList<T> {
    private val client = ManagedSithsClient(connectionPool)

    companion object {
        @JvmStatic
        fun ofStrings(connectionPool: SithsConnectionPool, name: String = "list:${randomUUID()}") = SithsList(
            connectionPool = connectionPool,
            name = name,
            serializer = { it },
            deserializer = { it },
        )
    }

    override val size: Int
        get() = runBlocking { client.llen(name) }.toInt()

    override fun contains(element: T): Boolean {
        TODO("Implement LPOS first")
    }

    override fun containsAll(elements: Collection<T>): Boolean {
        TODO("Implement LPOS first")
    }

    override fun get(index: Int): T {
        TODO("Implement LINDEX first!")
    }

    override fun indexOf(element: T): Int {
        TODO("Not yet implemented")
    }

    override fun isEmpty(): Boolean = this.size == 0

    override fun iterator(): MutableIterator<T> {
        TODO("Not yet implemented")
    }

    override fun lastIndexOf(element: T): Int {
        TODO("Not yet implemented")
    }

    override fun add(element: T): Boolean {
        return runBlocking {
            connectionPool.get().use { conn ->
                val pipeline = RedisPipelineBuilder(conn)
                val sizePriorToUpdate = pipeline.llen(name)
                val sizeAfterUpdate = pipeline.rpush(name, serializer(element))
                pipeline.exec(inTransaction = true)
                sizePriorToUpdate != sizeAfterUpdate
            }
        }
    }

    override fun add(index: Int, element: T) {
        TODO("Not yet implemented")
    }

    override fun addAll(index: Int, elements: Collection<T>): Boolean {
        TODO("Not yet implemented")
    }

    override fun addAll(elements: Collection<T>): Boolean {
        TODO("Not yet implemented")
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
        TODO("Not yet implemented")
    }

    override fun removeAll(elements: Collection<T>): Boolean {
        TODO("Not yet implemented")
    }

    override fun removeAt(index: Int): T {
        TODO("Not yet implemented")
    }

    override fun retainAll(elements: Collection<T>): Boolean {
        TODO("Not yet implemented")
    }

    override fun set(index: Int, element: T): T {
        TODO("Not yet implemented")
    }

    /**
     * Note that this returns a new Kotlin list, not backed by redis!
     */
    override fun subList(fromIndex: Int, toIndex: Int): MutableList<T> {
        return runBlocking { client.lrange(name, fromIndex, toIndex-1) }.map(deserializer).toMutableList()
    }
}