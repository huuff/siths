package xyz.haff.siths.dstructures

import kotlinx.coroutines.runBlocking
import xyz.haff.siths.client.pooled.ManagedSithsClient
import xyz.haff.siths.pipelining.SithsPipelinedClient
import xyz.haff.siths.client.withRedis
import xyz.haff.siths.common.headAndTail
import xyz.haff.siths.common.randomUUID
import xyz.haff.siths.protocol.SithsConnectionPool
import xyz.haff.siths.protocol.luaBooleanToBoolean
import xyz.haff.siths.scripts.RedisScripts
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

// TODO: Try to find the redis error for a non-existent index and convert it to IndexOutOfBoundsException?
class SithsList<T : Any>(
    private val connectionPool: SithsConnectionPool,
    val name: String = "list:${randomUUID()}",
    private val serialize: (T) -> String,
    private val deserialize: (String) -> T,
    private val maxCursorSize: Int = 10,
) : MutableList<T> {
    private val client = ManagedSithsClient(connectionPool)

    companion object {
        @JvmStatic
        fun ofStrings(connectionPool: SithsConnectionPool, name: String = "list:${randomUUID()}", maxCursorSize: Int = 10) = SithsList(
            connectionPool = connectionPool,
            name = name,
            serialize = { it },
            deserialize = { it },
            maxCursorSize = maxCursorSize,
        )
    }

    override val size: Int
        get() = runBlocking { client.llen(name) }.toInt()

    override fun contains(element: T): Boolean
        = runBlocking { client.lpos(name, serialize(element)) != null }

    override fun containsAll(elements: Collection<T>): Boolean
        = runBlocking {
            connectionPool.get().use { conn ->
                val pipeline = SithsPipelinedClient(conn)
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

    override fun iterator(): MutableIterator<T> = runBlocking {
        connectionPool.get().use { conn ->
            val pipeline = SithsPipelinedClient(conn)
            val cursorContents = pipeline.lrange(name, 0, maxCursorSize - 1)
            val size = pipeline.llen(name)
            pipeline.exec(inTransaction = true)
            return@use Iterator(
                lastCursor = Cursor(cursorContents.get().map(deserialize).toMutableList(), 0, cursorContents.get().size - 1),
                size = size.get().toInt()
            )
        }
    }

    override fun lastIndexOf(element: T): Int
        = runBlocking { client.lpos(name, serialize(element), rank = -1)?.toInt() ?: -1 }

    override fun add(element: T): Boolean {
        return runBlocking {
            connectionPool.get().use { conn ->
                val pipeline = SithsPipelinedClient(conn)
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
                val pipeline = SithsPipelinedClient(conn)
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

    override fun listIterator(): MutableListIterator<T> = listIterator(0)

    override fun listIterator(index: Int): MutableListIterator<T> = runBlocking {
        val elementsBeforeIndex = max(index - 1, 0)
        // The start of the list, or the requested index minus half the cursor length
        val cursorStart = index - floor(min(elementsBeforeIndex.toDouble(), maxCursorSize.toDouble()/2)).toInt()

        return@runBlocking connectionPool.get().use { conn ->
            val pipeline = SithsPipelinedClient(conn)
            val size = pipeline.llen(name)
            val cursorContents = pipeline.lrange(name, cursorStart, cursorStart + maxCursorSize)
            pipeline.exec(inTransaction = true)
            return@use ListIterator(
                lastCursor = Cursor(cursorContents.get().map(deserialize).toMutableList(), cursorStart, cursorStart + (cursorContents.get().size - 1)),
                size = size.get().toInt(),
                currentIndex = index - 1,
            )

        }
    }

    override fun remove(element: T): Boolean {
        return runBlocking { client.lrem(name, serialize(element), count = 1 ) == 1L}
    }

    override fun removeAll(elements: Collection<T>): Boolean = runBlocking {
            connectionPool.get().use { conn ->
                val pipeline = SithsPipelinedClient(conn)
                val responses = elements.map {
                    pipeline.lrem(name, serialize(it), count = 0)
                }
                pipeline.exec(inTransaction = true)
                responses.asSequence().map { it.get() }.reduce(Long::plus) != 0L
            }
        }

    override fun removeAt(index: Int): T = runBlocking {
        connectionPool.get().use { conn ->
            val pipeline = SithsPipelinedClient(conn)
            val removedElement = pipeline.lindex(name, index)
            val removeMarker = randomUUID()
            pipeline.lset(name, index, removeMarker)
            pipeline.lrem(name, removeMarker, count = 1)
            pipeline.exec(inTransaction = true)
            return@use deserialize(removedElement.get() ?: throw IndexOutOfBoundsException(index))
        }
    }

    // TODO: Three round-trips to the server because I can't run runScript in a single pipeline...
    override fun retainAll(elements: Collection<T>): Boolean = runBlocking {
        val temporaryOtherList = randomUUID()
        val (otherHead, otherTail) = elements.map(serialize).toTypedArray().headAndTail()
        client.rpush(temporaryOtherList, otherHead, *otherTail)

        val response = withRedis(connectionPool) {
            runScript(RedisScripts.LIST_RETAIN_ALL, keys = listOf(this@SithsList.name, temporaryOtherList))
        }.luaBooleanToBoolean()
        client.del(temporaryOtherList)

        return@runBlocking response
    }

    override fun set(index: Int, element: T): T = runBlocking {
        connectionPool.get().use { conn ->
            val pipeline = SithsPipelinedClient(conn)
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

    // TODO: Use locks somehow to prevent concurrent modifications?
    data class Cursor<T>(val contents: MutableList<T>, val start: Int, var stop: Int)
    open inner class Iterator(
        protected var lastCursor: Cursor<T>,
        protected var size: Int,
        protected var currentIndex: Int = lastCursor.start - 1
    ): MutableIterator<T> {
        protected val currentIndexInCursor get() = currentIndex - lastCursor.start

        override fun hasNext(): Boolean = currentIndex < (size - 1)

        override fun next(): T = if (currentIndex < lastCursor.stop) {
            currentIndex++
            lastCursor.contents[currentIndexInCursor]
        } else {
            val remainingElements = size - lastCursor.stop
            val cursorSize = if (remainingElements < maxCursorSize) { remainingElements } else { maxCursorSize }
            val newStart = lastCursor.stop + 1
            val newStop = newStart + (cursorSize - 1)
            lastCursor = Cursor(runBlocking { client.lrange(name, newStart, newStop) }.map(deserialize).toMutableList(), newStart, newStop)
            currentIndex++
            lastCursor.contents[0]
        }

        override fun remove() {
            if (currentIndex != -1) {
                lastCursor.contents.removeAt(currentIndexInCursor)
                size--
                lastCursor.stop--
                removeAt(currentIndex)
            }
        }

    }

    inner class ListIterator(
        lastCursor: Cursor<T>,
        size: Int,
        currentIndex: Int = lastCursor.start - 1
    ): MutableListIterator<T>, Iterator(lastCursor, size, currentIndex) {
        override fun hasPrevious(): Boolean = currentIndex > -1

        override fun nextIndex(): Int = if (hasNext()) { currentIndex + 1 } else { size }

        override fun previous(): T = if (currentIndex >= lastCursor.start) {
            lastCursor.contents[currentIndexInCursor].also {
                currentIndex--
            }
        } else {
            val elementsBeforeCurrentCursor = lastCursor.start
            val cursorSize = if (elementsBeforeCurrentCursor < maxCursorSize) { elementsBeforeCurrentCursor } else { maxCursorSize }
            val newStart = lastCursor.start - cursorSize
            val newStop = lastCursor.start - 1
            lastCursor = Cursor(runBlocking { client.lrange(name, newStart, newStop) }.map(deserialize).toMutableList(), newStart, newStop)
            currentIndex--
            lastCursor.contents[lastCursor.contents.size - 1]
        }

        override fun previousIndex(): Int = currentIndex

        override fun add(element: T) {
            size++
            lastCursor.stop++
            val positionToInsert = when {
                currentIndexInCursor < 0 -> 0
                currentIndexInCursor >= lastCursor.contents.size -> lastCursor.contents.size
                else -> currentIndexInCursor+1
            }
            lastCursor.contents.add(positionToInsert, element)

            runBlocking {
                withRedis(connectionPool) {
                    runScript(RedisScripts.LIST_INSERT_AT, keys = listOf(name), args = listOf(currentIndex.toString(), serialize(element)))
                }
            }
        }

        override fun set(element: T) {
            if (currentIndex != -1) {
                runBlocking { client.lset(name, currentIndex, serialize(element)) }
                lastCursor.contents[currentIndexInCursor] = element
            }
        }

    }
}