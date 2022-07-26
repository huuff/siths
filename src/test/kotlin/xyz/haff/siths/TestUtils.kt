package xyz.haff.siths

import org.testcontainers.containers.GenericContainer
import redis.clients.jedis.JedisPool

fun poolFromContainer(container: GenericContainer<Nothing>) = JedisPool(container.host, container.firstMappedPort)