package com.formula.contracts

import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.serialization.CordaSerializable
import net.corda.core.transactions.LedgerTransaction
import java.time.Instant

typealias DriverId = String


@CordaSerializable
data class RaceResults(
    val orderedResults: List<DriverId>,
    val raceStartTime: Instant
) {
    companion object {
        val RACE_POINTS_DISTRIBUTION = listOf(25, 18, 15, 12, 10, 8, 6, 4, 2, 1)
        val TOTAL_RACE_POINTS = RACE_POINTS_DISTRIBUTION.sum()
    }
}

class RaceResultContract : Contract {
    companion object {
        const val ID = "com.formula.contracts.RaceResultContract"
    }

    data class RaceResultState(
        override val linearId: UniqueIdentifier,
        val governanceBody: AbstractParty,
        val raceResults: RaceResults
    ) : LinearState {
        override val participants: List<AbstractParty>
            get() = listOf(governanceBody)
    }

    override fun verify(tx: LedgerTransaction) {
        // do nothing
    }

    // Used to indicate the transaction's intent.
    sealed class RaceResultCommand : CommandData {
        class Record : RaceResultCommand()
    }
}
