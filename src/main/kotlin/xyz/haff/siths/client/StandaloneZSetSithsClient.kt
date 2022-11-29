package xyz.haff.siths.client

import xyz.haff.siths.client.api.ZSetSithsImmediateClient
import xyz.haff.siths.command.RedisCommandBuilder
import xyz.haff.siths.option.ComparisonCondition
import xyz.haff.siths.option.ExistenceCondition
import xyz.haff.siths.option.Limit
import xyz.haff.siths.protocol.SithsConnection
import xyz.haff.siths.protocol.toLong
import xyz.haff.siths.protocol.toStringSet
import xyz.haff.siths.protocol.toStringToDoubleList

class StandaloneZSetSithsClient(
    private val connection: SithsConnection,
    private val commandBuilder: RedisCommandBuilder = RedisCommandBuilder()
) : ZSetSithsImmediateClient {
    override suspend fun zadd(
        key: String,
        scoreAndMember: Pair<Double, String>,
        vararg rest: Pair<Double, String>,
        existenceCondition: ExistenceCondition?,
        comparisonCondition: ComparisonCondition?,
        returnChanged: Boolean
    ): Long = connection.runCommand(
        commandBuilder.zadd(
            key,
            scoreAndMember,
            *rest,
            existenceCondition = existenceCondition,
            comparisonCondition = comparisonCondition,
            returnChanged = returnChanged
        )
    ).toLong()

    override suspend fun zadd(
        key: String,
        scoreAndMembers: Collection<Pair<Double, String>>,
        existenceCondition: ExistenceCondition?,
        comparisonCondition: ComparisonCondition?,
        returnChanged: Boolean
    ): Long = connection.runCommand(commandBuilder.zadd(key, scoreAndMembers, existenceCondition, comparisonCondition, returnChanged)).toLong()

    override suspend fun zrangeByRank(
        key: String,
        start: Int,
        stop: Int,
        reverse: Boolean,
        limit: Limit?
    ): Set<String> = connection.runCommand(commandBuilder.zrangeByRank(key, start, stop, reverse, limit)).toStringSet()

    override suspend fun zrangeByRankWithScores(
        key: String,
        start: Int,
        stop: Int,
        reverse: Boolean,
        limit: Limit?
    ): List<Pair<String, Double>> = connection.runCommand(commandBuilder.zrangeByRankWithScores(key, start, stop, reverse, limit)).toStringToDoubleList()

    override suspend fun zrangeByScore(
        key: String,
        start: Double,
        stop: Double,
        reverse: Boolean,
        limit: Limit?
    ): Set<String> = connection.runCommand(
        commandBuilder.zrangeByScore(key, start, stop, reverse, limit)
    ).toStringSet()

    override suspend fun zrangeByScoreWithScores(
        key: String,
        start: Double,
        stop: Double,
        reverse: Boolean,
        limit: Limit?
    ): List<Pair<String, Double>> = connection.runCommand(
        commandBuilder.zrangeByScoreWithScores(key, start, stop, reverse, limit)
    ).toStringToDoubleList()
}