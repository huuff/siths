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

    /**
     *  Inserts at a specific position in a list, the algorithm is:
     *      1. Save existing key at position
     *      2. Insert a random marker
     *      3. Insert after the random marker
     *      4. Put the previous value at the marker's position
     *
     *  KEYS[1]: List key
     *  ARGV[1]: Position at which to insert
     *  ARGV[1:]: Elements to insert
     *
     */
    val LIST_INSERT_AT = RedisScript("""
        local list = KEYS[1]
        local index = ARGV[1]
        local previous_val = redis.call("lindex", KEYS[1], index)
        local position_marker = math.random()
        redis.call("lset", list, index, position_marker)
        
        for i = #ARGV, 2, -1 do
            redis.call("linsert", list, "AFTER", position_marker, ARGV[i]) 
        end
        
        redis.call("lset", list, index, previous_val)
    """.trimIndent())


    val LIST_RETAIN_ALL = RedisScript("""
        local destination = KEYS[1]
        local other = KEYS[2]
        local changed_list = false
        local delete_marker = math.random()
        
        for pos = 0, redis.call("llen", destination) - 1 do
            local current_element = redis.call("lindex", destination, pos)
            local position_in_other = redis.call("lpos", other, current_element)
            if not position_in_other then
                redis.call("lset", destination, pos, delete_marker)
                changed_list = true
            end
        end
        
        redis.call("lrem", destination, 0, delete_marker)
        return changed_list
    """.trimIndent())
}