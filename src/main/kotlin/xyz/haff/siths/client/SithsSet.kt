package xyz.haff.siths.client

import kotlinx.coroutines.runBlocking
import java.util.*

class SithsSet<T: Any>(
    private val sithsClient: SithsClient,
    private val name: String = "set:${UUID.randomUUID()}"
) : MutableSet<T> {
    override fun add(element: T): Boolean = runBlocking { sithsClient.sadd(name, element) == 1L }

    override fun addAll(elements: Collection<T>): Boolean {
        TODO("Not yet implemented")
    }

    override fun clear() {
        runBlocking { sithsClient.del(name) }
    }

    override fun iterator(): MutableIterator<T> {
        TODO("Not yet implemented")
    }

    override fun remove(element: T): Boolean {
        TODO("Not yet implemented")
    }

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
        TODO("Not yet implemented")
    }

    override fun isEmpty(): Boolean = size == 0
}