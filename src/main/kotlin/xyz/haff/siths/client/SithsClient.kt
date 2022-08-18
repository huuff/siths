package xyz.haff.siths.client

import kotlin.time.Duration

interface SithsClient : Siths<Unit, String, RespType<*>, Long, List<RedisClient>, Duration> {

    suspend fun getOrNull(key: String): String?
}