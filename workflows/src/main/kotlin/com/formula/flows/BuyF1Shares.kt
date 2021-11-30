package com.formula.flows

import co.paralleluniverse.fibers.Suspendable
import com.formula.contracts.F1ShareBalanceContract.F1ShareBalanceState
import com.formula.contracts.F1ShareBalanceContract.F1ShareCommand
import net.corda.core.flows.FinalityFlow
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.StartableByRPC
import net.corda.core.identity.Party
import net.corda.core.node.ServiceHub
import net.corda.core.node.services.vault.QueryCriteria.LinearStateQueryCriteria
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker


@StartableByRPC
class BuyF1SharesFlow(private val seller: Party,
                      private val buyer: Party,
                      private val unitPrice: Int,
                      private val amount: Int) : FlowLogic<Unit>() {
    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call() {

        val notary = serviceHub.notary()

        val currentBalanceStateAndRef = serviceHub.vaultService.queryBy(
            F1ShareBalanceState::class.java, LinearStateQueryCriteria(
                linearId = listOf(buyer.toId())
            )
        ).states.singleOrNull()

        val newBalanceState = F1ShareBalanceState(
            linearId = buyer.toId(),
            amount = (currentBalanceStateAndRef?.state?.data?.amount ?: 0) + amount,
            owner = buyer
        )

        val builder = TransactionBuilder(notary)
        currentBalanceStateAndRef?.let { builder.addInputState(it) }
        builder.addCommand(F1ShareCommand.Issue())
        builder.addOutputState(newBalanceState)

        builder.verify(serviceHub)
        val ptx = serviceHub.signInitialTransaction(builder)

        subFlow(FinalityFlow(ptx, emptyList()))
    }
}

fun ServiceHub.notary() = networkMapCache.notaryIdentities.first()
