package com.formula.contracts

import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.serialization.CordaSerializable
import net.corda.core.transactions.LedgerTransaction

@CordaSerializable
data class DriverData(
    val driverId: DriverId,
    val house: String
)

class DriverContract : Contract {
    companion object {
        const val ID = "com.formula.contracts.DriverContract"
    }

    data class DriverState(
        override val linearId: UniqueIdentifier,
        val driverId: DriverId,
        val house: Party
    ) : LinearState {
        override val participants: List<AbstractParty>
            get() = listOf(house)

        fun toDriverData(): DriverData = DriverData(driverId, house.name.toString())
    }

    override fun verify(tx: LedgerTransaction) {
        // do nothing
    }

    // Used to indicate the transaction's intent.
    sealed class DriverCommand : CommandData {
        class Issue : DriverCommand()
        class Transfer : DriverCommand()
    }
}
