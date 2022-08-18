package xyz.haff.siths.common

import xyz.haff.siths.client.RespType
import kotlin.time.Duration

class RedisScriptNotLoadedException() : RuntimeException()

class RedisLockTimeoutException(
    lockName: String,
    acquireTimeout: Duration
) : RuntimeException("Timed out waiting for $lockName after $acquireTimeout")

class RedisUnexpectedRespResponse(response: RespType<*>): RuntimeException("Unexpected RESP response: $response")

class RedisBrokenConnectionException(cause: Throwable) : RuntimeException(cause)

class RedisPoolOutOfConnections(): RuntimeException("All Redis connections of this pool are currently used")