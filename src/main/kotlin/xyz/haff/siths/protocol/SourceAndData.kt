package xyz.haff.siths.protocol

/**
 * For those operations that operate on several keys (sources) and return the source that matched, along with the data received
 * MLPOP, for example
 */
data class SourceAndData<T>(val source: String, val data: T)
