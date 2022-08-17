package xyz.haff.siths.client

data class RedisPipeline(
    var commands: List<RedisCommand> = listOf()
) {
    constructor(vararg commands: RedisCommand): this(commands.asList())

    fun toResp() = commands.map { it.toResp() }.joinToString(separator = "\r\n")
}