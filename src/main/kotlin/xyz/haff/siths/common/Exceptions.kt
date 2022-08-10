package xyz.haff.siths.client

import java.time.Duration

class RedisScriptNotLoadedException() : RuntimeException()

class RedisLockTimeoutException(
    lockName: String,
    acquireTimeout: Duration
) : RuntimeException("Timed out waiting for $lockName after $acquireTimeout")