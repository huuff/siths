package xyz.haff.siths.client

import xyz.haff.siths.client.api.HashSithsImmediateClient
import xyz.haff.siths.command.RedisCommandBuilder
import xyz.haff.siths.common.mapSecond
import xyz.haff.siths.protocol.*

class StandaloneHashSithsClient(
    private val connection: SithsConnection,
    private val commandBuilder: RedisCommandBuilder = RedisCommandBuilder()
) : HashSithsImmediateClient {
    override suspend fun hgetOrNull(key: String, field: String): String?
        = connection.runCommand(commandBuilder.hget(key, field)).toStringOrNull()

    override suspend fun hget(key: String, field: String): String = hgetOrNull(key, field)!!

    override suspend fun hset(key: String, pair: Pair<String, String>, vararg rest: Pair<String, String>): Long
        = connection.runCommand(commandBuilder.hset(key, pair, *rest)).toLong()

    override suspend fun hsetAny(key: String, pair: Pair<String, Any>, vararg rest: Pair<String, Any>): Long
        = hset(key, pair.mapSecond(Any::toString), *rest.map { it.mapSecond(Any::toString) }.toTypedArray())

    override suspend fun hgetall(key: String): Map<String, String>
        = connection.runCommand(commandBuilder.hgetall(key)).toStringMap()

    override suspend fun hkeys(key: String): List<String>
        = connection.runCommand(commandBuilder.hkeys(key)).toStringList()

    override suspend fun hvals(key: String): List<String>
        = connection.runCommand(commandBuilder.hvals(key)).toStringList()

    override suspend fun hexists(key: String, field: String): Boolean
        = connection.runCommand(commandBuilder.hexists(key, field)).integerToBoolean()

    override suspend fun hincrby(key: String, field: String, increment: Long): Long
        = connection.runCommand(commandBuilder.hincrby(key, field, increment)).toLong()

    override suspend fun hincrbyfloat(key: String, field: String, increment: Double): Double
        = connection.runCommand(commandBuilder.hincrbyfloat(key, field, increment)).toDouble()

    override suspend fun hmget(key: String, field: String, vararg rest: String): Map<String, String>
        = connection.runCommand(commandBuilder.hmget(key, field, *rest)).associateArrayToArguments(field, *rest)
}