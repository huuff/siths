package xyz.haff.siths.client

import kotlin.time.Duration

class RedisPipelineBuilder(
    private val commandBuilder: RedisCommandBuilder = RedisCommandBuilder(),
): Siths<Unit, Unit, Unit, Unit, Unit, Unit, Unit, Unit> {
    private val commandList = mutableListOf<RedisCommand>()
    val length get() = commandList.size

    fun build() = RedisPipeline(commandList)

    override suspend fun set(key: String, value: Any, exclusiveMode: ExclusiveMode?, timeToLive: Duration?) {
        commandList += commandBuilder.set(key, value, exclusiveMode, timeToLive)
    }

    override suspend fun get(key: String) {
        commandList += commandBuilder.get(key)
    }

    override suspend fun ttl(key: String) {
        commandList += commandBuilder.ttl(key)
    }

    override suspend fun scriptLoad(script: String) {
        commandList += commandBuilder.scriptLoad(script)
    }

    override suspend fun evalSha(sha: String, keys: List<String>, args: List<String>) {
        commandList += commandBuilder.evalSha(sha, keys, args)
    }

    override suspend fun incrBy(key: String, value: Long) {
        commandList += commandBuilder.incrBy(key, value)
    }

    override suspend fun clientList() {
        commandList += commandBuilder.clientList()
    }

    override suspend fun eval(script: String, keys: List<String>, args: List<String>) {
        commandList += commandBuilder.eval(script, keys, args)
    }

    override suspend fun sadd(key: String, value: Any) {
        commandList += commandBuilder.sadd(key, value)
    }

    override suspend fun smembers(key: String) {
        commandList += commandBuilder.smembers(key)
    }

    override suspend fun sismember(key: String, member: Any) {
        commandList += commandBuilder.sismember(key, member)
    }

    override suspend fun scard(key: String) {
        commandList += commandBuilder.scard(key)
    }
}