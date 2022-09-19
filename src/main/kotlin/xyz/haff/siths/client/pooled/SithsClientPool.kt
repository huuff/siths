package xyz.haff.siths.client.pooled

import xyz.haff.siths.client.api.SithsImmediateClient
import xyz.haff.siths.client.StandaloneSithsClient
import xyz.haff.siths.pooling.Pool
import xyz.haff.siths.pooling.PoolStatus
import xyz.haff.siths.protocol.SithsConnectionPool

// TODO: Am I not removing broken clients here?
class SithsClientPool(
    private val connectionPool: SithsConnectionPool,
) : Pool<SithsImmediateClient, PooledSithsClient> {
    private val identifiersToClients = mutableMapOf<String, PooledSithsClient>()

    override val currentResources: Int get() = identifiersToClients.values.count { it.status != PoolStatus.BROKEN }

    override suspend fun get(): PooledSithsClient {
        val connection = connectionPool.get()

        return identifiersToClients[connection.identifier]?.also { existingClient ->
            existingClient.status = PoolStatus.BUSY
        } ?: PooledSithsClient(
            resource = StandaloneSithsClient(connection),
            pool = this,
            identifier = connection.identifier,
            status = PoolStatus.BUSY
        ).also {
            identifiersToClients[connection.identifier] = it
        }
    }

    override fun release(resourceIdentifier: String) {
        connectionPool.release(resourceIdentifier)

        identifiersToClients[resourceIdentifier]?.let {
            if (it.status == PoolStatus.BUSY) {
                it.status = PoolStatus.FREE
            }
        }
    }

    override fun remove(resourceIdentifier: String) {
        identifiersToClients -= resourceIdentifier
    }

}