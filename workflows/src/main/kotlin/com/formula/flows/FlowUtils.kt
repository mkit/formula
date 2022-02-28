package com.formula.flows

import net.corda.core.node.ServiceHub

fun ServiceHub.notary() = networkMapCache.notaryIdentities.first()
