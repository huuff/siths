package xyz.haff.siths.client

data class RedisConnection(
    val host: String = "localhost",
    val port: Int = 6379,
    val user: String? = null,
    val password: String? = null,
)
