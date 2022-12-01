package xyz.haff.siths.common

import xyz.haff.siths.command.RedisCommand
import xyz.haff.siths.protocol.RespType
import kotlin.time.Duration

class RedisScriptNotLoadedException() : RuntimeException()

class RedisException(val type: String, override val message: String): RuntimeException(message)

class RedisLockTimeoutException(
    lockName: String,
    acquireTimeout: Duration
) : RuntimeException("Timed out waiting for $lockName after $acquireTimeout")

class RedisUnexpectedRespResponseException(response: RespType<*>): RuntimeException("Unexpected RESP response: $response")

class RedisBrokenConnectionException(offendingCommand: RedisCommand, cause: Throwable) : RuntimeException(
    "Broken redis connection when running $offendingCommand", cause
)

class RedisPoolOutOfConnectionsException(): RuntimeException("All Redis connections of this pool are currently used")

class RedisAuthException(response: RespType<*>): RuntimeException("Unable to authenticate. Redis response: $response")

class UnexecutedRedisPipelineException: RuntimeException("Trying to get the contents of an unexecuted pipeline!")

