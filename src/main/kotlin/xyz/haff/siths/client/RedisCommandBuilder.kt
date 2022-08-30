package xyz.haff.siths.client

import kotlin.time.Duration

class RedisCommandBuilder : Siths<
        RedisCommand,
        RedisCommand,
        RedisCommand,
        RedisCommand,
        RedisCommand,
        RedisCommand,
        RedisCommand,
        RedisCommand,
        RedisCommand,
        RedisCommand,
        RedisCommand,
        RedisCommand,
        RedisCommand,
        RedisCommand,
        RedisCommand,
        RedisCommand,
        > {

    override suspend fun get(key: String) = RedisCommand("GET", key)

    override suspend fun set(key: String, value: String, exclusiveMode: ExclusiveMode?, timeToLive: Duration?): RedisCommand {
        val mainCommand = RedisCommand("SET", key, value, exclusiveMode?.toString())

        return if (timeToLive == null) {
            mainCommand
        } else {
            mainCommand + RedisCommand("PX", timeToLive.inWholeMilliseconds.toString())
        }
    }

    override suspend fun del(key: String, vararg rest: String) = RedisCommand(
        "DEL",
        key,
        *rest
    )

    override suspend fun ttl(key: String) = RedisCommand("PTTL", key)

    override suspend fun scriptLoad(script: String) = RedisCommand("SCRIPT", "LOAD", script)

    override suspend fun evalSha(sha: String, keys: List<String>, args: List<String>)
        = RedisCommand(
        "EVALSHA",
        sha,
        keys.size.toString(),
        *keys.toTypedArray(),
        *args.toTypedArray()
    )

    override suspend fun incrBy(key: String, value: Long) = RedisCommand("INCRBY", key, value)

    override suspend fun incrByFloat(key: String, value: Double): RedisCommand = RedisCommand("INCRBYFLOAT", key, value)

    override suspend fun clientList() = RedisCommand("CLIENT", "LIST")

    override suspend fun eval(script: String, keys: List<String>, args: List<String>) = RedisCommand(
        "EVAL",
        script,
        keys.size.toString(),
        *keys.toTypedArray(),
        *args.toTypedArray()
    )

    override suspend fun exists(key: String, vararg rest: String) = RedisCommand("EXISTS", key, *rest)

    override suspend fun expire(key: String, duration: Duration, expirationCondition: ExpirationCondition?)
            = RedisCommand("PEXPIRE", key, duration.inWholeMilliseconds, expirationCondition)

    override suspend fun ping(): RedisCommand = RedisCommand("PING")

    override suspend fun persist(key: String): RedisCommand
            = RedisCommand("PERSIST", key)

    // SET OPERATIONS

    override suspend fun sadd(key: String, value: String, vararg rest: String) = RedisCommand(
        "SADD",
        key,
        value,
        *rest,
    )

    override suspend fun smembers(key: String): RedisCommand = RedisCommand("SMEMBERS", key)

    override suspend fun sismember(key: String, member: String) = RedisCommand("SISMEMBER", key, member)
    override suspend fun scard(key: String): RedisCommand = RedisCommand("SCARD", key)
    override suspend fun srem(key: String, member: String, vararg rest: String): RedisCommand
        = RedisCommand("SREM", key, member, *rest)

    override suspend fun sintercard(key: String, vararg rest: String, limit: Int?): RedisCommand {
        val mainCommand = RedisCommand("SINTERCARD", 1 + rest.size, key, *rest)

        return if (limit == null) {
            mainCommand
        } else {
            mainCommand + RedisCommand("LIMIT", limit)
        }
    }

    override suspend fun sdiffstore(destination: String, key: String, vararg rest: String): RedisCommand
        = RedisCommand("SDIFFSTORE", destination, key, *rest)

    override suspend fun sinterstore(destination: String, key: String, vararg rest: String): RedisCommand
        = RedisCommand("SINTERSTORE", destination, key, *rest)

    override suspend fun sscan(key: String, cursor: Long, match: String?, count: Int?): RedisCommand {
        var command = RedisCommand("SSCAN", key, cursor)

        if (match != null) {
            command += RedisCommand("MATCH", match)
        }

        if (count != null) {
            command += RedisCommand("COUNT", count)
        }

        return command
    }

    override suspend fun sdiff(key: String, vararg rest: String): RedisCommand
        = RedisCommand("SDIFF", key, *rest)

    override suspend fun sinter(key: String, vararg rest: String): RedisCommand
        = RedisCommand("SINTER", key, *rest)

    override suspend fun smove(source: String, destination: String, member: String): RedisCommand
        = RedisCommand("SMOVE", source, destination, member)

    override suspend fun spop(key: String): RedisCommand
        = RedisCommand("SPOP", key)

    override suspend fun spop(key: String, count: Int?): RedisCommand
        = RedisCommand("SPOP", key, count)

    override suspend fun srandmember(key: String)
        = RedisCommand("SRANDMEMBER", key)

    override suspend fun srandmember(key: String, count: Int?): RedisCommand
        = RedisCommand("SRANDMEMBER", key, count)

    override suspend fun sunion(key: String, vararg rest: String): RedisCommand
        = RedisCommand("SUNION", key, *rest)

    override suspend fun sunionstore(destination: String, key: String, vararg rest: String): RedisCommand
        = RedisCommand("SUNIONSTORE", destination, key, *rest)

    override suspend fun smismember(key: String, member: String, vararg rest: String)
        = RedisCommand("SMISMEMBER", key, member, *rest)

    override suspend fun llen(key: String): RedisCommand
        = RedisCommand("LLEN", key)

    override suspend fun lindex(key: String, index: Int): RedisCommand
        = RedisCommand("LINDEX", key, index)

    override suspend fun linsert(
        key: String,
        relativePosition: RelativePosition,
        pivot: String,
        element: String
    ): RedisCommand = RedisCommand("LINSERT", key, relativePosition, pivot, element)

    override suspend fun lpop(key: String, count: Int?): RedisCommand
        = RedisCommand("LPOP", key, count)

    override suspend fun lpop(key: String): RedisCommand = RedisCommand("LPOP", key)

    override suspend fun lmpop(keys: List<String>, end: ListEnd, count: Int?): RedisCommand
        = RedisCommand("LMPOP", keys.size, *keys.toTypedArray(), end) + count?.let { RedisCommand("COUNT", it) }

    override suspend fun rpop(key: String, count: Int?): RedisCommand
        = RedisCommand("RPOP", key, count)

    override suspend fun rpop(key: String): RedisCommand = RedisCommand("RPOP", key)

    override suspend fun lpush(key: String, element: String, vararg rest: String): RedisCommand
        = RedisCommand("LPUSH", key, element, *rest)

    override suspend fun lpushx(key: String, element: String, vararg rest: String): RedisCommand
        = RedisCommand("LPUSHX", key, element, *rest)

    override suspend fun rpush(key: String, element: String, vararg rest: String): RedisCommand
        = RedisCommand("RPUSH", key, element, *rest)

    override suspend fun lrem(key: String, element: String, count: Int): RedisCommand
        = RedisCommand("LREM", key, count, element)

    override suspend fun lrange(key: String, start: Int, stop: Int): RedisCommand
        = RedisCommand("LRANGE", key, start, stop)

    override suspend fun lpos(key: String, element: String, rank: Int?, maxlen: Int?)
        = (RedisCommand("LPOS", key, element)
            + rank?.let { RedisCommand("RANK", it) }
            + maxlen?.let { RedisCommand("MAXLEN", it)}
    )

    override suspend fun lpos(key: String, element: String, rank: Int?, count: Int, maxlen: Int?): RedisCommand
        = (RedisCommand("LPOS", key, element)
                + rank?.let { RedisCommand("RANK", it) }
                + RedisCommand("COUNT", count)
                + maxlen?.let { RedisCommand("MAXLEN", it) })

    override suspend fun lset(key: String, index: Int, element: String): RedisCommand
        = RedisCommand("LSET", key, index, element)

    override suspend fun ltrim(key: String, start: Int, stop: Int): RedisCommand
        = RedisCommand("LTRIM", key, start, stop)
}