package xyz.haff.siths.pipelining

import xyz.haff.siths.command.RedisCommand
import xyz.haff.siths.protocol.RespArray
import xyz.haff.siths.protocol.RespType
import xyz.haff.siths.protocol.SithsConnection
import xyz.haff.siths.protocol.handleAsUnexpected

/**
 * Builds a list of deferred commands, and allows running them, filling in the responses
 */
class RedisPipelineExecutor(
    private val operations: MutableList<DeferredCommand<*>> = mutableListOf()
) {

    val length get() = operations.size

    fun <T> addOperation(operation: DeferredCommand<T>): QueuedResponseImpl<T> {
        operations += operation
        return operation.response
    }

    suspend fun exec(connection: SithsConnection, inTransaction: Boolean = false): List<RespType<*>> {
        val pipeline = if (inTransaction) {
            RedisPipeline(
                commands = listOf(
                    RedisCommand("MULTI"),
                    *operations.map { it.command }.toTypedArray(),
                    RedisCommand("EXEC")
                )
            )
        } else {
            RedisPipeline(commands = operations.map { it.command })
        }
        val queuedResponses = operations.map { it.response }
        val actualResponses = if (inTransaction) {
            val results = connection.runPipeline(pipeline)
            val multiResponse = results[1 + operations.size] // Skip all QUEUED responses, and the OK for the multi
            if (multiResponse is RespArray) {
                multiResponse.value // It must be a RespArray, since that's the specification for an EXEC
            } else {
                multiResponse.handleAsUnexpected()
            }
        } else {
            connection.runPipeline(pipeline)
        }

        (queuedResponses zip actualResponses).forEach { (queuedResponse, actualResponse) ->
            queuedResponse.set(actualResponse)
        }

        return actualResponses
    }
}