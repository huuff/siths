package xyz.haff.siths.common

import java.util.*

fun randomUUID() = UUID.randomUUID().toString()

// TODO: This in koy
// OPT: Prevent copying the array
fun <T> Array<T>.headAndTail(): Pair<T, Array<T>> = Pair(this[0], this.copyOfRange(1, this.size))