package xyz.haff.siths.client

import kotlin.time.Duration

// TODO: Maybe test the most complex ones
class RedisCommandBuilder {

    fun get(key: String) = RedisCommand("GET", key)

    fun set(key: String, value: Any, exclusiveMode: ExclusiveMode? = null, timeToLive: Duration? = null): RedisCommand {
        val mainCommand = RedisCommand("SET", key, value.toString(), exclusiveMode?.toString())

        return if (timeToLive == null) {
            mainCommand
        } else {
            mainCommand + RedisCommand("PX", timeToLive.inWholeMilliseconds.toString())
        }
    }

    fun ttl(key: String) = RedisCommand("PTTL", key)

    fun scriptLoad(script: String) = RedisCommand("SCRIPT", "LOAD", script)

    fun evalSha(sha: String, keys: List<String> = listOf(), args: List<String> = listOf())
        = RedisCommand(
        "EVALSHA",
        sha,
        keys.size.toString(),
        *keys.toTypedArray(),
        *args.toTypedArray()
    )

    fun incrBy(key: String, value: Long) = RedisCommand("INCRBY", key, value)

    fun clientList() = RedisCommand("CLIENT", "LIST")
}