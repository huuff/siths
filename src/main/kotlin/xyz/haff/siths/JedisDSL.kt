package xyz.haff.siths

import redis.clients.jedis.Jedis
import redis.clients.jedis.params.SetParams
import java.time.Duration

fun Jedis.setWithParams(
    key: String,
    value: String,
    expiration: Duration? = null,
    notExistent: Boolean = false,
): String {
    val params = SetParams()
    if (expiration != null) {
        params.px(expiration.toMillis())
    }

    if (notExistent) {
        params.nx()
    }

    return this.set(key, value, params)
}