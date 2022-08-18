package xyz.haff.siths.client

import kotlin.time.Duration

// TODO: Improve it so it doesn't create a new StandaloneSiths every time!
class PooledSithsClient(
    private val pool: SithsPool
): SithsClient {
    override suspend fun set(key: String, value: Any, exclusiveMode: ExclusiveMode?, timeToLive: Duration?) {
        pool.getConnection().use { conn -> StandaloneSithsClient(conn).set(key, value, exclusiveMode, timeToLive) }
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

    override suspend fun clientList(): List<RedisClient> {
        return pool.getConnection().use { conn -> StandaloneSithsClient(conn).clientList() }
    }
}