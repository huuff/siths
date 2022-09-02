# Architecture
(Not really an in-depth architecture overview, but some rationale behind my decisions)

I try to mess as little as possible my layers of abstraction, so each one builds on top of the below one to provide some features. So in order from lowest to highest-level:

* A `SithsConnection` only takes `RedisCommand`s, sends them to Redis, receives the result and parses it into a `RespType`
* A `RedisCommandReceiver` is an interface that maps each Redis command to a Kotlin method, it receives a lot of generics so an implementer can decide the return types. This is not a strictly one-to-one mapping: commands that might return several different types depending on arguments are mapped to different methods. For example, `LPOP` returns a list or an array depending on whether a count was provided or not.
* A `SithsClient` provides an interface that maps each command to a method, runs it through the `SithsConnection` and transforms the response into an appropriate Kotlin/Java structure. I try to keep the logic low though, and limit it to the conversion to Kotlin. A `SithsClient` might also provide some convenience methods that make them more comfortable to use for clients (such as `getOrNull`)
* A `SithsDSL` is the highest abstraction level: it provides a `SithsClient` interface along with more high-level utilities such as locks, or scripts.

Also, there's a reason I name some interfaces as `I«implementation's name»`. Normally, I'd choose to end the implementation's name with `Impl` instead (or better, something descriptive). But for this case, these interfaces are not meant to be used by the user, so I think that adding an `Impl` every time would encumber them.
