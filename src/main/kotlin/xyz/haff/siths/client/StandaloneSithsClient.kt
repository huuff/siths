package xyz.haff.siths.client

import kotlin.time.Duration

class StandaloneSithsClient(
    private val connection: SithsConnection,
    private val commandBuilder: RedisCommandBuilder = RedisCommandBuilder()
) : SithsClient {

    override suspend fun set(key: String, value: Any, exclusiveMode: ExclusiveMode?, timeToLive: Duration?): Unit
        = connection.runCommand(commandBuilder.set(key, value, exclusiveMode, timeToLive)).toUnit()

    override suspend fun ttl(key: String): Duration?
        = connection.runCommand(commandBuilder.ttl(key)).toDurationOrNull()

    override suspend fun del(key: String, vararg rest: String): Long
    = connection.runCommand(commandBuilder.del(key, *rest)).toLong()

    override suspend fun exists(key: String, vararg rest: String): Boolean
        = connection.runCommand(commandBuilder.exists(key, *rest)).existenceToBoolean(setOf(key, *rest).size.toLong())

    override suspend fun getOrNull(key: String): String? = connection.runCommand(commandBuilder.get(key)).toStringOrNull()

    override suspend fun get(key: String): String = getOrNull(key) ?: throw RuntimeException("Key $key does not exist!")

    override suspend fun scriptLoad(script: String): String
        = connection.runCommand(commandBuilder.scriptLoad(script)).toStringNonNull()

    override suspend fun evalSha(sha: String, keys: List<String>, args: List<String>): RespType<*>
        = connection.runCommand(commandBuilder.evalSha(sha, keys, args)).throwOnError()

    override suspend fun eval(script: String, keys: List<String>, args: List<String>): RespType<*>
        = connection.runCommand(commandBuilder.eval(script, keys, args)).throwOnError()

    override suspend fun incrBy(key: String, value: Long): Long
        = connection.runCommand(commandBuilder.incrBy(key, value)).toLong()

    // SET OPERATIONS

    override suspend fun sadd(key: String, value: Any, vararg rest: Any): Long
        = connection.runCommand(commandBuilder.sadd(key, value, *rest)).toLong()

    override suspend fun smembers(key: String): Set<String>
        = connection.runCommand(commandBuilder.smembers(key)).toStringSet()

    override suspend fun scard(key: String): Long = connection.runCommand(commandBuilder.scard(key)).toLong()

    override suspend fun srem(key: String, member: Any, vararg rest: Any): Long
        = connection.runCommand(commandBuilder.srem(key, member, *rest)).toLong()

    override suspend fun sintercard(key: String, vararg rest: String, limit: Int?): Long
        = connection.runCommand(commandBuilder.sintercard(key, rest = rest, limit = limit)).toLong()

    override suspend fun sdiffstore(destination: String, key: String, vararg rest: String): Long
        = connection.runCommand(commandBuilder.sdiffstore(destination, key, *rest)).toLong()

    override suspend fun sdiff(key: String, vararg rest: String): Set<String>
        = connection.runCommand(commandBuilder.sdiff(key, *rest)).toStringSet()

    override suspend fun sinter(key: String, vararg rest: String): Set<String>
        = connection.runCommand(commandBuilder.sinter(key, *rest)).toStringSet()

    override suspend fun smove(source: String, destination: String, member: Any): Boolean
        = connection.runCommand(commandBuilder.smove(source, destination, member)).integerToBoolean()

    override suspend fun spop(key: String): String?
        = connection.runCommand(commandBuilder.spop(key)).toStringOrNull()

    override suspend fun spop(key: String, count: Int?): Set<String>
        = connection.runCommand(commandBuilder.spop(key, count)).bulkOrArrayToStringSet()

    override suspend fun srandmember(key: String): String?
        = connection.runCommand(commandBuilder.srandmember(key)).toStringOrNull()

    override suspend fun srandmember(key: String, count: Int?): Set<String>
        = connection.runCommand(commandBuilder.srandmember(key, count)).bulkOrArrayToStringSet()

    override suspend fun sunion(key: String, vararg rest: String): Set<String>
        = connection.runCommand(commandBuilder.sunion(key, *rest)).toStringSet()

    override suspend fun sunionstore(destination: String, key: String, vararg rest: String): Long
        = connection.runCommand(commandBuilder.sunionstore(destination, key, *rest)).toLong()

    override suspend fun sinterstore(destination: String, key: String, vararg rest: String): Long
        = connection.runCommand(commandBuilder.sinterstore(destination, key, *rest)).toLong()

    override suspend fun sismember(key: String, member: Any): Boolean
    = connection.runCommand(commandBuilder.sismember(key, member)).integerToBoolean()

    override suspend fun sscan(key: String, cursor: Long, match: String?, count: Int?): RedisCursor<String>
        = connection.runCommand(commandBuilder.sscan(key, cursor, match, count)).toStringCursor()

    override suspend fun smismember(key: String, member: Any, vararg rest: Any): Map<String, Boolean>
            = connection.runCommand(commandBuilder.smismember(key, member, *rest)).toStringToBooleanMap(member, *rest)

    // LIST OPERATIONS

    override suspend fun llen(key: String): Long
        = connection.runCommand(commandBuilder.llen(key)).toLong()

    override suspend fun lindex(key: String, index: Int): String?
        = connection.runCommand(commandBuilder.lindex(key, index)).toStringOrNull()

    override suspend fun linsert(key: String, relativePosition: RelativePosition, pivot: Any, element: Any): Long?
        = connection.runCommand(commandBuilder.linsert(key, relativePosition, pivot, element)).toPositiveLongOrNull()

    override suspend fun lpop(key: String): String?
        = connection.runCommand(commandBuilder.lpop(key)).toStringOrNull()

    override suspend fun lpop(key: String, count: Int): List<String>
        = connection.runCommand(commandBuilder.lpop(key, count)).bulkOrArrayToStringList()

    override suspend fun rpop(key: String, count: Int): List<String>
        = connection.runCommand(commandBuilder.rpop(key, count)).bulkOrArrayToStringList()

    override suspend fun rpop(key: String): String?
        = connection.runCommand(commandBuilder.rpop(key)).toStringOrNull()

    override suspend fun lpush(key: String, element: Any, vararg rest: Any): Long
        = connection.runCommand(commandBuilder.lpush(key, element, *rest)).toLong()

    override suspend fun rpush(key: String, element: Any, vararg rest: Any): Long
        = connection.runCommand(commandBuilder.rpush(key, element, *rest)).toLong()

    override suspend fun lrem(key: String, element: Any, count: Int): Long
        = connection.runCommand(commandBuilder.lrem(key, element, count)).toLong()

    override suspend fun lrange(key: String, start: Int, stop: Int): List<String>
        = connection.runCommand(commandBuilder.lrange(key, start, stop)).toStringList()

    override suspend fun lpos(key: String, element: Any, rank: Int?, maxlen: Int?): Long?
        = connection.runCommand(commandBuilder.lpos(key, element, rank, maxlen)).toLongOrNull()

    override suspend fun lpos(key: String, element: Any, rank: Int?, count: Int, maxlen: Int?): List<Long>
        = connection.runCommand(commandBuilder.lpos(key, element, rank, count, maxlen)).toLongList()

    override suspend fun clientList(): List<RedisClient>
        = connection.runCommand(commandBuilder.clientList()).toClientList()

    override suspend fun ping(): Boolean = connection.runCommand(commandBuilder.ping()).pongToBoolean()

    override suspend fun expire(key: String, duration: Duration, expirationCondition: ExpirationCondition?): Boolean
        = connection.runCommand(commandBuilder.expire(key, duration, expirationCondition)).integerToBoolean()
}