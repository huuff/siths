package xyz.haff.siths.client

import xyz.haff.siths.scripts.RedisScript

class PooledSiths(
    private val pool: SithsPool
): Siths {
    override suspend fun set(key: String, value: String) {
        pool.getConnection().use { conn -> StandaloneSiths(conn).set(key, value) }
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

    override suspend fun evalSha(sha: String, keys: List<String>, args: List<String>): String {
        return pool.getConnection().use { conn -> StandaloneSiths(conn).evalSha(sha, keys, args) }
    }

    override suspend fun runScript(script: RedisScript, keys: List<String>, args: List<String>): String {
       return pool.getConnection().use { conn -> StandaloneSiths(conn).runScript(script, keys, args) }
    }
}