package com.formula

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default

class Args(parser: ArgParser) {
    val address by parser.storing(
        "-a", "-A", "--address",
        help = "Corda Address"
    ).default("localhost:10032")

    val user by parser.storing(
        "-u", "-U", "--user",
        help = "Corda RPC User"
    ).default("user1")

    val secret by parser.storing(
        "-s", "-S", "--secret",
        help = "Corda RPC Password"
    ).default("test")

    val port by parser.storing(
        "-p", "-P", "--port",
        help = "WebServer Port"
    ).default("8030")
}