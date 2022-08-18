package xyz.haff.siths.common

import xyz.haff.siths.client.RespType
import kotlin.time.Duration

class RedisScriptNotLoadedException() : RuntimeException()

class RedisLockTimeoutException(
    lockName: String,
    acquireTimeout: Duration
) : RuntimeException("Timed out waiting for $lockName after $acquireTimeout")

class UnexpectedRespResponse(response: RespType<*>): RuntimeException("Unexpected RESP response: $response")

class BrokenRedisConnectionException(cause: Throwable) : RuntimeException(cause)