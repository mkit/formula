package com.formula.flows

import co.paralleluniverse.fibers.Suspendable
import com.formula.contracts.RaceResultContract.RaceResultState
import com.formula.contracts.RaceResults
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.StartableByRPC


@StartableByRPC
class GetRaceResultsFlow : FlowLogic<List<RaceResults>>() {

    @Suspendable
    override fun call(): List<RaceResults> {
        val resultStates = serviceHub.vaultService.queryBy(RaceResultState::class.java).states
        return resultStates.map { it.state.data.raceResults }
    }
}
