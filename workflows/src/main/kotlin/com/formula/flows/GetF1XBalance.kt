package com.formula.flows

import co.paralleluniverse.fibers.Suspendable
import com.formula.contracts.F1XBalanceContract.F1XBalanceState
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.StartableByRPC
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.QueryCriteria.LinearStateQueryCriteria
import net.corda.core.serialization.CordaSerializable

@CordaSerializable
data class F1XBalanceData(val amount: Int, val note: String)

private fun F1XBalanceState.toF1XBalanceData() = F1XBalanceData(
    amount = this.amount.intValueExact(),
    note = this.note
)

@StartableByRPC
class GetF1XBalanceFlow : FlowLogic<F1XBalanceData>() {

    @Suspendable
    override fun call(): F1XBalanceData {
        val currentBalanceStateAndRef = serviceHub.vaultService.queryBy(
            F1XBalanceState::class.java, LinearStateQueryCriteria(
                linearId = listOf(ourIdentity.toF1XAccountId())
            )
        ).states.singleOrNull()
        return currentBalanceStateAndRef?.state?.data?.toF1XBalanceData() ?: F1XBalanceData(0, "Initial")
    }
}

@StartableByRPC
class GetF1XBalancesFlow : FlowLogic<List<F1XBalanceData>>() {

    @Suspendable
    override fun call(): List<F1XBalanceData> {
        val oldBalances = serviceHub.vaultService.queryBy(
            F1XBalanceState::class.java, LinearStateQueryCriteria(
                status = Vault.StateStatus.ALL
            )
        ).states.map { it.state.data }
        return oldBalances.reversed().let { list ->
            if (list.isNotEmpty()) {
                list.map { it.toF1XBalanceData() } + listOf(F1XBalanceData(0, "Initial"))
            } else {
                list.map { it.toF1XBalanceData() }
            }
        }
    }
}
