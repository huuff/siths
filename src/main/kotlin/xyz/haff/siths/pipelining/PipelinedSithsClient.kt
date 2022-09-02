package xyz.haff.siths.pipelining

import xyz.haff.siths.client.api.SithsClient
import xyz.haff.siths.command.RedisCommandBuilder
import xyz.haff.siths.option.ExistenceCondition
import xyz.haff.siths.option.ExpirationCondition
import xyz.haff.siths.protocol.*
import java.time.ZonedDateTime
import kotlin.time.Duration

class PipelinedSithsClient(
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
        QueuedResponse<ZonedDateTime?>,

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
    IPipelinedListSithsClient by PipelinedListSithsClient(executor) {

    suspend fun exec(connection: SithsConnection, inTransaction: Boolean = false) =
        executor.exec(connection, inTransaction)

    override suspend fun set(
        key: String,
        value: String,
        existenceCondition: ExistenceCondition?,
        timeToLive: Duration?
    ): QueuedResponse<Unit> = executor.addOperation(
        DeferredCommand(
            command = commandBuilder.set(key, value, existenceCondition, timeToLive),
            response = QueuedResponseImpl(RespType<*>::toUnit)
        )
    )

    override suspend fun get(key: String): QueuedResponse<String> =
        executor.addOperation(
            DeferredCommand(
                commandBuilder.get(key),
                QueuedResponseImpl(RespType<*>::toStringNonNull)
            )
        )

    override suspend fun getLong(key: String): QueuedResponse<Long> = get(key).map(String::toLong)

    override suspend fun getOrNull(key: String): QueuedResponse<String?> =
        executor.addOperation(DeferredCommand(commandBuilder.get(key), QueuedResponseImpl(RespType<*>::toStringOrNull)))

    override suspend fun mset(vararg pairs: Pair<String, String>): QueuedResponse<Unit> =
        executor.addOperation(DeferredCommand(commandBuilder.mset(*pairs), QueuedResponseImpl(RespType<*>::toUnit)))

    override suspend fun mget(key: String, vararg rest: String): QueuedResponse<Map<String, String>> =
        executor.addOperation(
            DeferredCommand(
                commandBuilder.mget(key, *rest),
                QueuedResponseImpl({ it.associateArrayToArguments(key, *rest) })
            )
        )

    override suspend fun del(key: String, vararg rest: String): QueuedResponse<Long> =
        executor.addOperation(DeferredCommand(commandBuilder.del(key, *rest), QueuedResponseImpl(RespType<*>::toLong)))

    override suspend fun ttl(key: String): QueuedResponse<Duration?> =
        executor.addOperation(
            DeferredCommand(
                commandBuilder.ttl(key),
                QueuedResponseImpl(RespType<*>::toDurationOrNull)
            )
        )

    override suspend fun scriptLoad(script: String): QueuedResponse<String> = executor.addOperation(
        DeferredCommand(
            command = commandBuilder.scriptLoad(script),
            response = QueuedResponseImpl(RespType<*>::toStringNonNull)
        )
    )

    override suspend fun evalSha(sha: String, keys: List<String>, args: List<String>): QueuedResponse<RespType<*>> =
        executor.addOperation(
            DeferredCommand(
                command = commandBuilder.evalSha(sha, keys, args),
                response = QueuedResponseImpl(RespType<*>::throwOnError)
            )
        )

    override suspend fun eval(script: String, keys: List<String>, args: List<String>): QueuedResponse<RespType<*>> =
        executor.addOperation(
            DeferredCommand(
                command = commandBuilder.eval(script, keys, args),
                response = QueuedResponseImpl(RespType<*>::throwOnError)
            )
        )

    override suspend fun incrBy(key: String, value: Long): QueuedResponse<Long> =
        executor.addOperation(
            DeferredCommand(
                commandBuilder.incrBy(key, value),
                QueuedResponseImpl(RespType<*>::toLong)
            )
        )

    override suspend fun incrByFloat(key: String, value: Double): QueuedResponse<Double> =
        executor.addOperation(
            DeferredCommand(
                commandBuilder.incrByFloat(key, value),
                QueuedResponseImpl(RespType<*>::toDouble)
            )
        )

    override suspend fun exists(key: String, vararg rest: String): QueuedResponse<Boolean> {
        return executor.addOperation(
            DeferredCommand(
                command = commandBuilder.exists(key, *rest),
                response = QueuedResponseImpl(converter = { it.existenceToBoolean(setOf(key, *rest).size.toLong()) })
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
            response = QueuedResponseImpl(RespType<*>::integerToBoolean)
        )
    )

    override suspend fun persist(key: String): QueuedResponse<Boolean> = executor.addOperation(
        DeferredCommand(
            command = commandBuilder.persist(key),
            response = QueuedResponseImpl(RespType<*>::integerToBoolean)
        )
    )

    override suspend fun clientList(): QueuedResponse<List<RedisClient>> =
        executor.addOperation(
            DeferredCommand(
                commandBuilder.clientList(),
                QueuedResponseImpl(RespType<*>::toClientList)
            )
        )

    override suspend fun ping(): QueuedResponse<Boolean> =
        executor.addOperation(DeferredCommand(commandBuilder.ping(), QueuedResponseImpl(RespType<*>::pongToBoolean)))

    override suspend fun expireAt(
        key: String,
        time: ZonedDateTime,
        expirationCondition: ExpirationCondition?
    ): QueuedResponse<Boolean> = executor.addOperation(
        DeferredCommand(
            command = commandBuilder.expireAt(key, time, expirationCondition),
            response = QueuedResponseImpl(RespType<*>::integerToBoolean)
        )
    )

    override suspend fun expireTime(key: String): QueuedResponse<ZonedDateTime?> = executor.addOperation(
        DeferredCommand(
            command = commandBuilder.expireTime(key),
            response = QueuedResponseImpl(RespType<*>::toNullableZonedDateTime)
        )
    )
}