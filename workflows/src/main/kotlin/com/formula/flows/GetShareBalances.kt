package com.formula.flows

import co.paralleluniverse.fibers.Suspendable
import com.formula.contracts.F1ShareBalanceContract.F1ShareBalanceState
import com.formula.contracts.OwnerShareBalance
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.StartableByRPC


@StartableByRPC
class GetShareBalancesFlow : FlowLogic<List<OwnerShareBalance>>() {

    @Suspendable
    override fun call(): List<OwnerShareBalance> {
        val shareBalanceStates = serviceHub.vaultService.queryBy(F1ShareBalanceState::class.java).states
        return shareBalanceStates.map {
            it.state.data.let { state ->
                OwnerShareBalance(
                    state.owner.name.toString(),
                    state.balances
                )
            }
        }
    }
}
