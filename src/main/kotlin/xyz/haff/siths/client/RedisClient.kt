package xyz.haff.siths.client

data class RedisClient(
    val id: String,
    val name: String,
)

private val SPACES_REGEX = Regex("\\s+")
// TODO: Test
fun parseClientList(response: String): List<RedisClient> = response.split("\n")
    .asSequence()
    .filter { it.isNotBlank() }
    .map { it.split(SPACES_REGEX)}
    .map { pairs ->
        val responseAsMap = mutableMapOf<String, String>()
        pairs.forEach { pair -> responseAsMap +=  pair.split("=").let { (key, value) -> key to value } }
        responseAsMap
    }
    .map { map -> RedisClient(id = map["id"]!!, name = map["name"]!!) }
    .toList()
