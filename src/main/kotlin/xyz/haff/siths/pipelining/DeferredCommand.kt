package xyz.haff.siths.pipelining

import xyz.haff.siths.command.RedisCommand

data class DeferredCommand<T>(val command: RedisCommand, val response: QueuedResponseImpl<T>)
