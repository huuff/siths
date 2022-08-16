package xyz.haff.siths.scripts

import org.apache.commons.codec.digest.DigestUtils

data class RedisScript(
    val code: String,
) {
    val sha: String = DigestUtils.sha1Hex(code)
}