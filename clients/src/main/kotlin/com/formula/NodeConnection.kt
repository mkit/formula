package com.formula

import net.corda.client.rpc.CordaRPCClient
import net.corda.client.rpc.CordaRPCConnection
import net.corda.client.rpc.GracefulReconnect
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.utilities.NetworkHostAndPort
import org.slf4j.LoggerFactory

class NodeConnection(
    networkHostAndPort: NetworkHostAndPort,
    user: String,
    password: String
) {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }

    val proxy: CordaRPCOps

    private val connection: CordaRPCConnection

    init {
        val client = CordaRPCClient(networkHostAndPort)
        connection = client.start(
            user,
            password,
            GracefulReconnect(
                { logger.error("disconnected") },
                { logger.info("reconnected") },
                5
            )
        )
        proxy = connection.proxy
        logger.info("KIT: Connected")
    }

    fun close() = connection.close().also {
        logger.info("KIT: disconnected")
    }

}