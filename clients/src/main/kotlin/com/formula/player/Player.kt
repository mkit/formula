package com.formula.player

import com.formula.Args
import com.formula.NodeConnection
import com.formula.myName
import com.formula.player.views.BalanceView
import com.formula.player.views.DriversView
import com.formula.player.views.RaceView
import com.formula.startWebServer
import com.xenomachina.argparser.ArgParser
import net.corda.core.utilities.NetworkHostAndPort
import net.corda.core.utilities.loggerFor

/**
 * Connects to a Corda node via RPC and performs RPC operations on the node.
 *
 * The RPC connection is configured using command line arguments.
 */
fun main(args: Array<String>) = Player().main(args)

class Player {
    companion object {
        val logger = loggerFor<Player>()
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

        startWebServer(parsedArgs.port.toInt(), proxy.myName(), listOf(RaceView(proxy), BalanceView(proxy), DriversView(proxy)))

//        //Close the client connection
//        nodeConnection.close()
    }
}