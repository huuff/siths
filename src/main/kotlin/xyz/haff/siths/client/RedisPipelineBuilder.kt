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

    override suspend fun del(key: String, vararg rest: String) {
        commandList += commandBuilder.del(key, *rest)
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

    override suspend fun exists(key: String, vararg rest: String) {
        commandList += commandBuilder.exists(key, *rest)
    }

    override suspend fun sadd(key: String, value: Any, vararg rest: Any) {
        commandList += commandBuilder.sadd(key, value, *rest)
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

    override suspend fun srem(key: String, member: Any, vararg rest: Any) {
        commandList += commandBuilder.srem(key, member, *rest)
    }

    override suspend fun sintercard(key: String, vararg rest: String, limit: Int?) {
        commandList += commandBuilder.sintercard(key, rest = rest, limit = limit)
    }

    override suspend fun sdiffstore(destination: String, key: String, vararg rest: String) {
        commandList += commandBuilder.sdiffstore(destination, key, *rest)
    }
}