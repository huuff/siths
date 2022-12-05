package xyz.haff.siths.pipelining

import xyz.haff.siths.command.RedisCommandBuilder
import xyz.haff.siths.option.ComparisonCondition
import xyz.haff.siths.option.ExistenceCondition
import xyz.haff.siths.option.Limit
import xyz.haff.siths.protocol.RespType
import xyz.haff.siths.protocol.toLong
import xyz.haff.siths.protocol.toStringSet
import xyz.haff.siths.protocol.toStringToDoubleList

class PipelinedZSetSithsClient(
    private val executor: RedisPipelineExecutor = RedisPipelineExecutor(),
    private val commandBuilder: RedisCommandBuilder = RedisCommandBuilder(),
): IPipelinedZSetSithsClient {
    override suspend fun zadd(
        key: String,
        scoreAndMember: Pair<Double, String>,
        vararg rest: Pair<Double, String>,
        existenceCondition: ExistenceCondition?,
        comparisonCondition: ComparisonCondition?,
        returnChanged: Boolean
    ): QueuedResponse<Long> = executor.addOperation(
        DeferredCommand(
            command = commandBuilder.zadd(
                key,
                scoreAndMember,
                *rest,
                existenceCondition = existenceCondition,
                comparisonCondition = comparisonCondition,
                returnChanged = returnChanged
            ),
            response = QueuedResponseImpl(RespType<*>::toLong)
        )
    )

    override suspend fun zadd(
        key: String,
        scoreAndMembers: Collection<Pair<Double, String>>,
        existenceCondition: ExistenceCondition?,
        comparisonCondition: ComparisonCondition?,
        returnChanged: Boolean
    ): QueuedResponse<Long> = executor.addOperation(
        DeferredCommand(
            command = commandBuilder.zadd(key, scoreAndMembers, existenceCondition, comparisonCondition, returnChanged),
            response = QueuedResponseImpl(RespType<*>::toLong)
        )
    )

    override suspend fun zrangeByRank(
        key: String,
        start: Int,
        stop: Int,
        reverse: Boolean,
        limit: Limit?
    ): QueuedResponse<Set<String>> = executor.addOperation(
        DeferredCommand(
            command = commandBuilder.zrangeByRank(key, start, stop, reverse, limit),
            response = QueuedResponseImpl(RespType<*>::toStringSet)
        )
    )

    override suspend fun zrangeByRankWithScores(
        key: String,
        start: Int,
        stop: Int,
        reverse: Boolean,
        limit: Limit?
    ): QueuedResponse<List<Pair<String, Double>>> = executor.addOperation(
        DeferredCommand(
            command = commandBuilder.zrangeByRankWithScores(key, start, stop, reverse, limit),
            response = QueuedResponseImpl(RespType<*>::toStringToDoubleList)
        )
    )
    override suspend fun zrangeByScore(
        key: String,
        start: Double,
        stop: Double,
        reverse: Boolean,
        limit: Limit?
    ): QueuedResponse<Set<String>> = executor.addOperation(
        DeferredCommand(
            command = commandBuilder.zrangeByScore(key, start, stop, reverse, limit),
            response = QueuedResponseImpl(RespType<*>::toStringSet)
        )
    )

    override suspend fun zrangeByScoreWithScores(
        key: String,
        start: Double,
        stop: Double,
        reverse: Boolean,
        limit: Limit?
    ): QueuedResponse<List<Pair<String, Double>>> = executor.addOperation(
        DeferredCommand(
            command = commandBuilder.zrangeByScoreWithScores(key, start, stop, reverse, limit),
            response = QueuedResponseImpl(RespType<*>::toStringToDoubleList)
        )
    )

    override suspend fun zremRangeByScore(key: String, min: Double, max: Double): QueuedResponse<Long> = executor.addOperation(
        DeferredCommand(
            command = commandBuilder.zremRangeByScore(key, min, max),
            response = QueuedResponseImpl(RespType<*>::toLong)
        )
    )
}