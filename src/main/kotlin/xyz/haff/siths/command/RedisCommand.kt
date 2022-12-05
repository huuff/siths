package xyz.haff.siths.command

import xyz.haff.siths.pipelining.RedisPipeline

data class RedisCommand(
    private val parts: List<String>,
) {
    constructor(vararg parts: Any?) : this(parts.asSequence().filterNotNull().map(Any::toString).toList())

    companion object {
        fun fromCollection(parts: Collection<String>) = RedisCommand(parts = parts.toList())
    }

    fun toResp(): String = buildString {
        append("*${parts.size}\r\n")
        for (part in parts) {
            // TODO: XXX: Did this in a pinch, but converting to a byte array on every iteration might be very wasteful,
            // considering that we're about to do it again then when sending the command. Maybe I should just try to send a byte array
            // directly from this method and send that through the socker?
            append("$${part.toByteArray(Charsets.UTF_8).size}\r\n")
            append("${part}\r\n")
        }
    }

    operator fun plus(other: RedisCommand?) = if (other != null) {
        RedisCommand(this.parts + other.parts)
    } else {
        this
    }

    // TODO: Test
    operator fun plus(rest: Iterable<RedisCommand>): RedisCommand = rest.fold(this, RedisCommand::plus)

    operator fun plus(pipeline: RedisPipeline) = RedisPipeline(listOf(this) + pipeline.commands)

    override fun toString(): String {
        return parts.joinToString(separator = " ") { "\"${it.replace("\"", "\\\"")}\"" }
    }
}