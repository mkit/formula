package com.formula.governor

import com.formula.Args
import com.formula.NodeConnection
import com.formula.governor.views.RaceView
import com.formula.governor.views.ResultsEnterView
import com.formula.myName
import com.formula.startWebServer
import com.xenomachina.argparser.ArgParser
import net.corda.core.utilities.NetworkHostAndPort
import net.corda.core.utilities.loggerFor

/**
 * Connects to a Corda node via RPC and performs RPC operations on the node.
 *
 * The RPC connection is configured using command line arguments.
 */
fun main(args: Array<String>) = Governor().main(args)

class Governor {
    companion object {
        val logger = loggerFor<Governor>()
    }

    fun main(args: Array<String>) {
        // Create an RPC connection to the node.
        val parsedArgs = ArgParser(args).parseInto(::Args)
        val nodeConnection = NodeConnection(
            NetworkHostAndPort.parse(parsedArgs.address),
            parsedArgs.user,
            parsedArgs.secret
        )
        val proxy = nodeConnection.proxy

        // Interact with the node.
        // Example #1, here we print the nodes on the network.
        val nodes = proxy.networkMapSnapshot()
        println("\n-- Here is the networkMap snapshot --")
        logger.info("{}", nodes)

        // Example #2, here we print the PartyA's node info
        val me = proxy.nodeInfo().legalIdentities.first().name
        println("\n-- Here is the node info of the node that the client connected to --")
        logger.info("{}", me)

        startWebServer(parsedArgs.port.toInt(), proxy.myName(), listOf(
            RaceView(proxy),
            ResultsEnterView(proxy)))

        //Close the client connection
        //nodeConnection.close()
    }
}