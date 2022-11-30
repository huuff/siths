package xyz.haff.siths.client.pooled

import xyz.haff.siths.client.api.SithsImmediateClient
import xyz.haff.siths.common.RedisBrokenConnectionException
import xyz.haff.siths.option.*
import xyz.haff.siths.pooling.PoolStatus
import xyz.haff.siths.protocol.*
import java.time.ZonedDateTime
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Use a PooledClient as usual, but catch a RedisBrokenConnectionException and mark it as broken so that it's not reused
 * anymore
 */
private inline fun <T> PooledSithsClient.useSafely(f: (PooledSithsClient) -> T): T = try {
    this.use { f(this) }
} catch (e: RedisBrokenConnectionException) {
    this.status = PoolStatus.BROKEN
    throw e
}

// I'm not splitting this client into one for lists, sets, etc. because this has no logic whatsoever. But that might change
// in the future
class ManagedSithsClient(
    private val pool: SithsClientPool,
) : SithsImmediateClient {
    constructor(
        redisConnection: RedisConnection,
        maxConnections: Int = 10,
        acquireTimeout: Duration = 10.seconds
    ) : this(SithsClientPool(SithsConnectionPool(redisConnection, maxConnections, acquireTimeout)))

    constructor(
        connectionPool: SithsConnectionPool
    ) : this(SithsClientPool(connectionPool))

    /**
     * XXX: This is the only genuine method of ManagedSithsClient, and all the rest just wrap the methods of the underlying
     * unmanaged client. It runs the given function on the underlying client, and, in the case of errors, marks the client
     * as broken and retries.
     * XXX: Is it safe to always retry for a broken connection?
     */
    private suspend inline fun <T> runSafely(f: (PooledSithsClient) -> T): T {
        return try {
            pool.get().useSafely(f)
        } catch (e: RedisBrokenConnectionException) {
            pool.get().useSafely(f)
        }
    }

    override suspend fun set(
        key: String,
        value: String,
        existenceCondition: ExistenceCondition?,
        timeToLive: Duration?
    ) = pool.get().useSafely { it.set(key, value, existenceCondition, timeToLive) }

    override suspend fun mset(vararg pairs: Pair<String, String>) = pool.get().useSafely { it.mset(*pairs) }

    override suspend fun mget(key: String, vararg rest: String): Map<String, String> =
        pool.get().useSafely { it.mget(key, *rest) }

    override suspend fun del(key: String, vararg rest: String): Long = pool.get().useSafely { it.del(key, *rest) }

    override suspend fun ttl(key: String): Duration? = pool.get().useSafely { it.ttl(key) }

    override suspend fun getOrNull(key: String): String? = pool.get().useSafely { it.getOrNull(key) }

    override suspend fun get(key: String): String = pool.get().useSafely { it.get(key) }

    override suspend fun getLong(key: String): Long = pool.get().useSafely { it.get(key).toLong() }

    override suspend fun scriptLoad(script: String): String = pool.get().useSafely { it.scriptLoad(script) }

    override suspend fun evalSha(sha: String, keys: List<String>, args: List<String>): RespType<*> =
        pool.get().useSafely { it.evalSha(sha, keys, args) }

    override suspend fun eval(script: String, keys: List<String>, args: List<String>): RespType<*> =
        pool.get().useSafely { it.eval(script, keys, args) }

    override suspend fun incrBy(key: String, value: Long): Long = pool.get().useSafely { it.incrBy(key, value) }

    override suspend fun incrByFloat(key: String, value: Double): Double =
        pool.get().useSafely { it.incrByFloat(key, value) }

    override suspend fun exists(key: String, vararg rest: String): Boolean =
        pool.get().useSafely { it.exists(key, *rest) }

    override suspend fun clientList(): List<RedisClient> = pool.get().useSafely { it.clientList() }

    override suspend fun ping(): Boolean = pool.get().useSafely { it.ping() }

    override suspend fun expire(key: String, duration: Duration, expirationCondition: ExpirationCondition?): Boolean =
        pool.get().useSafely { it.expire(key, duration, expirationCondition) }

    override suspend fun persist(key: String): Boolean = pool.get().useSafely { it.persist(key) }

    override suspend fun expireAt(
        key: String,
        time: ZonedDateTime,
        expirationCondition: ExpirationCondition?
    ): Boolean = pool.get().useSafely { it.expireAt(key, time, expirationCondition) }

    override suspend fun expireTime(key: String): ZonedDateTime? = pool.get().useSafely { it.expireTime(key) }

    override suspend fun dbSize(): Long = pool.get().useSafely { it.dbSize() }
    override suspend fun flushDb(mode: SyncMode?) = pool.get().useSafely { it.flushDb(mode) }

    // SET OPERATIONS

    override suspend fun sadd(key: String, value: String, vararg rest: String): Long =
        pool.get().useSafely { it.sadd(key, value, *rest) }

    override suspend fun saddAny(key: String, value: Any, vararg rest: Any): Long =
        pool.get().useSafely { it.saddAny(key, value, rest) }

    override suspend fun smembers(key: String): Set<String> = pool.get().useSafely { it.smembers(key) }

    override suspend fun sisMember(key: String, member: String): Boolean =
        pool.get().useSafely { it.sisMember(key, member) }

    override suspend fun smisMember(key: String, member: String, vararg rest: String): Map<String, Boolean> =
        pool.get().useSafely { it.smisMember(key, member, *rest) }

    override suspend fun scard(key: String): Long = pool.get().useSafely { it.scard(key) }

    override suspend fun srem(key: String, member: String, vararg rest: String): Long =
        pool.get().useSafely { it.srem(key, member, *rest) }

    override suspend fun sinterCard(key: String, vararg rest: String, limit: Int?): Long =
        pool.get().useSafely { it.sinterCard(key, rest = rest, limit = limit) }

    override suspend fun sdiffStore(destination: String, key: String, vararg rest: String): Long =
        pool.get().useSafely { it.sdiffStore(destination, key, *rest) }

    override suspend fun sdiff(key: String, vararg rest: String): Set<String> =
        pool.get().useSafely { it.sdiff(key, *rest) }

    override suspend fun sinter(key: String, vararg rest: String): Set<String> =
        pool.get().useSafely { it.sinter(key, *rest) }

    override suspend fun smove(source: String, destination: String, member: String): Boolean =
        pool.get().useSafely { it.smove(source, destination, member) }

    override suspend fun spop(key: String): String? = pool.get().useSafely { it.spop(key) }

    override suspend fun spop(key: String, count: Int?): Set<String> = pool.get().useSafely { it.spop(key, count) }

    override suspend fun srandMember(key: String, count: Int?): Set<String> =
        pool.get().useSafely { it.srandMember(key, count) }

    override suspend fun srandMember(key: String): String? = pool.get().useSafely { it.srandMember(key) }

    override suspend fun sunion(key: String, vararg rest: String): Set<String> =
        pool.get().useSafely { it.sunion(key, *rest) }

    override suspend fun sunionStore(destination: String, key: String, vararg rest: String): Long =
        pool.get().useSafely { it.sunionStore(destination, key, *rest) }

    override suspend fun sinterStore(destination: String, key: String, vararg rest: String): Long =
        pool.get().useSafely { it.sinterStore(destination, key, *rest) }

    override suspend fun sscan(key: String, cursor: Long, match: String?, count: Int?): RedisCursor<String> =
        pool.get().useSafely { it.sscan(key, cursor, match, count) }

    // LIST OPERATIONS
    override suspend fun llen(key: String): Long = pool.get().useSafely { it.llen(key) }

    override suspend fun lindex(key: String, index: Int): String? = pool.get().useSafely { it.lindex(key, index) }

    override suspend fun linsert(
        key: String,
        relativePosition: RelativePosition,
        pivot: String,
        element: String
    ): Long? = pool.get().useSafely { it.linsert(key, relativePosition, pivot, element) }

    override suspend fun lpop(key: String, count: Int?): List<String> = pool.get().useSafely { it.lpop(key, count) }

    override suspend fun lpop(key: String): String? = pool.get().useSafely { it.lpop(key) }

    override suspend fun lmpop(
        keys: List<String>,
        end: ListEnd,
        count: Int?
    ): SourceAndData<List<String>>? = pool.get().useSafely { it.lmpop(keys, end, count) }

    override suspend fun blmpop(
        timeout: Duration,
        key: String,
        vararg otherKeys: String,
        end: ListEnd,
        count: Int?
    ): SourceAndData<List<String>>? =
        pool.get().useSafely { it.blmpop(timeout, key, otherKeys = otherKeys, end, count) }

    override suspend fun brpop(key: String, vararg otherKeys: String, timeout: Duration?): SourceAndData<String>? =
        pool.get().useSafely { it.brpop(key, otherKeys = otherKeys, timeout) }

    override suspend fun blpop(key: String, vararg otherKeys: String, timeout: Duration?): SourceAndData<String>? =
        pool.get().useSafely { it.blpop(key, otherKeys = otherKeys, timeout) }

    override suspend fun lmpop(key: String, end: ListEnd, count: Int?): List<String> =
        pool.get().useSafely { it.lmpop(key, end, count) }

    override suspend fun rpop(key: String, count: Int?): List<String> = pool.get().useSafely { it.rpop(key, count) }

    override suspend fun rpop(key: String): String? = pool.get().useSafely { it.rpop(key) }

    override suspend fun lpush(key: String, element: String, vararg rest: String): Long =
        pool.get().useSafely { it.lpush(key, element, *rest) }

    override suspend fun lpushx(key: String, element: String, vararg rest: String): Long =
        pool.get().useSafely { it.lpushx(key, element, *rest) }

    override suspend fun rpush(key: String, element: String, vararg rest: String): Long =
        pool.get().useSafely { it.rpush(key, element, *rest) }

    override suspend fun rpushx(key: String, element: String, vararg rest: String): Long =
        pool.get().useSafely { it.rpushx(key, element, *rest) }

    override suspend fun lrem(key: String, element: String, count: Int): Long =
        pool.get().useSafely { it.lrem(key, element, count) }

    override suspend fun lrange(key: String, start: Int, stop: Int): List<String> =
        pool.get().useSafely { it.lrange(key, start, stop) }

    override suspend fun lpos(key: String, element: String, rank: Int?, maxlen: Int?): Long? =
        pool.get().useSafely { it.lpos(key, element, rank, maxlen) }

    override suspend fun lpos(key: String, element: String, rank: Int?, count: Int, maxlen: Int?): List<Long> =
        pool.get().useSafely { it.lpos(key, element, rank, count, maxlen) }

    override suspend fun lset(key: String, index: Int, element: String): Boolean =
        pool.get().useSafely { it.lset(key, index, element) }

    override suspend fun ltrim(key: String, start: Int, stop: Int) = pool.get().useSafely { it.ltrim(key, start, stop) }

    override suspend fun lmove(
        source: String,
        destination: String,
        sourceEnd: ListEnd,
        destinationEnd: ListEnd
    ): String = pool.get().useSafely { it.lmove(source, destination, sourceEnd, destinationEnd) }

    override suspend fun blmove(
        source: String,
        destination: String,
        sourceEnd: ListEnd,
        destinationEnd: ListEnd,
        timeout: Duration?
    ): String? = pool.get().useSafely { it.blmove(source, destination, sourceEnd, destinationEnd) }

    override suspend fun lpushAny(key: String, element: Any, vararg rest: Any): Long =
        pool.get().useSafely { it.lpushAny(key, element, rest) }

    override suspend fun rpushAny(key: String, element: Any, vararg rest: Any): Long =
        pool.get().useSafely { it.rpushAny(key, element, rest) }

    // HASH OPERATIONS
    override suspend fun hget(key: String, field: String): String = pool.get().useSafely { it.hget(key, field) }

    override suspend fun hset(key: String, pair: Pair<String, String>, vararg rest: Pair<String, String>): Long =
        pool.get().useSafely { it.hset(key, pair, *rest) }

    override suspend fun hsetAny(key: String, pair: Pair<String, Any>, vararg rest: Pair<String, Any>): Long =
        pool.get().useSafely { it.hsetAny(key, pair, *rest) }

    override suspend fun hgetOrNull(key: String, field: String): String? =
        pool.get().useSafely { it.hgetOrNull(key, field) }

    override suspend fun hgetAll(key: String): Map<String, String> = pool.get().useSafely { it.hgetAll(key) }

    override suspend fun hkeys(key: String): List<String> = pool.get().useSafely { it.hkeys(key) }

    override suspend fun hvals(key: String): List<String> = pool.get().useSafely { it.hvals(key) }

    override suspend fun hexists(key: String, field: String): Boolean = pool.get().useSafely { it.hexists(key, field) }

    override suspend fun hincrBy(key: String, field: String, increment: Long): Long =
        pool.get().useSafely { it.hincrBy(key, field, increment) }

    override suspend fun hincrByFloat(key: String, field: String, increment: Double): Double =
        pool.get().useSafely { it.hincrByFloat(key, field, increment) }

    override suspend fun hmget(key: String, field: String, vararg rest: String): Map<String, String> =
        pool.get().useSafely { it.hmget(key, field, *rest) }

    override suspend fun hlen(key: String): Long = pool.get().useSafely { it.hlen(key) }

    override suspend fun hdel(key: String, field: String, vararg rest: String): Long =
        pool.get().useSafely { it.hdel(key, field, *rest) }

    override suspend fun hstrLen(key: String, field: String): Long = pool.get().useSafely { it.hstrLen(key, field) }

    override suspend fun hsetnx(key: String, field: String, value: String): Boolean =
        pool.get().useSafely { it.hsetnx(key, field, value) }

    override suspend fun hscan(
        key: String,
        cursor: Long,
        match: String?,
        count: Int?
    ): RedisCursor<Pair<String, String>> = pool.get().useSafely { it.hscan(key, cursor, match, count) }

    // HRANDFIELD
    override suspend fun hrandField(key: String): String? = pool.get().useSafely { it.hrandField(key) }

    override suspend fun hrandField(key: String, count: Int): List<String> =
        pool.get().useSafely { it.hrandField(key, count) }

    override suspend fun hrandFieldWithValues(key: String, count: Int): Map<String, String> =
        pool.get().useSafely { it.hrandFieldWithValues(key, count) }


    // ZSET
    override suspend fun zadd(
        key: String,
        scoreAndMember: Pair<Double, String>,
        vararg rest: Pair<Double, String>,
        existenceCondition: ExistenceCondition?,
        comparisonCondition: ComparisonCondition?,
        returnChanged: Boolean
    ): Long = pool.get().useSafely {
            it.zadd(
                key,
                scoreAndMember,
                *rest,
                existenceCondition = existenceCondition,
                comparisonCondition = comparisonCondition,
                returnChanged = returnChanged,
            )
        }

    override suspend fun zadd(
        key: String,
        scoreAndMembers: Collection<Pair<Double, String>>,
        existenceCondition: ExistenceCondition?,
        comparisonCondition: ComparisonCondition?,
        returnChanged: Boolean
    ): Long = pool.get().useSafely { it.zadd(key, scoreAndMembers, existenceCondition, comparisonCondition, returnChanged) }
    override suspend fun zrangeByRank(
        key: String,
        start: Int,
        stop: Int,
        reverse: Boolean,
        limit: Limit?
    ): Set<String> = pool.get().useSafely { it.zrangeByRank(key, start, stop, reverse, limit) }

    override suspend fun zrangeByRankWithScores(
        key: String,
        start: Int,
        stop: Int,
        reverse: Boolean,
        limit: Limit?
    ): List<Pair<String, Double>> = pool.get().useSafely { it.zrangeByRankWithScores(key, start, stop, reverse, limit) }

    override suspend fun zrangeByScore(
        key: String,
        start: Double,
        stop: Double,
        reverse: Boolean,
        limit: Limit?
    ): Set<String> = pool.get().useSafely { it.zrangeByScore(key, start, stop, reverse, limit) }

    override suspend fun zrangeByScoreWithScores(
        key: String,
        start: Double,
        stop: Double,
        reverse: Boolean,
        limit: Limit?
    ): List<Pair<String, Double>>  = pool.get().useSafely { it.zrangeByScoreWithScores(key, start, stop, reverse, limit) }
}