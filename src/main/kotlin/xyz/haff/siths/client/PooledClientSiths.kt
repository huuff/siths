package xyz.haff.siths.client

import kotlin.time.Duration

// TODO: Improve it so it doesn't create a new StandaloneSiths every time!
class PooledClientSiths(
    private val pool: SithsPool
): SithsClient {
    override suspend fun set(key: String, value: Any, exclusiveMode: ExclusiveMode?, timeToLive: Duration?) {
        pool.getConnection().use { conn -> StandaloneClientSiths(conn).set(key, value, exclusiveMode, timeToLive) }
    }

    override suspend fun ttl(key: String): Duration? {
        return pool.getConnection().use { conn -> StandaloneClientSiths(conn).ttl(key) }
    }

    override suspend fun getOrNull(key: String): String? {
        return pool.getConnection().use { conn -> StandaloneClientSiths(conn).getOrNull(key) }
    }

    override suspend fun get(key: String): String {
        return pool.getConnection().use { conn -> StandaloneClientSiths(conn).get(key) }
    }

    override suspend fun scriptLoad(script: String): String {
        return pool.getConnection().use { conn -> StandaloneClientSiths(conn).scriptLoad(script) }
    }

    override suspend fun evalSha(sha: String, keys: List<String>, args: List<String>): RespType<*> {
        return pool.getConnection().use { conn -> StandaloneClientSiths(conn).evalSha(sha, keys, args) }
    }

    override suspend fun incrBy(key: String, value: Long): Long {
        return pool.getConnection().use { conn -> StandaloneClientSiths(conn).incrBy(key, value) }
    }

    override suspend fun clientList(): List<RedisClient> {
        return pool.getConnection().use { conn -> StandaloneClientSiths(conn).clientList() }
    }
}