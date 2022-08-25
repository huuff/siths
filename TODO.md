# Tasks
## Ops
* Use SLF4J to do some debug logging
* Setup CircleCI to publish to maven central 
* Set `codecov.io`

## Siths
* Distributed data structures that implement their respective Kotlin interfaces such as `MutableList`, `MutableMap`,  etc.
* As a follow up to the previous one (a different kind of data structure) distributed hash-map that takes a key prefix (i.e. such as `cache`) and implements `MutableMap`, operating on all elements `cache:«key»`
* Pipe dream: A mutable map that uses that one trick I read on Redis' documentation about storing a hashmap entirely on smaller hashmaps with skip encoding.
* Test with some huge key or value
* Test my transaction code with the locking examples of "Redis in Action"
* Implement the Redis in Action semaphore
