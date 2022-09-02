package xyz.haff.siths.pipelining

import xyz.haff.siths.command.RedisCommandBuilder
import xyz.haff.siths.protocol.*

class PipelinedSetSithsClient(
    private val executor: RedisPipelineExecutor = RedisPipelineExecutor(),
    private val commandBuilder: RedisCommandBuilder = RedisCommandBuilder(),
) : IPipelinedSetSithsClient {

    override suspend fun sadd(key: String, value: String, vararg rest: String): QueuedResponse<Long> = executor.addOperation(
        DeferredCommand(
            command = commandBuilder.sadd(key, value, *rest),
            response = QueuedResponse(RespType<*>::toLong)
        )
    )

    override suspend fun saddAny(key: String, value: Any, vararg rest: Any): QueuedResponse<Long>
            = sadd(key, value.toString(), *rest.map(Any::toString).toTypedArray())

    override suspend fun smembers(key: String): QueuedResponse<Set<String>> =
        executor.addOperation(DeferredCommand(commandBuilder.smembers(key), QueuedResponse(RespType<*>::toStringSet)))

    override suspend fun sisMember(key: String, member: String): QueuedResponse<Boolean> = executor.addOperation(
        DeferredCommand(
            command = commandBuilder.sisMember(key, member),
            response = QueuedResponse(RespType<*>::integerToBoolean)
        )
    )

    override suspend fun scard(key: String): QueuedResponse<Long> =
        executor.addOperation(DeferredCommand(commandBuilder.scard(key), QueuedResponse(RespType<*>::toLong)))

    override suspend fun srem(key: String, member: String, vararg rest: String): QueuedResponse<Long> = executor.addOperation(
        DeferredCommand(
            command = commandBuilder.srem(key, member, *rest),
            response = QueuedResponse(RespType<*>::toLong)
        )
    )

    override suspend fun sinterCard(key: String, vararg rest: String, limit: Int?): QueuedResponse<Long> = executor.addOperation(
        DeferredCommand(
            command = commandBuilder.sinterCard(key, rest = rest, limit = limit),
            response = QueuedResponse(RespType<*>::toLong)
        )
    )

    override suspend fun sdiffStore(destination: String, key: String, vararg rest: String): QueuedResponse<Long> =
        executor.addOperation(
            DeferredCommand(
                command = commandBuilder.sdiffStore(destination, key, *rest),
                response = QueuedResponse(RespType<*>::toLong)
            )
        )

    override suspend fun sinterStore(destination: String, key: String, vararg rest: String): QueuedResponse<Long> =
        executor.addOperation(
            DeferredCommand(
                command = commandBuilder.sinterStore(destination, key, *rest),
                response = QueuedResponse(RespType<*>::toLong)
            )
        )

    override suspend fun sscan(
        key: String,
        cursor: Long,
        match: String?,
        count: Int?
    ): QueuedResponse<RedisCursor<String>> = executor.addOperation(
        DeferredCommand(
            command = commandBuilder.sscan(key, cursor, match, count),
            response = QueuedResponse(RespType<*>::toStringCursor)
        )
    )

    override suspend fun sdiff(key: String, vararg rest: String): QueuedResponse<Set<String>> = executor.addOperation(
        DeferredCommand(
            command = commandBuilder.sdiff(key, *rest),
            response = QueuedResponse(RespType<*>::toStringSet)
        )
    )

    override suspend fun sinter(key: String, vararg rest: String): QueuedResponse<Set<String>> = executor.addOperation(
        DeferredCommand(
            command = commandBuilder.sinter(key, *rest),
            response = QueuedResponse(RespType<*>::toStringSet)
        )
    )

    override suspend fun smove(source: String, destination: String, member: String): QueuedResponse<Boolean> =
        executor.addOperation(
            DeferredCommand(
                command = commandBuilder.smove(source, destination, member),
                response = QueuedResponse(RespType<*>::integerToBoolean)
            )
        )

    override suspend fun spop(key: String): QueuedResponse<String?> =
        executor.addOperation(
            DeferredCommand(
                command = commandBuilder.spop(key),
                response = QueuedResponse(RespType<*>::toStringOrNull)
            )
        )

    override suspend fun spop(key: String, count: Int?): QueuedResponse<Set<String>> = executor.addOperation(
        DeferredCommand(
            command = commandBuilder.spop(key, count),
            response = QueuedResponse(RespType<*>::bulkOrArrayToStringSet)
        )
    )

    override suspend fun srandMember(key: String): QueuedResponse<String?> = executor.addOperation(
        DeferredCommand(
            command = commandBuilder.srandMember(key),
            response = QueuedResponse(RespType<*>::toStringOrNull)
        )
    )

    override suspend fun srandMember(key: String, count: Int?): QueuedResponse<Set<String>> {
        return executor.addOperation(
            DeferredCommand(
                command = commandBuilder.srandMember(key, count),
                response = QueuedResponse(RespType<*>::bulkOrArrayToStringSet)
            )
        )
    }

    override suspend fun sunion(key: String, vararg rest: String): QueuedResponse<Set<String>> = executor.addOperation(
        DeferredCommand(
            command = commandBuilder.sunion(key, *rest),
            response = QueuedResponse(RespType<*>::toStringSet)
        )
    )

    override suspend fun sunionStore(destination: String, key: String, vararg rest: String): QueuedResponse<Long> =
        executor.addOperation(
            DeferredCommand(
                commandBuilder.sunionStore(destination, key, *rest),
                QueuedResponse(RespType<*>::toLong)
            )
        )

    override suspend fun smisMember(
        key: String,
        member: String,
        vararg rest: String
    ): QueuedResponse<Map<String, Boolean>> = executor.addOperation(
        DeferredCommand(
            commandBuilder.smisMember(key, member, *rest),
            QueuedResponse(converter = { it.toStringToBooleanMap(member, *rest) })
        )
    )
}