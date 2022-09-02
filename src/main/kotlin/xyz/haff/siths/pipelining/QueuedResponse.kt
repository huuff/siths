package xyz.haff.siths.pipelining

import xyz.haff.siths.protocol.RespType

interface QueuedResponse<T> {

    fun get(): T
    fun set(response: RespType<*>)
    fun <R> map(f: (T) -> R): QueuedResponse<R> = QueuedResponseDecorator<T, R>(decorated = this, transform = f)
}