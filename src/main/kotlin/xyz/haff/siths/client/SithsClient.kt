package xyz.haff.siths.client

import kotlin.time.Duration

interface SithsClient : Siths<Unit, String, RespType<*>, Long, List<RedisClient>, Duration, Set<String>, Boolean> {

    suspend fun getOrNull(key: String): String?
}