package xyz.haff.siths.jedis

import redis.clients.jedis.JedisPool
import java.time.Duration
import java.util.zip.CRC32

data class CacheResult<T>(val contents: T, val hit: Boolean, val keyHash: String)
class RedisCache<Key, Value>(
    private val lockTimeout: Duration,
    private val keyTtl: Duration,
    private val jedisPool: JedisPool,
    private val name: String = "cache",
    private val serializingFunction: (Value) -> String,
    private val deserializingFunction: (String) -> Value,
) {

     fun getOrLoad(key: Key, loadingFunction: (Key) -> Value): CacheResult<Value> {
        val keyHash = CRC32().apply { update(key.toString().toByteArray()) }.value.toString()
        val keyName = "$name:$keyHash"

        return jedisPool.resource.use { redis ->
            redis.withLock<CacheResult<Value>>(keyName, lockTimeout) {
                val storedValue = redis[keyName]
                if (storedValue != null) {
                    return CacheResult(deserializingFunction(storedValue), true, keyHash)
                } else {
                    val value = loadingFunction(key)
                    redis.setWithParams(key = keyName, value = serializingFunction(value), expiration = keyTtl)
                    return CacheResult(value, false, keyHash)
                }
            }
        }
    }
}