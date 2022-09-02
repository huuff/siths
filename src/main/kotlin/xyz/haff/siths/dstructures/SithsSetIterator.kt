package xyz.haff.siths.dstructures

import kotlinx.coroutines.runBlocking
import xyz.haff.siths.client.api.SithsClient
import xyz.haff.siths.client.api.SithsImmediateClient
import xyz.haff.siths.protocol.RedisCursor

class SithsSetIterator<T>(
    private var lastCursorResult: RedisCursor<T>,
    private val client: SithsImmediateClient
) : MutableIterator<T> {
    private var positionWithinLastCursor = 0
    private var lastResult: T? =
        null // XXX: Only to implement `remove`... it's hacky but all of my other options were too

    override fun hasNext(): Boolean =
        lastCursorResult.next != 0L || positionWithinLastCursor < lastCursorResult.contents.size

    override fun next(): T = lastCursorResult.contents[positionWithinLastCursor].also {
        lastResult = it
        positionWithinLastCursor++
        // XXX: Overflowed the last cursor, but there are more elements
        if (positionWithinLastCursor >= lastCursorResult.contents.size && hasNext()) {
            lastCursorResult = runBlocking { client.sscan(name, lastCursorResult.next).map(deserialize) }
            positionWithinLastCursor = 0
        }
    }

    override fun remove() {
        if (lastResult != null) {
            // XXX: Non-null assertion is correct, since if `lastResult` has changed, it must have changed to a non-null value
            // (see `next()`)
            runBlocking { client.srem(name, serialize(lastResult!!)) }
        }
    }
}