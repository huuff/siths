package xyz.haff.siths.client


import xyz.haff.siths.client.api.SetSithsImmediateClient
import xyz.haff.siths.command.RedisCommandBuilder
import xyz.haff.siths.protocol.*

class StandaloneSetSithsClient(
    private val connection: SithsConnection,
    private val commandBuilder: RedisCommandBuilder = RedisCommandBuilder()
): SetSithsImmediateClient {

    override suspend fun sadd(key: String, value: String, vararg rest: String): Long
            = connection.runCommand(commandBuilder.sadd(key, value, *rest)).toLong()

    override suspend fun saddAny(key: String, value: Any, vararg rest: Any): Long
        = sadd(key, value.toString(), *rest.map(Any::toString).toTypedArray())

    override suspend fun smembers(key: String): Set<String>
            = connection.runCommand(commandBuilder.smembers(key)).toStringSet()

    override suspend fun scard(key: String): Long = connection.runCommand(commandBuilder.scard(key)).toLong()

    override suspend fun srem(key: String, member: String, vararg rest: String): Long
            = connection.runCommand(commandBuilder.srem(key, member, *rest)).toLong()

    override suspend fun sinterCard(key: String, vararg rest: String, limit: Int?): Long
            = connection.runCommand(commandBuilder.sinterCard(key, rest = rest, limit = limit)).toLong()

    override suspend fun sdiffStore(destination: String, key: String, vararg rest: String): Long
            = connection.runCommand(commandBuilder.sdiffStore(destination, key, *rest)).toLong()

    override suspend fun sdiff(key: String, vararg rest: String): Set<String>
            = connection.runCommand(commandBuilder.sdiff(key, *rest)).toStringSet()

    override suspend fun sinter(key: String, vararg rest: String): Set<String>
            = connection.runCommand(commandBuilder.sinter(key, *rest)).toStringSet()

    override suspend fun smove(source: String, destination: String, member: String): Boolean
            = connection.runCommand(commandBuilder.smove(source, destination, member)).integerToBoolean()

    override suspend fun spop(key: String): String?
            = connection.runCommand(commandBuilder.spop(key)).toStringOrNull()

    override suspend fun spop(key: String, count: Int?): Set<String>
            = connection.runCommand(commandBuilder.spop(key, count)).bulkOrArrayToStringSet()

    override suspend fun srandMember(key: String): String?
            = connection.runCommand(commandBuilder.srandMember(key)).toStringOrNull()

    override suspend fun srandMember(key: String, count: Int?): Set<String>
            = connection.runCommand(commandBuilder.srandMember(key, count)).bulkOrArrayToStringSet()

    override suspend fun sunion(key: String, vararg rest: String): Set<String>
            = connection.runCommand(commandBuilder.sunion(key, *rest)).toStringSet()

    override suspend fun sunionStore(destination: String, key: String, vararg rest: String): Long
            = connection.runCommand(commandBuilder.sunionStore(destination, key, *rest)).toLong()

    override suspend fun sinterStore(destination: String, key: String, vararg rest: String): Long
            = connection.runCommand(commandBuilder.sinterStore(destination, key, *rest)).toLong()

    override suspend fun sisMember(key: String, member: String): Boolean
            = connection.runCommand(commandBuilder.sisMember(key, member)).integerToBoolean()

    override suspend fun sscan(key: String, cursor: Long, match: String?, count: Int?): RedisCursor<String>
            = connection.runCommand(commandBuilder.sscan(key, cursor, match, count)).toStringCursor()

    override suspend fun smisMember(key: String, member: String, vararg rest: String): Map<String, Boolean>
            = connection.runCommand(commandBuilder.smisMember(key, member, *rest)).toStringToBooleanMap(member, *rest)
}