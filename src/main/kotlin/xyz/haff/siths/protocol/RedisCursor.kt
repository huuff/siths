package xyz.haff.siths.protocol

data class RedisCursor<T>(
    val next: Long,
    val contents: List<T>,
) {

    fun <R> map(f: (T) -> R): RedisCursor<R> = RedisCursor(next, contents.map(f))
}
