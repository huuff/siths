package xyz.haff.siths.scripts

object RedisScripts {

    /**
     * KEYS[1]: Lock key
     * ARGV[1]: Expiration time in milliseconds
     * ARGV[2]: Lock identifier
     */
    val ACQUIRE_LOCK = RedisScript("""
    if redis.call("exists", KEYS[1]) == 0 then
        return redis.call("psetex", KEYS[1], unpack(ARGV))
    end
    """.trimIndent())

    /**
     * KEYS[1]: Lock key
     * ARGV[1]: Lock identifier
     */
    val RELEASE_LOCK = RedisScript("""
    if redis.call("get", KEYS[1]) == ARGV[1] then
        return redis.call("del", KEYS[1]) or true
    end
    """.trimIndent()
    )
}