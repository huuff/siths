package xyz.haff.siths.client

import xyz.haff.siths.common.randomUUID

class PooledSithsClient(
    override val resource: SithsClient,
    override val pool: Pool<SithsClient, PooledSithsClient>,
    override var status: PoolStatus,
    override val identifier: String = randomUUID(),
): PooledResource<SithsClient>, SithsClient by resource