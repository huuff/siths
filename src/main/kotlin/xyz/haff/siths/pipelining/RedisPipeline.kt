package xyz.haff.siths.pipelining

import xyz.haff.siths.command.RedisCommand

data class RedisPipeline(
    val commands: List<RedisCommand> = listOf()
) {
    constructor(vararg commands: RedisCommand?): this(commands.filterNotNull())

    fun toResp() = commands.joinToString(separator = "\r\n") { it.toResp() }

    operator fun plus(command: RedisCommand) = RedisPipeline(commands + command)
    operator fun plus(other: RedisPipeline) = RedisPipeline(this.commands + other.commands)

    override fun toString() = commands.joinToString(separator = "\n") { it.toString() }
}
