package xyz.haff.siths.client.api

import xyz.haff.siths.option.ComparisonCondition
import xyz.haff.siths.option.ExistenceCondition
import xyz.haff.siths.option.Limit

interface ZSetRedisCommandReceiver<
        LongResponseType,
        StringSetResponseType,
        StringToDoubleListResponseType,
        > {

    suspend fun zadd(
        key: String,
        scoreAndMember: Pair<Double, String>,
        vararg rest: Pair<Double, String>,
        existenceCondition: ExistenceCondition? = null,
        comparisonCondition: ComparisonCondition? = null,
        returnChanged: Boolean = false,
    ): LongResponseType

    suspend fun zadd(
        key: String,
        scoreAndMembers: Collection<Pair<Double, String>>,
        existenceCondition: ExistenceCondition? = null,
        comparisonCondition: ComparisonCondition? = null,
        returnChanged: Boolean = false,
    ): LongResponseType

    suspend fun zrangeByRank(
        key: String,
        start: Int,
        stop: Int,
        reverse: Boolean = false,
        limit: Limit? = null,
    ): StringSetResponseType

    suspend fun zrangeByRankWithScores(
        key: String,
        start: Int,
        stop: Int,
        reverse: Boolean = false,
        limit: Limit? = null
    ): StringToDoubleListResponseType

    // TODO: I'm not considering all the range variations there are for this (open, closed, to infinite, etc.)
    // TODO: Variants: zrangeByLex, zrangeByLexWithScores
    suspend fun zrangeByScore(
        key: String,
        start: Double,
        stop: Double,
        reverse: Boolean = false,
        limit: Limit? = null,
    ): StringSetResponseType

    suspend fun zrangeByScoreWithScores(
        key: String,
        start: Double,
        stop: Double,
        reverse: Boolean = false,
        limit: Limit? = null,
    ): StringToDoubleListResponseType

    suspend fun zremRangeByScore(
        key: String,
        min: Double,
        max: Double,
    ): LongResponseType
}