package xyz.haff.siths.client.pooled

import xyz.haff.siths.client.api.SithsClient
import xyz.haff.siths.client.StandaloneSithsClient
import xyz.haff.siths.pooling.Pool
import xyz.haff.siths.pooling.PoolStatus
import xyz.haff.siths.protocol.SithsConnectionPool

class SithsClientPool(
    private val connectionPool: SithsConnectionPool,
) : Pool<SithsClient, PooledSithsClient> {
    private val identifiersToClients = mutableMapOf<String, PooledSithsClient>()

    override val currentResources: Int get() = identifiersToClients.size

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
            it.status = PoolStatus.FREE
        }
    }

    override fun remove(resourceIdentifier: String) {
        identifiersToClients - resourceIdentifier
    }

}