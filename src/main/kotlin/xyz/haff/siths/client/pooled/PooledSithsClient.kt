package xyz.haff.siths.client.pooled

import xyz.haff.siths.client.api.SithsClient
import xyz.haff.siths.common.randomUUID
import xyz.haff.siths.pooling.Pool
import xyz.haff.siths.pooling.PoolStatus
import xyz.haff.siths.pooling.PooledResource

class PooledSithsClient(
    override val resource: SithsClient,
    override val pool: Pool<SithsClient, PooledSithsClient>,
    override var status: PoolStatus,
    override val identifier: String = randomUUID(),
): PooledResource<SithsClient>, SithsClient by resource