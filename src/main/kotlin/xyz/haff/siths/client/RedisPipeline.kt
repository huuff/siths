package xyz.haff.siths.client

data class RedisPipeline(
    val commands: List<RedisCommand> = listOf()
) {
    constructor(vararg commands: RedisCommand): this(commands.asList())

    fun toResp() = commands.joinToString(separator = "\r\n") { it.toResp() }

    operator fun plus(command: RedisCommand) = RedisPipeline(commands + command)
}
