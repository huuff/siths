package xyz.haff.siths.client

import kotlin.time.Duration

// TODO: Improve it so it doesn't create a new StandaloneSiths every time!
class PooledSithsClient(
    private val pool: SithsPool
): SithsClient {
    override suspend fun set(key: String, value: Any, exclusiveMode: ExclusiveMode?, timeToLive: Duration?) {
        pool.getConnection().use { conn -> StandaloneSithsClient(conn).set(key, value, exclusiveMode, timeToLive) }
    }

    override suspend fun del(key: String, vararg rest: String): Long {
        return pool.getConnection().use { conn -> StandaloneSithsClient(conn).del(key, *rest)}
    }

    override suspend fun ttl(key: String): Duration? {
        return pool.getConnection().use { conn -> StandaloneSithsClient(conn).ttl(key) }
    }

    override suspend fun getOrNull(key: String): String? {
        return pool.getConnection().use { conn -> StandaloneSithsClient(conn).getOrNull(key) }
    }

    override suspend fun get(key: String): String {
        return pool.getConnection().use { conn -> StandaloneSithsClient(conn).get(key) }
    }

    override suspend fun scriptLoad(script: String): String {
        return pool.getConnection().use { conn -> StandaloneSithsClient(conn).scriptLoad(script) }
    }

    override suspend fun evalSha(sha: String, keys: List<String>, args: List<String>): RespType<*> {
        return pool.getConnection().use { conn -> StandaloneSithsClient(conn).evalSha(sha, keys, args) }
    }

    override suspend fun eval(script: String, keys: List<String>, args: List<String>): RespType<*> {
        return pool.getConnection().use { conn -> StandaloneSithsClient(conn).eval(script, keys, args)}
    }

    override suspend fun incrBy(key: String, value: Long): Long {
        return pool.getConnection().use { conn -> StandaloneSithsClient(conn).incrBy(key, value) }
    }

    override suspend fun exists(key: String, vararg rest: String): Boolean {
        return pool.getConnection().use { conn -> StandaloneSithsClient(conn).exists(key, *rest) }
    }

    override suspend fun sadd(key: String, value: Any, vararg rest: Any): Long {
        return pool.getConnection().use { conn -> StandaloneSithsClient(conn).sadd(key, value, *rest) }
    }

    override suspend fun smembers(key: String): Set<String> {
        return pool.getConnection().use { conn -> StandaloneSithsClient(conn).smembers(key) }
    }

    override suspend fun sismember(key: String, member: Any): Boolean {
        return pool.getConnection().use { conn -> StandaloneSithsClient(conn).sismember(key, member) }
    }

    override suspend fun scard(key: String): Long {
        return pool.getConnection().use { conn -> StandaloneSithsClient(conn).scard(key) }
    }

    override suspend fun srem(key: String, member: Any, vararg rest: Any): Long {
        return pool.getConnection().use { conn -> StandaloneSithsClient(conn).srem(key, member, *rest) }
    }

    override suspend fun sintercard(key: String, vararg rest: String, limit: Int?): Long {
        return pool.getConnection().use { conn -> StandaloneSithsClient(conn).sintercard(key, rest = rest, limit = limit)}
    }

    override suspend fun sdiffstore(destination: String, key: String, vararg rest: String): Long {
        return pool.getConnection().use { conn -> StandaloneSithsClient(conn).sdiffstore(destination, key, *rest) }
    }

    override suspend fun sinterstore(destination: String, key: String, vararg rest: String): Long {
        return pool.getConnection().use { conn -> StandaloneSithsClient(conn).sinterstore(destination, key, *rest) }
    }

    override suspend fun sscan(key: String, cursor: Long, match: String?, count: Int?): RedisCursor<Set<String>> {
        return pool.getConnection().use { conn -> StandaloneSithsClient(conn).sscan(key, cursor, match, count) }
    }

    override suspend fun clientList(): List<RedisClient> {
        return pool.getConnection().use { conn -> StandaloneSithsClient(conn).clientList() }
    }

    override suspend fun ping(): Boolean {
        return pool.getConnection().use { conn -> StandaloneSithsClient(conn).ping() }
    }
}