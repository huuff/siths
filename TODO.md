# Tasks
## Ops
* Use SLF4J to do some debug logging
* Setup CircleCI to publish to maven central 
* Set `codecov.io`
* Put up a nice badge to Maven Central

## Jedis tools
* Maybe a builder for a jedis pool

## Siths
* String escaping? At least ensure injection attacks are not possible
* A distributed hash-map that takes a key prefix (i.e. such as `cache`) and implements `MutableMap`, operating on all elements `cache:«key»`
* Allow working with the `RedisScript` class.
* Do a benchmark between Jedis and Siths.
