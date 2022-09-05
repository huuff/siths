package xyz.haff.siths.command

import xyz.haff.siths.client.api.RedisCommandReceiver
import xyz.haff.siths.option.*
import java.time.ZonedDateTime
import kotlin.time.Duration
import kotlin.time.DurationUnit

private fun countSubCommand(count: Int?) = count?.let { RedisCommand("COUNT", it) }
private fun durationToFloatSeconds(duration: Duration?) = duration?.toDouble(DurationUnit.SECONDS) ?: 0.0
private fun pairsToStringArray(vararg pairs: Pair<String, String>) =
    pairs.flatMap { listOf(it.first, it.second) }.toTypedArray()

class RedisCommandBuilder : RedisCommandReceiver<
        // lol
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
        RedisCommand,
        RedisCommand,
        RedisCommand,
        RedisCommand,
        > {

    override suspend fun get(key: String) = RedisCommand("GET", key)

    override suspend fun mset(vararg pairs: Pair<String, String>): RedisCommand =
        RedisCommand("MSET", *pairsToStringArray(*pairs))

    override suspend fun mget(key: String, vararg rest: String): RedisCommand = RedisCommand("MGET", key, *rest)

    override suspend fun set(
        key: String,
        value: String,
        existenceCondition: ExistenceCondition?,
        timeToLive: Duration?
    ): RedisCommand {
        val mainCommand = RedisCommand("SET", key, value, existenceCondition?.toString())

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

    override suspend fun evalSha(sha: String, keys: List<String>, args: List<String>) = RedisCommand(
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

    override suspend fun expire(key: String, duration: Duration, expirationCondition: ExpirationCondition?) =
        RedisCommand("PEXPIRE", key, duration.inWholeMilliseconds, expirationCondition)

    override suspend fun ping(): RedisCommand = RedisCommand("PING")

    override suspend fun persist(key: String): RedisCommand = RedisCommand("PERSIST", key)

    override suspend fun expireAt(
        key: String,
        time: ZonedDateTime,
        expirationCondition: ExpirationCondition?
    ): RedisCommand = RedisCommand("EXPIREAT", key, time.toEpochSecond(), expirationCondition)

    override suspend fun expireTime(key: String): RedisCommand = RedisCommand("EXPIRETIME", key)
    override suspend fun dbSize(): RedisCommand = RedisCommand("DBSIZE")
    override suspend fun flushDb(mode: SyncMode?) = RedisCommand("FLUSHDB", mode)

    // SET OPERATIONS

    override suspend fun sadd(key: String, value: String, vararg rest: String) = RedisCommand(
        "SADD",
        key,
        value,
        *rest,
    )

    override suspend fun smembers(key: String): RedisCommand = RedisCommand("SMEMBERS", key)

    override suspend fun sisMember(key: String, member: String) = RedisCommand("SISMEMBER", key, member)
    override suspend fun scard(key: String): RedisCommand = RedisCommand("SCARD", key)
    override suspend fun srem(key: String, member: String, vararg rest: String): RedisCommand =
        RedisCommand("SREM", key, member, *rest)

    override suspend fun sinterCard(key: String, vararg rest: String, limit: Int?): RedisCommand {
        val mainCommand = RedisCommand("SINTERCARD", 1 + rest.size, key, *rest)

        return if (limit == null) {
            mainCommand
        } else {
            mainCommand + RedisCommand("LIMIT", limit)
        }
    }

    override suspend fun sdiffStore(destination: String, key: String, vararg rest: String): RedisCommand =
        RedisCommand("SDIFFSTORE", destination, key, *rest)

    override suspend fun sinterStore(destination: String, key: String, vararg rest: String): RedisCommand =
        RedisCommand("SINTERSTORE", destination, key, *rest)

    override suspend fun sscan(key: String, cursor: Long, match: String?, count: Int?): RedisCommand =
        (RedisCommand("SSCAN", key, cursor)
                + match?.let { RedisCommand("MATCH", match) }
                + countSubCommand(count))

    override suspend fun sdiff(key: String, vararg rest: String): RedisCommand = RedisCommand("SDIFF", key, *rest)

    override suspend fun sinter(key: String, vararg rest: String): RedisCommand = RedisCommand("SINTER", key, *rest)

    override suspend fun smove(source: String, destination: String, member: String): RedisCommand =
        RedisCommand("SMOVE", source, destination, member)

    override suspend fun spop(key: String): RedisCommand = RedisCommand("SPOP", key)

    override suspend fun spop(key: String, count: Int?): RedisCommand = RedisCommand("SPOP", key, count)

    override suspend fun srandMember(key: String) = RedisCommand("SRANDMEMBER", key)

    override suspend fun srandMember(key: String, count: Int?): RedisCommand = RedisCommand("SRANDMEMBER", key, count)

    override suspend fun sunion(key: String, vararg rest: String): RedisCommand = RedisCommand("SUNION", key, *rest)

    override suspend fun sunionStore(destination: String, key: String, vararg rest: String): RedisCommand =
        RedisCommand("SUNIONSTORE", destination, key, *rest)

    override suspend fun smisMember(key: String, member: String, vararg rest: String) =
        RedisCommand("SMISMEMBER", key, member, *rest)

    override suspend fun llen(key: String): RedisCommand = RedisCommand("LLEN", key)

    override suspend fun lindex(key: String, index: Int): RedisCommand = RedisCommand("LINDEX", key, index)

    override suspend fun linsert(
        key: String,
        relativePosition: RelativePosition,
        pivot: String,
        element: String
    ): RedisCommand = RedisCommand("LINSERT", key, relativePosition, pivot, element)

    override suspend fun lpop(key: String, count: Int?): RedisCommand = RedisCommand("LPOP", key, count)

    override suspend fun lpop(key: String): RedisCommand = RedisCommand("LPOP", key)

    override suspend fun lmpop(keys: List<String>, end: ListEnd, count: Int?): RedisCommand =
        RedisCommand("LMPOP", keys.size, *keys.toTypedArray(), end) + countSubCommand(count)

    override suspend fun lmpop(key: String, end: ListEnd, count: Int?): RedisCommand = lmpop(listOf(key), end, count)

    override suspend fun rpop(key: String, count: Int?): RedisCommand = RedisCommand("RPOP", key, count)

    override suspend fun rpop(key: String): RedisCommand = RedisCommand("RPOP", key)

    override suspend fun lpush(key: String, element: String, vararg rest: String): RedisCommand =
        RedisCommand("LPUSH", key, element, *rest)

    override suspend fun lpushx(key: String, element: String, vararg rest: String): RedisCommand =
        RedisCommand("LPUSHX", key, element, *rest)

    override suspend fun rpush(key: String, element: String, vararg rest: String): RedisCommand =
        RedisCommand("RPUSH", key, element, *rest)

    override suspend fun rpushx(key: String, element: String, vararg rest: String): RedisCommand =
        RedisCommand("RPUSHX", key, element, *rest)

    override suspend fun lrem(key: String, element: String, count: Int): RedisCommand =
        RedisCommand("LREM", key, count, element)

    override suspend fun lrange(key: String, start: Int, stop: Int): RedisCommand =
        RedisCommand("LRANGE", key, start, stop)

    override suspend fun lpos(key: String, element: String, rank: Int?, maxlen: Int?) =
        (RedisCommand("LPOS", key, element)
                + rank?.let { RedisCommand("RANK", it) }
                + maxlen?.let { RedisCommand("MAXLEN", it) }
                )

    override suspend fun lpos(key: String, element: String, rank: Int?, count: Int, maxlen: Int?): RedisCommand =
        (RedisCommand("LPOS", key, element)
                + rank?.let { RedisCommand("RANK", it) }
                + countSubCommand(count)!!
                + maxlen?.let { RedisCommand("MAXLEN", it) })

    override suspend fun lset(key: String, index: Int, element: String): RedisCommand =
        RedisCommand("LSET", key, index, element)

    override suspend fun ltrim(key: String, start: Int, stop: Int): RedisCommand =
        RedisCommand("LTRIM", key, start, stop)

    override suspend fun lmove(
        source: String,
        destination: String,
        sourceEnd: ListEnd,
        destinationEnd: ListEnd
    ): RedisCommand = RedisCommand("LMOVE", source, destination, sourceEnd, destinationEnd)

    override suspend fun blmpop(
        timeout: Duration,
        key: String,
        vararg otherKeys: String,
        end: ListEnd,
        count: Int?
    ): RedisCommand =
        RedisCommand("BLMPOP", durationToFloatSeconds(timeout), 1 + otherKeys.size, end) + countSubCommand(count)

    override suspend fun brpop(key: String, vararg otherKeys: String, timeout: Duration?): RedisCommand =
        RedisCommand("BRPOP", key, *otherKeys, durationToFloatSeconds(timeout))

    override suspend fun blpop(key: String, vararg otherKeys: String, timeout: Duration?): RedisCommand =
        RedisCommand("BLPOP", key, *otherKeys, durationToFloatSeconds(timeout))

    override suspend fun blmove(
        source: String,
        destination: String,
        sourceEnd: ListEnd,
        destinationEnd: ListEnd,
        timeout: Duration?
    ): RedisCommand =
        RedisCommand("BLMOVE", source, destination, sourceEnd, destinationEnd, durationToFloatSeconds(timeout))

    // HASH OPERATIONS
    override suspend fun hget(key: String, field: String): RedisCommand = RedisCommand("HGET", key, field)

    override suspend fun hset(
        key: String,
        pair: Pair<String, String>,
        vararg rest: Pair<String, String>
    ): RedisCommand = RedisCommand("HSET", key, pair.first, pair.second, *pairsToStringArray(*rest))

    override suspend fun hgetAll(key: String): RedisCommand = RedisCommand("HGETALL", key)

    override suspend fun hkeys(key: String): RedisCommand = RedisCommand("HKEYS", key)

    override suspend fun hvals(key: String): RedisCommand = RedisCommand("HVALS", key)

    override suspend fun hexists(key: String, field: String): RedisCommand = RedisCommand("HEXISTS", key, field)

    override suspend fun hincrBy(key: String, field: String, increment: Long): RedisCommand =
        RedisCommand("HINCRBY", key, field, increment)

    override suspend fun hincrByFloat(key: String, field: String, increment: Double): RedisCommand =
        RedisCommand("HINCRBYFLOAT", key, field, increment)

    override suspend fun hmget(key: String, field: String, vararg rest: String): RedisCommand =
        RedisCommand("HMGET", key, field, *rest)

    override suspend fun hlen(key: String): RedisCommand = RedisCommand("HLEN", key)

    override suspend fun hdel(key: String, field: String, vararg rest: String): RedisCommand =
        RedisCommand("HDEL", key, field, *rest)

    override suspend fun hstrLen(key: String, field: String): RedisCommand = RedisCommand("HSTRLEN", key, field)

    override suspend fun hsetnx(key: String, field: String, value: String): RedisCommand =
        RedisCommand("HSETNX", key, field, value)

    override suspend fun hscan(key: String, cursor: Long, match: String?, count: Int?): RedisCommand =
        (RedisCommand("HSCAN", key, cursor)
                + match?.let { RedisCommand("MATCH", match) }
                + countSubCommand(count))

    // HRANDFIELD
    override suspend fun hrandField(key: String): RedisCommand = RedisCommand("HRANDFIELD", key)

    override suspend fun hrandField(key: String, count: Int): RedisCommand = RedisCommand("HRANDFIELD", key, count)

    override suspend fun hrandFieldWithValues(key: String, count: Int): RedisCommand =
        RedisCommand("HRANDFIELD", key, count, "WITHVALUES")


}