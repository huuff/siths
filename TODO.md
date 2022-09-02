# Tasks
## Ops
* Use SLF4J to do some debug logging
* Setup CircleCI to publish to maven central 
* Set `codecov.io`

## Siths
* Implement `EXPIREAT` and `EXPIRETIME`
* Implement a distributed counter!
* Implement `MutableMap`.
* What if I simplify complex operations such as those of `SithsSet` or `SithsList` by making them lua scripts?
* I don't really enjoy passing around either `SithsClient`, `SithsClientPool`, `SithsConnectionPool`, etc. It makes for an inconsistent interface. Perhaps I just should pass around `SithsConnectionPool`s for client-facing classes (Such as `SithsClient` and `SithsDSL`)
* Un-camelcase the commands I camelcased?
