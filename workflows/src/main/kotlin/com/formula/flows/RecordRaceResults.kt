package com.formula.flows

import co.paralleluniverse.fibers.Suspendable
import com.formula.contracts.F1ShareBalanceContract.F1ShareBalanceState
import com.formula.contracts.RaceResultContract
import com.formula.contracts.RaceResultContract.RaceResultCommand
import com.formula.contracts.RaceResultContract.RaceResultState
import com.formula.contracts.RaceResults
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.FinalityFlow
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.StartableByRPC
import net.corda.core.node.services.vault.QueryCriteria.LinearStateQueryCriteria
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import java.util.UUID

@StartableByRPC
class RecordRaceResults(
    private val raceResults: RaceResults,
    private val raceId: UUID
) : FlowLogic<Unit>() {
    constructor(raceResults: RaceResults) : this(raceResults, UUID.randomUUID())

    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call() {

        val raceResultsState = RaceResultState(
            linearId = UniqueIdentifier(RaceResultContract.ID, raceId),
            raceResults = raceResults,
            governanceBody = ourIdentity
        )

        val builder = TransactionBuilder(serviceHub.notary())
        builder.addCommand(RaceResultCommand.Record(), ourIdentity.owningKey)
        builder.addOutputState(raceResultsState)

        builder.verify(serviceHub)
        val ptx = serviceHub.signInitialTransaction(builder)

        subFlow(FinalityFlow(ptx, emptyList()))

        val partyShareBalances = serviceHub.vaultService.queryBy(F1ShareBalanceState::class.java).states.map {
            it.state.data
        }.filter { partyShareBalances ->
            partyShareBalances.balances.any { shareBalance ->
                shareBalance.shareId in raceResults.orderedResults
            }
        }
    }
}
