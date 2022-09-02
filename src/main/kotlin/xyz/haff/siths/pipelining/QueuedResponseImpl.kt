package xyz.haff.siths.pipelining

import xyz.haff.siths.common.UnexecutedRedisPipelineException
import xyz.haff.siths.protocol.RespType

class QueuedResponseImpl<T>(
    private val converter: (RespType<*>) -> T,
    private var contents: RespType<*>? = null,
): QueuedResponse<T> {

    override fun get(): T {
        if (contents == null) {
            throw UnexecutedRedisPipelineException()
        } else {
            return converter(contents!!)
        }
    }

    override fun set(response: RespType<*>) {
        contents = response
    }
}