# Siths
*As in, "The opposite of Jedis"*

A collection of extension functions and utilities for Jedis.

### A small note on Jedis
Note that I don't particularly endorse Jedis and even advise against it. I'm writing these in a pinch because I'm stuck with Jedis for a while at work. Some of my complaints about Jedis are:

* Fundamentally blocking, so not especially suited for coroutines.
* Too low-level interface (e.g. returning "OK" strings for successful operations).
* Cumbersome interface with loads of method overloads which surprisingly always have all the overloads but the one you need right now.

Not to be too hard on Jedis, the low-level interface makes it easier to understand the underlying RESP operations and overloaded methods are the only way of providing methods with highly variable parameters in Java. I just don't think it's suited for Kotlin

## Utilities
* Distributed locking (through the `Jedis.acquireLock`, `Jedis.releaseLock`  and `Jedis.withLock` extension methods), mostly copied from *Redis In Action* by Josiah L. Carlson
* Loading and executing Lua scripts through the `RedisScript` class and `Jedis.runScript` extension methods. These ensure the script is loaded on first use and subsequently called only by SHA1 hash.
* "DSL" which is actually a bunch of extension methods that wrap more cumbersome operations
  * `withMulti` and `withWatch` to wrap transactions ensuring they are handled correctly
  * `hasExpiration` and `setExpiration` to allow working with expiration times with JDK classes.
  * `setWithParams` to provide with named and default arguments what Jedis does through builders and overloaded methods.
