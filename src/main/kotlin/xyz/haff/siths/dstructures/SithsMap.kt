package xyz.haff.siths.dstructures

import kotlinx.coroutines.runBlocking
import xyz.haff.siths.client.SithsDSL
import xyz.haff.siths.common.headAndTail
import xyz.haff.siths.common.randomUUID
import xyz.haff.siths.protocol.SithsConnectionPool

// XXX: This is a much finer implementation that the Set's and List's ones because it uses SithsDSL
class SithsMap<K, V>(
    connectionPool: SithsConnectionPool,
    val name: String = "map:${randomUUID()}",
    private val serializeKey: (K) -> String,
    private val deserializeKey: (String) -> K,
    private val serializeValue: (V) -> String,
    private val deserializeValue: (String) -> V,
) : MutableMap<K, V> {
    companion object {
        fun ofStrings(pool: SithsConnectionPool, name: String = "map:${randomUUID()}") = SithsMap<String, String>(
            connectionPool = pool,
            name = name,
            serializeKey = { it },
            deserializeKey = { it },
            serializeValue = { it },
            deserializeValue = { it },
        )
    }

    private val client = SithsDSL(connectionPool)

    override val size: Int get() = runBlocking { client.hlen(name).toInt() }

    override fun containsKey(key: K): Boolean = runBlocking { client.hexists(name, serializeKey(key)) }

    override fun containsValue(value: V): Boolean = runBlocking { serializeValue(value) in client.hvals(name) }

    override fun get(key: K): V? = runBlocking { client.hgetOrNull(name, serializeKey(key))?.let(deserializeValue) }

    override fun isEmpty(): Boolean = this.size == 1

    override val entries: MutableSet<MutableMap.MutableEntry<K, V>>
        get() = runBlocking {
            client.hgetAll(name).entries.map { (key, value) ->
                object : MutableMap.MutableEntry<K, V> {
                    override val key: K = deserializeKey(key)
                    override val value: V = deserializeValue(value)
                    override fun setValue(newValue: V): V = this.value.also {
                        runBlocking { client.hset(name, key to serializeValue(newValue)) }
                    }
                }
            }.toMutableSet()
        }

    override val keys: MutableSet<K> get() = runBlocking { client.hkeys(name).map(deserializeKey).toMutableSet() }

    override val values: MutableCollection<V>
        get() = runBlocking {
            client.hvals(name).map(deserializeValue).toMutableSet()
        }

    override fun clear() {
        runBlocking { client.del(name) }
    }

    override fun put(key: K, value: V): V? = runBlocking {
        client.transactional {
            val serializedKey = serializeKey(key)
            val previousValue = hgetOrNull(name, serializedKey).map { it?.let(deserializeValue) }
            hset(name, serializedKey to serializeValue(value))

            previousValue
        }
    }

    override fun putAll(from: Map<out K, V>) {
        val (head, tail) = from.map { (key, value) -> serializeKey(key) to serializeValue(value) }.toTypedArray()
            .headAndTail()

        runBlocking { client.hset(name, head, *tail) }
    }

    override fun remove(key: K): V? = runBlocking {
        client.transactional {
            val serializedKey = serializeKey(key)
            val previousValue = hgetOrNull(name, serializedKey).map { it?.let(deserializeValue) }
            hdel(name, serializedKey)

            previousValue
        }
    }
}