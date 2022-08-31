package xyz.haff.siths.client

import xyz.haff.siths.common.UnexecutedRedisPipelineException
import xyz.haff.siths.protocol.RespType

class QueuedResponse<T>(
    private val converter: (RespType<*>) -> T,
    private var contents: RespType<*>? = null,
) {

    fun get(): T {
        if (contents == null) {
            throw UnexecutedRedisPipelineException()
        } else {
            return converter(contents!!)
        }
    }

    internal fun set(response: RespType<*>) {
        contents = response
    }
}