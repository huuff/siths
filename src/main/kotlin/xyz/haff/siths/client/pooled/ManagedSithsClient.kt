package xyz.haff.siths.client.pooled

import xyz.haff.siths.client.api.SithsImmediateClient
import xyz.haff.siths.option.ExclusiveMode
import xyz.haff.siths.option.ExpirationCondition
import xyz.haff.siths.option.ListEnd
import xyz.haff.siths.option.RelativePosition
import xyz.haff.siths.protocol.*
import java.time.ZonedDateTime
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

// I'm not splitting this client into one for lists, sets, etc. because this has no logic whatsoever. But that might change
// in the future
class ManagedSithsClient(
    private val pool: SithsClientPool,
) : SithsImmediateClient {
    constructor(
        redisConnection: RedisConnection,
        maxConnections: Int = 10,
        acquireTimeout: Duration = 10.seconds
    ): this(SithsClientPool(SithsConnectionPool(redisConnection, maxConnections, acquireTimeout)))

    constructor(
        connectionPool: SithsConnectionPool
    ): this(SithsClientPool(connectionPool))

    override suspend fun set(key: String, value: String, exclusiveMode: ExclusiveMode?, timeToLive: Duration?)
        = pool.get().use { it.set(key, value, exclusiveMode, timeToLive) }

    override suspend fun mset(vararg pairs: Pair<String, String>)
        = pool.get().use { it.mset(*pairs) }

    override suspend fun mget(key: String, vararg rest: String): Map<String, String>
        = pool.get().use { it.mget(key, *rest) }

    override suspend fun del(key: String, vararg rest: String): Long
        = pool.get().use { it.del(key, *rest) }

    override suspend fun ttl(key: String): Duration?
        = pool.get().use { it.ttl(key) }

    override suspend fun getOrNull(key: String): String?
        = pool.get().use { it.getOrNull(key) }

    override suspend fun get(key: String): String
        = pool.get().use { it.get(key) }

    override suspend fun getLong(key: String): Long = pool.get().use { it.get(key).toLong() }

    override suspend fun scriptLoad(script: String): String
        = pool.get().use { it.scriptLoad(script) }

    override suspend fun evalSha(sha: String, keys: List<String>, args: List<String>): RespType<*>
        = pool.get().use { it.evalSha(sha, keys, args) }

    override suspend fun eval(script: String, keys: List<String>, args: List<String>): RespType<*>
        = pool.get().use { it.eval(script, keys, args) }

    override suspend fun incrBy(key: String, value: Long): Long
        = pool.get().use { it.incrBy(key, value) }

    override suspend fun incrByFloat(key: String, value: Double): Double
        = pool.get().use { it.incrByFloat(key, value) }

    override suspend fun exists(key: String, vararg rest: String): Boolean
        = pool.get().use { it.exists(key, *rest) }

    override suspend fun clientList(): List<RedisClient>
            = pool.get().use { it.clientList() }

    override suspend fun ping(): Boolean
            = pool.get().use { it.ping() }

    override suspend fun expire(key: String, duration: Duration, expirationCondition: ExpirationCondition?): Boolean
            = pool.get().use { it.expire(key, duration, expirationCondition) }

    override suspend fun persist(key: String): Boolean
            = pool.get().use { it.persist(key) }

    override suspend fun expireAt(
        key: String,
        time: ZonedDateTime,
        expirationCondition: ExpirationCondition?
    ): Boolean = pool.get().use { it.expireAt(key, time, expirationCondition) }

    override suspend fun expireTime(key: String): ZonedDateTime?
        = pool.get().use { it.expireTime(key) }

    // SET OPERATIONS

    override suspend fun sadd(key: String, value: String, vararg rest: String): Long
        = pool.get().use { it.sadd(key, value, *rest) }

    override suspend fun saddAny(key: String, value: Any, vararg rest: Any): Long
        = pool.get().use { it.saddAny(key, value, rest) }

    override suspend fun smembers(key: String): Set<String>
        = pool.get().use { it.smembers(key) }

    override suspend fun sisMember(key: String, member: String): Boolean
        = pool.get().use { it.sisMember(key, member) }

    override suspend fun smisMember(key: String, member: String, vararg rest: String): Map<String, Boolean>
        = pool.get().use { it.smisMember(key, member, *rest) }

    override suspend fun scard(key: String): Long
        = pool.get().use { it.scard(key) }

    override suspend fun srem(key: String, member: String, vararg rest: String): Long
        = pool.get().use { it.srem(key, member, *rest) }

    override suspend fun sinterCard(key: String, vararg rest: String, limit: Int?): Long
        = pool.get().use { it.sinterCard(key, rest = rest, limit = limit) }

    override suspend fun sdiffStore(destination: String, key: String, vararg rest: String): Long
        = pool.get().use { it.sdiffStore(destination, key, *rest) }

    override suspend fun sdiff(key: String, vararg rest: String): Set<String>
        = pool.get().use { it.sdiff(key, *rest) }

    override suspend fun sinter(key: String, vararg rest: String): Set<String>
        = pool.get().use { it.sinter(key, *rest) }

    override suspend fun smove(source: String, destination: String, member: String): Boolean
        = pool.get().use { it.smove(source, destination, member) }

    override suspend fun spop(key: String): String?
        = pool.get().use { it.spop(key) }

    override suspend fun spop(key: String, count: Int?): Set<String>
        = pool.get().use { it.spop(key, count) }

    override suspend fun srandMember(key: String, count: Int?): Set<String>
        = pool.get().use { it.srandMember(key, count) }

    override suspend fun srandMember(key: String): String?
        = pool.get().use { it.srandMember(key) }

    override suspend fun sunion(key: String, vararg rest: String): Set<String>
        = pool.get().use { it.sunion(key, *rest) }

    override suspend fun sunionStore(destination: String, key: String, vararg rest: String): Long
        = pool.get().use { it.sunionStore(destination, key, *rest) }

    override suspend fun sinterStore(destination: String, key: String, vararg rest: String): Long
        = pool.get().use { it.sinterStore(destination, key, *rest) }

    override suspend fun sscan(key: String, cursor: Long, match: String?, count: Int?): RedisCursor<String>
        = pool.get().use { it.sscan(key, cursor, match, count) }

    // LIST OPERATIONS
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

    override suspend fun blmpop(
        timeout: Duration,
        key: String,
        vararg otherKeys: String,
        end: ListEnd,
        count: Int?
    ): SourceAndData<List<String>>?
        = pool.get().use { it.blmpop(timeout, key, otherKeys = otherKeys, end, count)}

    override suspend fun brpop(key: String, vararg otherKeys: String, timeout: Duration?): SourceAndData<String>?
        = pool.get().use { it.brpop(key, otherKeys = otherKeys, timeout) }

    override suspend fun blpop(key: String, vararg otherKeys: String, timeout: Duration?): SourceAndData<String>?
        = pool.get().use { it.blpop(key, otherKeys = otherKeys, timeout) }

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

    override suspend fun blmove(
        source: String,
        destination: String,
        sourceEnd: ListEnd,
        destinationEnd: ListEnd,
        timeout: Duration?
    ): String? = pool.get().use { it.blmove(source, destination, sourceEnd, destinationEnd) }

    override suspend fun lpushAny(key: String, element: Any, vararg rest: Any): Long
        = pool.get().use { it.lpushAny(key, element, rest) }

    override suspend fun rpushAny(key: String, element: Any, vararg rest: Any): Long
        = pool.get().use { it.rpushAny(key, element, rest) }

    // HASH OPERATIONS
    override suspend fun hget(key: String, field: String): String
        = pool.get().use { it.hget(key, field) }

    override suspend fun hset(key: String, pair: Pair<String, String>, vararg rest: Pair<String, String>): Long
        = pool.get().use { it.hset(key, pair, *rest) }

    override suspend fun hsetAny(key: String, pair: Pair<String, Any>, vararg rest: Pair<String, Any>): Long
        = pool.get().use { it.hsetAny(key, pair, *rest) }

    override suspend fun hgetOrNull(key: String, field: String): String?
        = pool.get().use { it.hgetOrNull(key, field) }

    override suspend fun hgetAll(key: String): Map<String, String>
        = pool.get().use { it.hgetAll(key) }

    override suspend fun hkeys(key: String): List<String>
        = pool.get().use { it.hkeys(key) }

    override suspend fun hvals(key: String): List<String>
        = pool.get().use { it.hvals(key) }

    override suspend fun hexists(key: String, field: String): Boolean
        = pool.get().use { it.hexists(key, field) }

    override suspend fun hincrBy(key: String, field: String, increment: Long): Long
        = pool.get().use { it.hincrBy(key, field, increment) }

    override suspend fun hincrByFloat(key: String, field: String, increment: Double): Double
        = pool.get().use { it.hincrByFloat(key, field, increment) }

    override suspend fun hmget(key: String, field: String, vararg rest: String): Map<String, String>
        = pool.get().use { it.hmget(key, field, *rest) }

    override suspend fun hlen(key: String): Long = pool.get().use { it.hlen(key) }

    override suspend fun hdel(key: String, field: String, vararg rest: String): Long
        = pool.get().use { it.hdel(key, field, *rest) }

    override suspend fun hstrLen(key: String, field: String): Long
        = pool.get().use { it.hstrLen(key, field) }

    override suspend fun hsetnx(key: String, field: String, value: String): Boolean
        = pool.get().use { it.hsetnx(key, field, value) }

    override suspend fun hscan(
        key: String,
        cursor: Long,
        match: String?,
        count: Int?
    ): RedisCursor<Pair<String, String>>
        = pool.get().use { it.hscan(key, cursor, match, count) }

    // HRANDFIELD
    override suspend fun hrandField(key: String): String?
        = pool.get().use { it.hrandField(key) }

    override suspend fun hrandField(key: String, count: Int): List<String>
        = pool.get().use { it.hrandField(key, count) }

    override suspend fun hrandFieldWithValues(key: String, count: Int): Map<String, String>
        = pool.get().use { it.hrandFieldWithValues(key, count) }
}