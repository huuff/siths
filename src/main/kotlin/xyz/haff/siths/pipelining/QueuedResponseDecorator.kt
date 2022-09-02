package xyz.haff.siths.pipelining

import xyz.haff.siths.protocol.RespType

class QueuedResponseDecorator<Input, Output>(
    private val decorated: QueuedResponse<Input>,
    private val transform: (Input) -> Output
): QueuedResponse<Output> {
    override fun get(): Output = transform(decorated.get())

    override fun set(response: RespType<*>) {
        decorated.set(response)
    }
}