package com.formula.flows

import co.paralleluniverse.fibers.Suspendable
import com.formula.contracts.DriverContract.DriverState
import com.formula.contracts.DriverData
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.StartableByRPC

@StartableByRPC
class GetDriversFlow : FlowLogic<List<DriverData>>() {

    @Suspendable
    override fun call(): List<DriverData> {
        val resultStates = serviceHub.vaultService.queryBy(DriverState::class.java).states
        return resultStates.map { it.state.data.toDriverData() }
    }
}
