package xyz.haff.siths.client

import xyz.haff.siths.client.api.ListSithsClient
import xyz.haff.siths.client.api.ListSithsImmediateClient
import xyz.haff.siths.command.RedisCommandBuilder
import xyz.haff.siths.option.ListEnd
import xyz.haff.siths.option.RelativePosition
import xyz.haff.siths.protocol.*
import kotlin.time.Duration

class StandaloneListSithsClient(
    private val connection: SithsConnection,
    private val commandBuilder: RedisCommandBuilder = RedisCommandBuilder()
): ListSithsImmediateClient {
    override suspend fun llen(key: String): Long
            = connection.runCommand(commandBuilder.llen(key)).toLong()

    override suspend fun lindex(key: String, index: Int): String?
            = connection.runCommand(commandBuilder.lindex(key, index)).toStringOrNull()

    override suspend fun linsert(key: String, relativePosition: RelativePosition, pivot: String, element: String): Long?
            = connection.runCommand(commandBuilder.linsert(key, relativePosition, pivot, element)).toPositiveLongOrNull()

    override suspend fun lpop(key: String): String?
            = connection.runCommand(commandBuilder.lpop(key)).toStringOrNull()

    override suspend fun lpop(key: String, count: Int?): List<String>
            = connection.runCommand(commandBuilder.lpop(key, count)).bulkOrArrayToStringList()

    override suspend fun lmpop(keys: List<String>, end: ListEnd, count: Int?): SourceAndData<List<String>>?
            = connection.runCommand(commandBuilder.lmpop(keys, end, count)).toSourceAndStringListOrNull()

    override suspend fun blmpop(
        timeout: Duration,
        key: String,
        vararg otherKeys: String,
        end: ListEnd,
        count: Int?
    ): SourceAndData<List<String>>?
        = connection.runCommand(commandBuilder.blmpop(timeout, key, otherKeys = otherKeys, end = end, count = count)).toSourceAndStringListOrNull()

    override suspend fun brpop(key: String, vararg otherKeys: String, timeout: Duration?): SourceAndData<String>?
        = connection.runCommand(commandBuilder.brpop(key, otherKeys = otherKeys, timeout = timeout)).toSourceAndStringOrNull()

    override suspend fun blpop(key: String, vararg otherKeys: String, timeout: Duration?): SourceAndData<String>?
        = connection.runCommand(commandBuilder.blpop(key, otherKeys = otherKeys, timeout = timeout)).toSourceAndStringOrNull()

    override suspend fun lmpop(key: String, end: ListEnd, count: Int?): List<String>
            = lmpop(listOf(key), end, count)?.data ?: listOf()

    override suspend fun rpop(key: String, count: Int?): List<String>
            = connection.runCommand(commandBuilder.rpop(key, count)).bulkOrArrayToStringList()

    override suspend fun rpop(key: String): String?
            = connection.runCommand(commandBuilder.rpop(key)).toStringOrNull()

    override suspend fun lpush(key: String, element: String, vararg rest: String): Long
            = connection.runCommand(commandBuilder.lpush(key, element, *rest)).toLong()

    override suspend fun lpush(key: String, elements: Collection<String>): Long
        = connection.runCommand(commandBuilder.lpush(key, elements)).toLong()

    override suspend fun lpushx(key: String, element: String, vararg rest: String): Long
            = connection.runCommand(commandBuilder.lpushx(key, element, *rest)).toLong()

    override suspend fun rpush(key: String, element: String, vararg rest: String): Long
            = connection.runCommand(commandBuilder.rpush(key, element, *rest)).toLong()

    override suspend fun rpush(key: String, elements: Collection<String>): Long
        = connection.runCommand(commandBuilder.rpush(key, elements)).toLong()

    override suspend fun rpushx(key: String, element: String, vararg rest: String): Long
            = connection.runCommand(commandBuilder.rpushx(key, element, *rest)).toLong()

    override suspend fun lrem(key: String, element: String, count: Int): Long
            = connection.runCommand(commandBuilder.lrem(key, element, count)).toLong()

    override suspend fun lrange(key: String, start: Int, stop: Int): List<String>
            = connection.runCommand(commandBuilder.lrange(key, start, stop)).toStringList()

    override suspend fun lpos(key: String, element: String, rank: Int?, maxlen: Int?): Long?
            = connection.runCommand(commandBuilder.lpos(key, element, rank, maxlen)).toLongOrNull()

    override suspend fun lpos(key: String, element: String, rank: Int?, count: Int, maxlen: Int?): List<Long>
            = connection.runCommand(commandBuilder.lpos(key, element, rank, count, maxlen)).toLongList()

    override suspend fun lset(key: String, index: Int, element: String): Boolean
            = connection.runCommand(commandBuilder.lset(key, index, element)).isOk()

    override suspend fun ltrim(key: String, start: Int, stop: Int)
            = connection.runCommand(commandBuilder.ltrim(key, start, stop)).assertOk()

    override suspend fun lmove(
        source: String,
        destination: String,
        sourceEnd: ListEnd,
        destinationEnd: ListEnd
    ): String
            = connection.runCommand(commandBuilder.lmove(source, destination, sourceEnd, destinationEnd)).toStringNonNull()

    override suspend fun blmove(
        source: String,
        destination: String,
        sourceEnd: ListEnd,
        destinationEnd: ListEnd,
        timeout: Duration?
    ): String?
        = connection.runCommand(commandBuilder.blmove(source, destination, sourceEnd, destinationEnd, timeout)).toStringOrNull()

    override suspend fun lpushAny(key: String, element: Any, vararg rest: Any): Long
        = lpush(key, element.toString(), *rest.map(Any::toString).toTypedArray())

    override suspend fun rpushAny(key: String, element: Any, vararg rest: Any): Long
        = rpush(key, element.toString(), *rest.map(Any::toString).toTypedArray())
}