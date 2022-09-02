package xyz.haff.siths.protocol

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
    val flags: Flags,
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
                    flags = Flags.fromString(map["flags"]!!),
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

    data class Flags(
        val closeAsap: Boolean = false,
        val blocked: Boolean = false,
        val closeAfterReply: Boolean = false,
        val watchedChanged: Boolean = false,
        val waitingVm: Boolean = false,
        val master: Boolean = false,
        val monitor: Boolean = false,
        val pubsub: Boolean = false,
        val readonly: Boolean = false,
        val replica: Boolean = false,
        val unblocked: Boolean = false,
        val unixSocket: Boolean = false,
        val inMulti: Boolean = false,
        val keysTracking: Boolean = false,
        val trackingInvalid: Boolean = false,
        val broadcastTracking: Boolean = false,
    ) {
        companion object {
            fun fromString(flags: String) = Flags(
                closeAsap = "A" in flags,
                blocked = "b" in flags,
                closeAfterReply = "c" in flags,
                watchedChanged = "d" in flags,
                waitingVm = "i" in flags,
                master = "M" in flags,
                monitor = "O" in flags,
                pubsub = "P" in flags,
                readonly = "r" in flags,
                replica = "S" in flags,
                unblocked = "u" in flags,
                unixSocket = "U" in flags,
                inMulti = "X" in flags,
                keysTracking = "t" in flags,
                trackingInvalid = "R" in flags,
                broadcastTracking = "B" in flags,
            )
        }
    }
}

private fun stringToAddr(string: String) = string.split(":").let { (ip, port) -> InetSocketAddress(ip, port.toInt()) }
private val SPACES_REGEX = Regex("\\s+")
fun parseClientList(response: String): List<RedisClient> = response.split("\n")
    .filter { it.isNotBlank() }
    .map { RedisClient.fromString(it) }
