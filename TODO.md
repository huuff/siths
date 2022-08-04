# Tasks
## Ops
* Use SLF4J to do some debug logging
* Setup CircleCI to publish to maven central 
* Set `codecov.io`
* Put up a nice badge to Maven Central

## Jedis tools
* Maybe a builder for a jedis pool
* Tests are very flaky. I think this is because I use `Thread`s a lot... but what am I to do? Jedis doesn't support any non-blocking model, least of all coroutines.

## Siths
* String escaping? At least ensure injection attacks are not possible
* Connection pooling implementation
* A distributed hash-map that takes a key prefix (i.e. such as `cache`) and implements `MutableMap`, operating on all elements `cache:«key»`
