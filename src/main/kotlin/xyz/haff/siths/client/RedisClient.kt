package xyz.haff.siths.client

import io.ktor.network.sockets.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

data class RedisClient(
    val id: String,
    val name: String?,
    val addr: InetSocketAddress,
    val laddr: InetSocketAddress,
    val fd: Int,
    val age: Duration,
    val idle: Duration,
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
) {
    companion object {
        fun fromString(string: String) = string.split(SPACES_REGEX)
            .associate {
                val (key, value) = it.split("=")
                Pair(key, value.ifEmpty { null })
            }.let { map -> RedisClient(
                    id = map["id"]!!,
                    addr = stringToAddr(map["addr"]!!),
                    laddr = stringToAddr(map["laddr"]!!),
                    fd = map["fd"]!!.toInt(),
                    name = map["name"],
                    age = map["age"]!!.toInt().seconds,
                    idle = map["idle"]!!.toInt().seconds,
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
    }
}

private fun stringToAddr(string: String) = string.split(":").let { (ip, port) -> InetSocketAddress(ip, port.toInt()) }
private val SPACES_REGEX = Regex("\\s+")
fun parseClientList(response: String): List<RedisClient> = response.split("\n")
    .filter { it.isNotBlank() }
    .map { RedisClient.fromString(it) }
