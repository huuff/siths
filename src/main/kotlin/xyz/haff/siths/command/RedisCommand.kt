package xyz.haff.siths.command

import xyz.haff.siths.pipelining.RedisPipeline

data class RedisCommand(
    private val parts: List<String>,
) {
    constructor(vararg parts: Any?) : this(parts.asSequence().filterNotNull().map(Any::toString).toList())

    fun toResp(): String = buildString {
        append("*${parts.size}\r\n")
        for (part in parts) {
            append("$${part.length}\r\n")
            append("${part}\r\n")
        }
    }

    operator fun plus(other: RedisCommand?) = if (other != null) {
        RedisCommand(this.parts + other.parts)
    } else {
        this
    }

    operator fun plus(pipeline: RedisPipeline) = RedisPipeline(listOf(this) + pipeline.commands)
}