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
        > {

    override suspend fun get(key: String) = RedisCommand("GET", key)

    override suspend fun set(key: String, value: Any, exclusiveMode: ExclusiveMode?, timeToLive: Duration?): RedisCommand {
        val mainCommand = RedisCommand("SET", key, value.toString(), exclusiveMode?.toString())

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

    override suspend fun clientList() = RedisCommand("CLIENT", "LIST")

    override suspend fun eval(script: String, keys: List<String>, args: List<String>) = RedisCommand(
        "EVAL",
        script,
        keys.size.toString(),
        *keys.toTypedArray(),
        *args.toTypedArray()
    )

    override suspend fun exists(key: String, vararg rest: String) = RedisCommand("EXISTS", key, *rest)

    override suspend fun ping(): RedisCommand = RedisCommand("PING")

    override suspend fun sadd(key: String, value: Any, vararg rest: Any) = RedisCommand(
        "SADD",
        key,
        value.toString(),
        *rest,
    )

    override suspend fun smembers(key: String): RedisCommand = RedisCommand("SMEMBERS", key)

    override suspend fun sismember(key: String, member: Any) = RedisCommand("SISMEMBER", key, member.toString())
    override suspend fun scard(key: String): RedisCommand = RedisCommand("SCARD", key)
    override suspend fun srem(key: String, member: Any, vararg rest: Any): RedisCommand
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
    
    override suspend fun expire(key: String, duration: Duration, expirationCondition: ExpirationCondition?)
        = RedisCommand("PEXPIRE", key, duration.inWholeMilliseconds, expirationCondition)
}