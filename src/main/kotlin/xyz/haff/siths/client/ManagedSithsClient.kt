package xyz.haff.siths.client

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class ManagedSithsClient(
    private val pool: SithsClientPool,
) : SithsClient {
    constructor(
        redisConnection: RedisConnection,
        maxConnections: Int = 10,
        acquireTimeout: Duration = 10.seconds
    ): this(SithsClientPool(SithsConnectionPool(redisConnection, maxConnections, acquireTimeout)))

    constructor(
        connectionPool: SithsConnectionPool
    ): this(SithsClientPool(connectionPool))

    override suspend fun set(key: String, value: Any, exclusiveMode: ExclusiveMode?, timeToLive: Duration?) {
        pool.get().use { it.set(key, value, exclusiveMode, timeToLive) }
    }

    override suspend fun del(key: String, vararg rest: String): Long {
        return pool.get().use { it.del(key, *rest) }
    }

    override suspend fun ttl(key: String): Duration? {
        return pool.get().use { it.ttl(key) }
    }

    override suspend fun getOrNull(key: String): String? {
        return pool.get().use { it.getOrNull(key) }
    }

    override suspend fun get(key: String): String {
        return pool.get().use { it.get(key) }
    }

    override suspend fun scriptLoad(script: String): String {
        return pool.get().use { it.scriptLoad(script) }
    }

    override suspend fun evalSha(sha: String, keys: List<String>, args: List<String>): RespType<*> {
        return pool.get().use { it.evalSha(sha, keys, args) }
    }

    override suspend fun eval(script: String, keys: List<String>, args: List<String>): RespType<*> {
        return pool.get().use { it.eval(script, keys, args) }
    }

    override suspend fun incrBy(key: String, value: Long): Long {
        return pool.get().use { it.incrBy(key, value) }
    }

    override suspend fun exists(key: String, vararg rest: String): Boolean {
        return pool.get().use { it.exists(key, *rest) }
    }

    override suspend fun sadd(key: String, value: Any, vararg rest: Any): Long {
        return pool.get().use { it.sadd(key, value, *rest) }
    }

    override suspend fun smembers(key: String): Set<String> {
        return pool.get().use { it.smembers(key) }
    }

    override suspend fun sismember(key: String, member: Any): Boolean {
        return pool.get().use { it.sismember(key, member) }
    }

    override suspend fun smismember(key: String, member: Any, vararg rest: Any): Map<String, Boolean> {
        return pool.get().use { it.smismember(key, member, *rest) }
    }

    override suspend fun scard(key: String): Long {
        return pool.get().use { it.scard(key) }
    }

    override suspend fun srem(key: String, member: Any, vararg rest: Any): Long {
        return pool.get().use { it.srem(key, member, *rest) }
    }

    override suspend fun sintercard(key: String, vararg rest: String, limit: Int?): Long {
        return pool.get().use { it.sintercard(key, rest = rest, limit = limit) }
    }

    override suspend fun sdiffstore(destination: String, key: String, vararg rest: String): Long {
        return pool.get().use { it.sdiffstore(destination, key, *rest) }
    }

    override suspend fun sdiff(key: String, vararg rest: String): Set<String> {
        return pool.get().use { it.sdiff(key, *rest) }
    }

    override suspend fun sinter(key: String, vararg rest: String): Set<String> {
        return pool.get().use { it.sinter(key, *rest) }
    }

    override suspend fun smove(source: String, destination: String, member: Any): Boolean {
        return pool.get().use { it.smove(source, destination, member) }
    }

    override suspend fun spop(key: String, count: Int?): Set<String> {
        return pool.get().use { it.spop(key, count) }
    }

    override suspend fun srandmember(key: String, count: Int?): Set<String> {
        return pool.get().use { it.srandmember(key, count) }
    }

    override suspend fun sunion(key: String, vararg rest: String): Set<String> {
        return pool.get().use { it.sunion(key, *rest) }
    }

    override suspend fun sunionstore(destination: String, key: String, vararg rest: String): Long {
        return pool.get().use { it.sunionstore(destination, key, *rest) }
    }

    override suspend fun sinterstore(destination: String, key: String, vararg rest: String): Long {
        return pool.get().use { it.sinterstore(destination, key, *rest) }
    }

    override suspend fun sscan(key: String, cursor: Long, match: String?, count: Int?): RedisCursor<String> {
        return pool.get().use { it.sscan(key, cursor, match, count) }
    }

    override suspend fun clientList(): List<RedisClient> {
        return pool.get().use { it.clientList() }
    }

    override suspend fun ping(): Boolean {
        return pool.get().use { it.ping() }
    }

    override suspend fun expire(key: String, duration: Duration, expirationCondition: ExpirationCondition?): Boolean {
        return pool.get().use { it.expire(key, duration, expirationCondition) }
    }
}