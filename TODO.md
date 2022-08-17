# Tasks
## Ops
* Use SLF4J to do some debug logging
* Setup CircleCI to publish to maven central 
* Set `codecov.io`

## Jedis tools
* Maybe a builder for a jedis pool

## Siths
* A distributed hash-map that takes a key prefix (i.e. such as `cache`) and implements `MutableMap`, operating on all elements `cache:«key»`
* Transactions, inside a block, with optional `WATCH`ing of keys
* Add authentication at the connection level
* A pipeline builder
* A method to get a list of `RedisClient`s from `Siths`
* Test that we can't go over the max number of connections in a pool because I think we can now
* Test with some huge key or value
