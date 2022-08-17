package xyz.haff.siths.client

import io.ktor.network.sockets.*

// TODO: Store everything?
data class RedisClient(
    val id: String,
    val name: String,
    val addr: InetSocketAddress,
    val laddr: InetSocketAddress,
    val fd: Int,
    // TODO: Should the following two be durations?
    val age: Int,
    val idle: Int,
    val flags: String, // TODO: Is there a better representation?
    val db: Int,
    val sub: Int,
    val psub: Int,
    val ssub: Int,
    val multi: Int,
    val qbuf: Int,
    val qbufFree: Int,
    val argvMem: Int,
    val multiMem: Int,
    val rbs: Int,
    val rbp: Int,
    val obl: Int,
    val oll: Int,
    val omem: Int,
    val totMem: Int,
    val events: String,
    val cmd: String,
    val user: String,
    val redir: Int,
    val resp: Int,
)

private fun stringToAddr(string: String) = string.split(":").let { (ip, port) -> InetSocketAddress(ip, port.toInt()) }
private val SPACES_REGEX = Regex("\\s+")
// TODO: Test
fun parseClientList(response: String): List<RedisClient> = response.split("\n")
    .asSequence()
    .filter { it.isNotBlank() }
    .map { it.split(SPACES_REGEX)}
    .map { pairs ->
        mutableMapOf<String, String>().apply {
            pairs.forEach { pair ->
                this +=  pair.split("=").let { (key, value) -> key to value } // TODO: What to do with empty fields?
            }
        }
    }
    .map { map -> RedisClient(
        id = map["id"]!!,
        addr = stringToAddr(map["addr"]!!),
        laddr = stringToAddr(map["laddr"]!!),
        fd = map["fd"]!!.toInt(),
        name = map["name"]!!,
        age = map["age"]!!.toInt(),
        idle = map["idle"]!!.toInt(),
        flags = map["flags"]!!,
        db = map["db"]!!.toInt(),
        sub = map["sub"]!!.toInt(),
        psub = map["psub"]!!.toInt(),
        ssub = map["ssub"]!!.toInt(),
        multi = map["multi"]!!.toInt(),
        qbuf = map["qbuf"]!!.toInt(),
        qbufFree = map["qbuf-free"]!!.toInt(),
        argvMem = map["argv-mem"]!!.toInt(),
        multiMem = map["multi-mem"]!!.toInt(),
        rbs = map["rbs"]!!.toInt(),
        rbp = map["rbp"]!!.toInt(),
        obl = map["obl"]!!.toInt(),
        oll = map["oll"]!!.toInt(),
        omem = map["omem"]!!.toInt(),
        totMem = map["tot-mem"]!!.toInt(),
        events = map["events"]!!,
        cmd = map["cmd"]!!,
        user = map["user"]!!,
        redir = map["redir"]!!.toInt(),
        resp = map["resp"]!!.toInt()
    )
    }
    .toList()
