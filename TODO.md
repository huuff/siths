# Tasks
## Ops
* Use SLF4J to do some debug logging
* Setup CircleCI to publish to maven central 
* Set `codecov.io`

## Siths
* Provide the conversion of the type response in `RedisCommandBuilder`, which would mean that both `StandaloneSithsClient` and `PipelinedSithsClient` would have to do much less work (in the case of the pipelined client, just convert to a `DeferredCommand`)
* Provide async (coroutines based) counterparts to data structures? Sync data structures would just call these, wrapped in `runBlocking`. However, the biggest issue I have with this solution is that there's no way for async data structures to implement the Java collection interfaces.
