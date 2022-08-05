package xyz.haff.siths.scripts

import org.apache.commons.codec.digest.DigestUtils
import redis.clients.jedis.Jedis
import redis.clients.jedis.exceptions.JedisNoScriptException

data class RedisScript(
    val code: String,
) {
    val sha: String = DigestUtils.sha1Hex(code)
}