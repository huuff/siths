package xyz.haff.siths.client

import xyz.haff.siths.client.api.*
import xyz.haff.siths.command.RedisCommandBuilder
import xyz.haff.siths.option.ExclusiveMode
import xyz.haff.siths.option.ExpirationCondition
import xyz.haff.siths.protocol.*
import kotlin.time.Duration

class StandaloneSithsClient(
    private val connection: SithsConnection,
    private val commandBuilder: RedisCommandBuilder = RedisCommandBuilder()
) :
    SithsImmediateClient,
    ListSithsImmediateClient by StandaloneListSithsClient(connection, commandBuilder),
    SetSithsImmediateClient by StandaloneSetSithsClient(connection, commandBuilder),
    HashSithsImmediateClient by StandaloneHashSithsClient(connection, commandBuilder)
{

    override suspend fun set(key: String, value: String, exclusiveMode: ExclusiveMode?, timeToLive: Duration?): Unit =
        connection.runCommand(commandBuilder.set(key, value, exclusiveMode, timeToLive)).toUnit()

    override suspend fun mset(vararg pairs: Pair<String, String>)
        = connection.runCommand(commandBuilder.mset(*pairs)).toUnit()

    override suspend fun mget(key: String, vararg rest: String): Map<String, String>
        = connection.runCommand(commandBuilder.mget(key, *rest)).associateArrayToArguments(key, *rest)

    override suspend fun ttl(key: String): Duration? = connection.runCommand(commandBuilder.ttl(key)).toDurationOrNull()

    override suspend fun del(key: String, vararg rest: String): Long =
        connection.runCommand(commandBuilder.del(key, *rest)).toLong()

    override suspend fun exists(key: String, vararg rest: String): Boolean =
        connection.runCommand(commandBuilder.exists(key, *rest)).existenceToBoolean(setOf(key, *rest).size.toLong())

    override suspend fun getOrNull(key: String): String? =
        connection.runCommand(commandBuilder.get(key)).toStringOrNull()

    override suspend fun get(key: String): String = getOrNull(key) ?: throw RuntimeException("Key $key does not exist!")

    override suspend fun getLong(key: String): Long = get(key).toLong()

    override suspend fun scriptLoad(script: String): String =
        connection.runCommand(commandBuilder.scriptLoad(script)).toStringNonNull()

    override suspend fun evalSha(sha: String, keys: List<String>, args: List<String>): RespType<*> =
        connection.runCommand(commandBuilder.evalSha(sha, keys, args)).throwOnError()

    override suspend fun eval(script: String, keys: List<String>, args: List<String>): RespType<*> =
        connection.runCommand(commandBuilder.eval(script, keys, args)).throwOnError()

    override suspend fun incrBy(key: String, value: Long): Long =
        connection.runCommand(commandBuilder.incrBy(key, value)).toLong()

    override suspend fun incrByFloat(key: String, value: Double): Double =
        connection.runCommand(commandBuilder.incrByFloat(key, value)).toDouble()

    override suspend fun clientList(): List<RedisClient> =
        connection.runCommand(commandBuilder.clientList()).toClientList()

    override suspend fun ping(): Boolean = connection.runCommand(commandBuilder.ping()).pongToBoolean()

    override suspend fun expire(key: String, duration: Duration, expirationCondition: ExpirationCondition?): Boolean =
        connection.runCommand(commandBuilder.expire(key, duration, expirationCondition)).integerToBoolean()

    override suspend fun persist(key: String): Boolean =
        connection.runCommand(commandBuilder.persist(key)).integerToBoolean()
}