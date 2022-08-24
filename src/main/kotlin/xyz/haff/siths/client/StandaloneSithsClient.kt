package xyz.haff.siths.client

import xyz.haff.siths.common.handleUnexpectedRespResponse
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

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

    override suspend fun sinterstore(destination: String, key: String, vararg rest: String): Long
        = connection.runCommand(commandBuilder.sinterstore(destination, key, *rest)).toLong()

    override suspend fun sismember(key: String, member: Any): Boolean
    = connection.runCommand(commandBuilder.sismember(key, member)).integerToBoolean()

    override suspend fun sscan(key: String, cursor: Long, match: String?, count: Int?): RedisCursor<String>
        = connection.runCommand(commandBuilder.sscan(key, cursor, match, count)).toStringCursor()

    override suspend fun clientList(): List<RedisClient>
        = connection.runCommand(commandBuilder.clientList()).toClientList()

    override suspend fun ping(): Boolean = connection.runCommand(commandBuilder.ping()).pongToBoolean()

    override suspend fun expire(key: String, duration: Duration, expirationCondition: ExpirationCondition?): Boolean
        = connection.runCommand(commandBuilder.expire(key, duration, expirationCondition)).integerToBoolean()
}