package xyz.haff.siths.pipelining

import xyz.haff.siths.client.api.SithsClient
import xyz.haff.siths.command.RedisCommandBuilder
import xyz.haff.siths.option.ExclusiveMode
import xyz.haff.siths.option.ExpirationCondition
import xyz.haff.siths.protocol.*
import kotlin.time.Duration

class PipelinedSithsClient(
    // TODO: Surely we shouldn't take the connection at the constructor, but only at execution! otherwise we'd be locking a
    // connection without actually using it
    private val connection: SithsConnection,
    private val commandBuilder: RedisCommandBuilder = RedisCommandBuilder(),
    private val executor: RedisPipelineExecutor = RedisPipelineExecutor(),
) : SithsClient<
        QueuedResponse<Long>,
        QueuedResponse<Long?>,
        QueuedResponse<List<Long>>,

        QueuedResponse<Double>,

        QueuedResponse<String>,
        QueuedResponse<String?>,
        QueuedResponse<List<String>>,
        QueuedResponse<Set<String>>,

        QueuedResponse<List<RedisClient>>,
        QueuedResponse<Duration?>,

        QueuedResponse<Map<String, Boolean>>,
        QueuedResponse<Map<String, String>>,

        QueuedResponse<RedisCursor<String>>,
        QueuedResponse<RedisCursor<Pair<String, String>>>,

        QueuedResponse<SourceAndData<String>?>,
        QueuedResponse<SourceAndData<List<String>>?>,

        QueuedResponse<Boolean>,
        QueuedResponse<Unit>,
        QueuedResponse<RespType<*>>,
        >,
        IPipelinedSetSithsClient by PipelinedSetSithsClient(executor),
        IPipelinedHashSithsClient by PipelinedHashSithsClient(executor),
        IPipelinedListSithsClient by PipelinedListSithsClient(executor)
{

    suspend fun exec(inTransaction: Boolean = false) = executor.exec(connection, inTransaction)

    override suspend fun set(
        key: String,
        value: String,
        exclusiveMode: ExclusiveMode?,
        timeToLive: Duration?
    ): QueuedResponse<Unit> = executor.addOperation(
        DeferredCommand(
            command = commandBuilder.set(key, value, exclusiveMode, timeToLive),
            response = QueuedResponse(RespType<*>::toUnit)
        )
    )

    override suspend fun get(key: String): QueuedResponse<String> =
        executor.addOperation(DeferredCommand(commandBuilder.get(key), QueuedResponse(RespType<*>::toStringNonNull)))

    override suspend fun getOrNull(key: String): QueuedResponse<String?> =
        executor.addOperation(DeferredCommand(commandBuilder.get(key), QueuedResponse(RespType<*>::toStringOrNull)))

    override suspend fun mset(vararg pairs: Pair<String, String>): QueuedResponse<Unit> =
        executor.addOperation(DeferredCommand(commandBuilder.mset(*pairs), QueuedResponse(RespType<*>::toUnit)))

    override suspend fun mget(key: String, vararg rest: String): QueuedResponse<Map<String, String>> = executor.addOperation(
        DeferredCommand(
            commandBuilder.mget(key, *rest),
            QueuedResponse({ it.associateArrayToArguments(key, *rest) })
        )
    )

    override suspend fun del(key: String, vararg rest: String): QueuedResponse<Long> =
        executor.addOperation(DeferredCommand(commandBuilder.del(key, *rest), QueuedResponse(RespType<*>::toLong)))

    override suspend fun ttl(key: String): QueuedResponse<Duration?> =
        executor.addOperation(DeferredCommand(commandBuilder.ttl(key), QueuedResponse(RespType<*>::toDurationOrNull)))

    override suspend fun scriptLoad(script: String): QueuedResponse<String> = executor.addOperation(
        DeferredCommand(
            command = commandBuilder.scriptLoad(script),
            response = QueuedResponse(RespType<*>::toStringNonNull)
        )
    )

    override suspend fun evalSha(sha: String, keys: List<String>, args: List<String>): QueuedResponse<RespType<*>> =
        executor.addOperation(
            DeferredCommand(
                command = commandBuilder.evalSha(sha, keys, args),
                response = QueuedResponse(RespType<*>::throwOnError)
            )
        )

    override suspend fun eval(script: String, keys: List<String>, args: List<String>): QueuedResponse<RespType<*>> =
        executor.addOperation(
            DeferredCommand(
                command = commandBuilder.eval(script, keys, args),
                response = QueuedResponse(RespType<*>::throwOnError)
            )
        )

    override suspend fun incrBy(key: String, value: Long): QueuedResponse<Long> =
        executor.addOperation(DeferredCommand(commandBuilder.incrBy(key, value), QueuedResponse(RespType<*>::toLong)))

    override suspend fun incrByFloat(key: String, value: Double): QueuedResponse<Double> =
        executor.addOperation(DeferredCommand(commandBuilder.incrByFloat(key, value), QueuedResponse(RespType<*>::toDouble)))

    override suspend fun exists(key: String, vararg rest: String): QueuedResponse<Boolean> {
        return executor.addOperation(
            DeferredCommand(
                command = commandBuilder.exists(key, *rest),
                response = QueuedResponse(converter = { it.existenceToBoolean(setOf(key, *rest).size.toLong()) })
            )
        )
    }

    override suspend fun expire(
        key: String,
        duration: Duration,
        expirationCondition: ExpirationCondition?
    ): QueuedResponse<Boolean> = executor.addOperation(
        DeferredCommand(
            command = commandBuilder.expire(key, duration, expirationCondition),
            response = QueuedResponse(RespType<*>::integerToBoolean)
        )
    )

    override suspend fun persist(key: String): QueuedResponse<Boolean> = executor.addOperation(
        DeferredCommand(
            command = commandBuilder.persist(key),
            response = QueuedResponse(RespType<*>::integerToBoolean)
        )
    )

    override suspend fun clientList(): QueuedResponse<List<RedisClient>> =
        executor.addOperation(DeferredCommand(commandBuilder.clientList(), QueuedResponse(RespType<*>::toClientList)))

    override suspend fun ping(): QueuedResponse<Boolean> =
        executor.addOperation(DeferredCommand(commandBuilder.ping(), QueuedResponse(RespType<*>::pongToBoolean)))

}