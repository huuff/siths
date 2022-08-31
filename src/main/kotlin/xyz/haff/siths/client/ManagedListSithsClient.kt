package xyz.haff.siths.client

import xyz.haff.siths.option.ListEnd
import xyz.haff.siths.option.RelativePosition
import kotlin.time.Duration

class ManagedListSithsClient(
    private val pool: SithsClientPool,
): ListSithsClient {

    override suspend fun llen(key: String): Long
            = pool.get().use { it.llen(key) }

    override suspend fun lindex(key: String, index: Int): String?
            = pool.get().use { it.lindex(key, index) }

    override suspend fun linsert(key: String, relativePosition: RelativePosition, pivot: String, element: String): Long?
            = pool.get().use { it.linsert(key, relativePosition, pivot, element) }

    override suspend fun lpop(key: String, count: Int?): List<String>
            = pool.get().use { it.lpop(key, count) }

    override suspend fun lpop(key: String): String?
            = pool.get().use { it.lpop(key) }

    override suspend fun lmpop(
        keys: List<String>,
        end: ListEnd,
        count: Int?
    ): SourceAndData<List<String>>?
            = pool.get().use { it.lmpop(keys, end, count) }

    override suspend fun blmpop(keys: List<String>, end: ListEnd, count: Int?): SourceAndData<List<String>>?
            = pool.get().use { it.blmpop(keys, end, count) }

    override suspend fun blmpop(key: String, end: ListEnd, count: Int?): List<String>
            = pool.get().use { it.blmpop(key, end, count) }

    override suspend fun lmpop(key: String, end: ListEnd, count: Int?): List<String>
            = pool.get().use { it.lmpop(key, end, count)}

    override suspend fun rpop(key: String, count: Int?): List<String>
            = pool.get().use { it.rpop(key, count) }

    override suspend fun rpop(key: String): String?
            = pool.get().use { it.rpop(key) }

    override suspend fun lpush(key: String, element: String, vararg rest: String): Long
            = pool.get().use { it.lpush(key, element, *rest) }

    override suspend fun lpushx(key: String, element: String, vararg rest: String): Long
            = pool.get().use { it.lpushx(key, element, *rest) }

    override suspend fun rpush(key: String, element: String, vararg rest: String): Long
            = pool.get().use { it.rpush(key, element, *rest)}

    override suspend fun rpushx(key: String, element: String, vararg rest: String): Long
            = pool.get().use { it.rpushx(key, element, *rest) }

    override suspend fun lrem(key: String, element: String, count: Int): Long
            = pool.get().use { it.lrem(key, element, count) }

    override suspend fun lrange(key: String, start: Int, stop: Int): List<String>
            = pool.get().use { it.lrange(key, start, stop) }

    override suspend fun lpos(key: String, element: String, rank: Int?, maxlen: Int?): Long?
            = pool.get().use { it.lpos(key, element, rank, maxlen) }

    override suspend fun lpos(key: String, element: String, rank: Int?, count: Int, maxlen: Int?): List<Long>
            = pool.get().use { it.lpos(key, element, rank, count, maxlen) }

    override suspend fun lset(key: String, index: Int, element: String): Boolean
            = pool.get().use { it.lset(key, index, element) }

    override suspend fun ltrim(key: String, start: Int, stop: Int)
            = pool.get().use { it.ltrim(key, start, stop) }

    override suspend fun lmove(
        source: String,
        destination: String,
        sourceEnd: ListEnd,
        destinationEnd: ListEnd
    ): String = pool.get().use { it.lmove(source, destination, sourceEnd, destinationEnd) }

    override suspend fun brpop(keys: List<String>, timeout: Duration?): SourceAndData<String>?
            = pool.get().use { it.brpop(keys, timeout) }

    override suspend fun brpop(key: String, timeout: Duration?): String?
            = pool.get().use { it.brpop(key, timeout)}

    override suspend fun blpop(keys: List<String>, timeout: Duration?): SourceAndData<String>?
            = pool.get().use { it.blpop(keys, timeout) }

    override suspend fun blpop(key: String, timeout: Duration?): String?
            = pool.get().use { it.blpop(key, timeout) }
}