package xyz.haff.siths.client

import java.time.Duration

class PooledSiths(
    private val pool: SithsPool
): Siths {
    override suspend fun set(key: String, value: String, exclusiveMode: ExclusiveMode?, timeToLive: Duration?) {
        pool.getConnection().use { conn -> StandaloneSiths(conn).set(key, value, exclusiveMode, timeToLive) }
    }

    override suspend fun ttl(key: String): Duration? {
        return pool.getConnection().use { conn -> StandaloneSiths(conn).ttl(key) }
    }

    override suspend fun getOrNull(key: String): String? {
        return pool.getConnection().use { conn -> StandaloneSiths(conn).getOrNull(key) }
    }

    override suspend fun get(key: String): String {
        return pool.getConnection().use { conn -> StandaloneSiths(conn).get(key) }
    }

    override suspend fun scriptLoad(script: String): String {
        return pool.getConnection().use { conn -> StandaloneSiths(conn).scriptLoad(script) }
    }

    override suspend fun evalSha(sha: String, keys: List<String>, args: List<String>): RespType<*> {
        return pool.getConnection().use { conn -> StandaloneSiths(conn).evalSha(sha, keys, args) }
    }

    override suspend fun incrBy(key: String, value: Long): Long {
        return pool.getConnection().use { conn -> StandaloneSiths(conn).incrBy(key, value) }
    }
}