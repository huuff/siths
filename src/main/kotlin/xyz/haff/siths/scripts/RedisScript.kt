package xyz.haff.siths.scripts

import org.apache.commons.codec.digest.DigestUtils
import redis.clients.jedis.Jedis
import redis.clients.jedis.exceptions.JedisNoScriptException
import xyz.haff.siths.common.escape

data class RedisScript(
    val code: String,
) {
    val sha: String = DigestUtils.sha1Hex(code)
}