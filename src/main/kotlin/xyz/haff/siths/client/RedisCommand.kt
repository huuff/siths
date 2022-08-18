package xyz.haff.siths.client

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

    operator fun plus(other: RedisCommand) = RedisCommand(this.parts + other.parts)
}