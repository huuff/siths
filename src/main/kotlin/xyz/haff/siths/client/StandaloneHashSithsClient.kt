package xyz.haff.siths.client

import xyz.haff.siths.client.api.HashSithsImmediateClient
import xyz.haff.siths.command.RedisCommandBuilder
import xyz.haff.siths.protocol.SithsConnection
import xyz.haff.siths.protocol.toLong
import xyz.haff.siths.protocol.toStringOrNull

class StandaloneHashSithsClient(
    private val connection: SithsConnection,
    private val commandBuilder: RedisCommandBuilder = RedisCommandBuilder()
) : HashSithsImmediateClient {
    override suspend fun hgetOrNull(key: String, field: String): String?
        = connection.runCommand(commandBuilder.hget(key, field)).toStringOrNull()

    override suspend fun hget(key: String, field: String): String = hgetOrNull(key, field)!!

    override suspend fun hset(key: String, pair: Pair<String, String>, vararg rest: Pair<String, String>): Long
        = connection.runCommand(commandBuilder.hset(key, pair, *rest)).toLong()
}