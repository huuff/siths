package xyz.haff.siths.client

/**
 * This Siths connection is pooled. Actually, just a decorator around a StandaloneSithsConnection, that instead of
 * closing the underlying socket, only releases it to the pool.
 */
class PooledSithsConnection(
    private val connection: StandaloneSithsConnection,
    private val pool: SithsPool,
) : SithsConnection by connection {

    companion object {
        suspend fun open( pool: SithsPool, host: String = "localhost", port: Int = 6379)
            = PooledSithsConnection(
                connection = StandaloneSithsConnection.open(host = host, port = port),
                pool = pool,
            )
    }

    override fun close() {
        pool.releaseConnection(this)
    }
}