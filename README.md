[![Maven Central](https://maven-badges.herokuapp.com/maven-central/xyz.haff/siths/badge.svg?style=flat)](https://search.maven.org/artifact/xyz.haff/siths)

# Siths
*As in, "The opposite of Jedis"*

Asynchronous, coroutines-based Redis client that tries to offer a sensible DSL.

## Redis API translation layer
A `SithsClient` is simply a thin translation layer between Kotlin and Redis. It makes the appropriate requests to Redis and parses the RESP responses into Kotlin types. Usual Redis commands such as `get`, `set`, `lpush`, etc. are readily available but also:

* Commands are camel-cased to make them look more Kotlin-native (e.g. `incrByFloat`)
* Arguments and responses try to be the most ergonomic Kotlin types, for example, returning a `Duration` instead of a number of seconds for `ttl`. This means that commands that are mostly the same but returning slightly different types are merged (such as `ttl` and `pttl`).
* Some other convenience methods are added, such as `getOrNull`, `setAny`, etc.

## Siths DSL
The Siths DSL wraps functionality from lower layers to provide a cleaner interface and remove syntactic distractions. At the most basic level, `withRedis` creates the appropriate clients and signifies that a block is executed in the context of a connection to Redis:

```kotlin
withRedis(connectionPool) {
  set("key", "value")

  val value = get("value")
}
```

Furthermore, utility functions are given to automatically wrap and execute a pipeline and transaction, returning the last result in a block:

```kotlin
dsl.transactional {
  val lengthBeforeRemoval = llen("list")
  lrem("list", "elem")

  lengthBeforeRemoval
}
```

Some higher-level utilities are also given, such as wrapping a block in a distributed lock acquire-and-release:

```kotlin
// Prevent that several deployed instances find a key missing and try to fill it simultaneously,
// performing an expensive computation
val elem = getOrNull("key") 

if (elem == null) {
  dsl.withLock("lock name") {
    set("key", computeValue())
  }
}
```

## Distributed data structures
Note that since these structures inherit the respective Java collections interfaces, they are blocking.

* `SithsSet` implements `MutableSet`
* `SithsList` implements `MutableList`
* `SithsMap` implements `MutableMap`

These also take serialization and deserialization functions to transparently convert them to/from the appropriate Redis types.
