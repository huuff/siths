# Tasks
## Ops
* Use SLF4J to do some debug logging
* Setup CircleCI to publish to maven central 
* Set `codecov.io`

## Siths
* Implement `EXPIREAT` and `EXPIRETIME`
* Implement `MSET` and `MGET`
* Implement `ZSET` operations! I had forgotten
* Implement a distributed counter!
* Implement `MutableMap`.
* What if I simplify complex operations such as those of `SithsSet` or `SithsList` by making them lua scripts?
* I don't really enjoy passing around either `SithsClient`, `SithsClientPool`, `SithsConnectionPool`, etc. It makes for an inconsistent interface. Perhaps I just should pass around `SithsConnectionPool`s for client-facing classes (Such as `SithsClient` and `SithsDSL`)
* I should make any `Any` parameters to `Siths` into `String`s, since the `Any`s confuse me, and the only help they provide is automatically running `.toString()` on arguments... which isn't very helpful for most types. UPDATE: I did this... but now setting a value to a non-string might be more troublesome... what if I just add an overload for every function that inserts element somewhere, that takes also ints?
