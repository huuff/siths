package xyz.haff.siths.client

import kotlin.time.Duration

// TODO: Maybe test the most complex ones
class RedisCommandBuilder : Siths<RedisCommand, RedisCommand, RedisCommand, RedisCommand, RedisCommand, RedisCommand, RedisCommand, RedisCommand> {

    override suspend fun get(key: String) = RedisCommand("GET", key)

    override suspend fun set(key: String, value: Any, exclusiveMode: ExclusiveMode?, timeToLive: Duration?): RedisCommand {
        val mainCommand = RedisCommand("SET", key, value.toString(), exclusiveMode?.toString())

        return if (timeToLive == null) {
            mainCommand
        } else {
            mainCommand + RedisCommand("PX", timeToLive.inWholeMilliseconds.toString())
        }
    }

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

    override suspend fun sadd(key: String, value: Any) = RedisCommand("SADD", key, value.toString())

    override suspend fun smembers(key: String): RedisCommand = RedisCommand("SMEMBERS", key)

    override suspend fun sismember(key: String, member: Any) = RedisCommand("SISMEMBER", key, member.toString())
    override suspend fun scard(key: String): RedisCommand = RedisCommand("SCARD", key)
}