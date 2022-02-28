package com.formula.flows

import co.paralleluniverse.fibers.Suspendable
import com.formula.contracts.DriverContract.Companion.ID
import com.formula.contracts.DriverContract.DriverCommand.Issue
import com.formula.contracts.DriverContract.DriverState
import com.formula.contracts.DriverId
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.FinalityFlow
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.StartableByRPC
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import java.util.UUID


@StartableByRPC
class DriverIssueFlow(private val driverId: DriverId) : FlowLogic<Unit>() {

    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call() {
        val issuingHouse = serviceHub.myInfo.legalIdentities.single()
        val notary = serviceHub.networkMapCache.notaryIdentities.first()

        val newDriver = DriverState(
            UniqueIdentifier(ID, UUID.nameUUIDFromBytes(driverId.toByteArray())),
            driverId,
            issuingHouse
        )

        val builder = TransactionBuilder(notary)
        builder.addCommand(Issue(), ourIdentity.owningKey)
        builder.addOutputState(newDriver)

        builder.verify(serviceHub)
        val ptx = serviceHub.signInitialTransaction(builder)

        val stx = subFlow(FinalityFlow(ptx, emptyList()))

        serviceHub.networkMapCache.allNodes.map { it.legalIdentitiesAndCerts.first().party }
            .filter { it != serviceHub.notary() }.forEach {
                subFlow(SendTransaction(stx, it))
            }
    }
}
