# Tasks
## Ops
* Use SLF4J to do some debug logging
* Setup CircleCI to publish to maven central 
* Set `codecov.io`

## Siths
* Implement `PERSIST`
* Implement `EXPIREAT`
* Implement `ZSET` operations! I had forgotten
* Implement a queue with lists!
* Implement a distributed counter!
* Before implementing the `MutableList`, implement ALL list commands
* See if I can some of the official documentation patterns, such as those [here](https://redis.io/commands/lmove/)
* Distributed data structures that implement their respective Kotlin interfaces such as `MutableList`, `MutableMap`,  etc.
* As a follow up to the previous one (a different kind of data structure) distributed hash-map that takes a key prefix (i.e. such as `cache`) and implements `MutableMap`, operating on all elements `cache:«key»`
* Pipe dream: A mutable map that uses that one trick I read on Redis' documentation about storing a hashmap entirely on smaller hashmaps with skip encoding.
* Test with some huge key or value
* Test my transaction code with the locking examples of "Redis in Action"
* Implement the Redis in Action semaphore
* Maybe I should separate the methods that take an optional count and return one or many elements into alternatives? such as `lpopOne` and `lpopMany`. I find it bothersome forcing the user to handle a singleton list
