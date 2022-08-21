package xyz.haff.siths.client

data class RedisCursor<T>(
    val next: Long,
    val contents: T,
)
