package xyz.haff.siths.client.pooled

import xyz.haff.siths.client.api.SithsImmediateClient
import xyz.haff.siths.common.randomUUID
import xyz.haff.siths.pooling.Pool
import xyz.haff.siths.pooling.PoolStatus
import xyz.haff.siths.pooling.PooledResource

class PooledSithsClient(
    override val resource: SithsImmediateClient,
    override val pool: Pool<SithsImmediateClient, PooledSithsClient>,
    override var status: PoolStatus,
    override val identifier: String = randomUUID(),
): PooledResource<SithsImmediateClient>, SithsImmediateClient by resource