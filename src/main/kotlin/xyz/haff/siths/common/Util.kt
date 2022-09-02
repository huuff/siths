package xyz.haff.siths.common

import java.util.*

fun randomUUID() = UUID.randomUUID().toString()

// TODO: This in koy
// OPT: Prevent copying the array
fun <T> Array<T>.headAndTail(): Pair<T, Array<T>> = Pair(this[0], this.copyOfRange(1, this.size))

// XXX: As checked as we can get with arrays
@Suppress("UNCHECKED_CAST")
fun <T> Iterable<T>.headAndTail(): Pair<T, Array<T>> {
    val tail = mutableListOf<T>()
    val iterator = this.iterator()
    if (!iterator.hasNext()) { throw IllegalArgumentException("Must have at least one element to get head!") }

    val head = iterator.next()
    while (iterator.hasNext()) { tail += iterator.next() }

    return Pair(head, tail.stream().toArray() as Array<T>)
}

// TODO: Also in koy
fun <A, B, C> Pair<A, B>.mapSecond(f: (B) -> C): Pair<A, C> = Pair(first, f(second))