package xyz.haff.siths.client

import xyz.haff.siths.option.ExclusiveMode
import xyz.haff.siths.option.ExpirationCondition
import xyz.haff.siths.option.ListEnd
import xyz.haff.siths.option.RelativePosition
import xyz.haff.siths.protocol.RedisConnection
import xyz.haff.siths.protocol.RedisCursor
import xyz.haff.siths.protocol.RespType
import xyz.haff.siths.protocol.SithsConnectionPool
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class ManagedSithsClient(
    private val pool: SithsClientPool,
) : SithsClient, ListSithsClient by ManagedListSithsClient(pool) {
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

    override suspend fun del(key: String, vararg rest: String): Long
        = pool.get().use { it.del(key, *rest) }

    override suspend fun ttl(key: String): Duration?
        = pool.get().use { it.ttl(key) }

    override suspend fun getOrNull(key: String): String?
        = pool.get().use { it.getOrNull(key) }

    override suspend fun get(key: String): String
        = pool.get().use { it.get(key) }

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

    // SET OPERATIONS

    override suspend fun sadd(key: String, value: String, vararg rest: String): Long
        = pool.get().use { it.sadd(key, value, *rest) }

    override suspend fun smembers(key: String): Set<String>
        = pool.get().use { it.smembers(key) }

    override suspend fun sismember(key: String, member: String): Boolean
        = pool.get().use { it.sismember(key, member) }

    override suspend fun smismember(key: String, member: String, vararg rest: String): Map<String, Boolean>
        = pool.get().use { it.smismember(key, member, *rest) }

    override suspend fun scard(key: String): Long
        = pool.get().use { it.scard(key) }

    override suspend fun srem(key: String, member: String, vararg rest: String): Long
        = pool.get().use { it.srem(key, member, *rest) }

    override suspend fun sintercard(key: String, vararg rest: String, limit: Int?): Long
        = pool.get().use { it.sintercard(key, rest = rest, limit = limit) }

    override suspend fun sdiffstore(destination: String, key: String, vararg rest: String): Long
        = pool.get().use { it.sdiffstore(destination, key, *rest) }

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

    override suspend fun srandmember(key: String, count: Int?): Set<String>
        = pool.get().use { it.srandmember(key, count) }

    override suspend fun srandmember(key: String): String?
        = pool.get().use { it.srandmember(key) }

    override suspend fun sunion(key: String, vararg rest: String): Set<String>
        = pool.get().use { it.sunion(key, *rest) }

    override suspend fun sunionstore(destination: String, key: String, vararg rest: String): Long
        = pool.get().use { it.sunionstore(destination, key, *rest) }

    override suspend fun sinterstore(destination: String, key: String, vararg rest: String): Long
        = pool.get().use { it.sinterstore(destination, key, *rest) }

    override suspend fun sscan(key: String, cursor: Long, match: String?, count: Int?): RedisCursor<String>
        = pool.get().use { it.sscan(key, cursor, match, count) }

    override suspend fun clientList(): List<RedisClient>
        = pool.get().use { it.clientList() }

    override suspend fun ping(): Boolean
        = pool.get().use { it.ping() }

    override suspend fun expire(key: String, duration: Duration, expirationCondition: ExpirationCondition?): Boolean
        = pool.get().use { it.expire(key, duration, expirationCondition) }

    override suspend fun persist(key: String): Boolean
        = pool.get().use { it.persist(key) }
}