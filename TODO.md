# Tasks
* Use SLF4J to do some debug logging
* Setup CircleCI to publish to maven central 
* Set `codecov.io`
* Maybe a builder for a jedis pool
* Put up a nice badge to Maven Central
* A class that holds a script and its SHA1, tries to execute it and in case it fails, loads it and executes it. In the best case, the class would calculate the SHA1 without needing to send it to redis.
* Tests are very flaky. I think this is because I use `Thread`s a lot... but what am I to do? Jedis doesn't support any non-blocking model, least of all coroutines.
