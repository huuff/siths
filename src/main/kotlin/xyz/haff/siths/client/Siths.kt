package xyz.haff.siths.client

import xyz.haff.siths.scripts.RedisScript

interface Siths {

    suspend fun set(key: String, value: String): Unit
    suspend fun getOrNull(key: String): String?
    suspend fun get(key: String): String
    suspend fun scriptLoad(script: String): String
    suspend fun evalSha(sha: String, keys: List<String> = listOf(), args: List<String> = listOf()): String
}