package com.formula.flows

import co.paralleluniverse.fibers.Suspendable
import com.formula.contracts.F1XBalanceContract
import com.formula.contracts.F1XBalanceContract.F1XBalanceState
import com.formula.contracts.F1XBalanceContract.F1XCommand
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.FinalityFlow
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.StartableByRPC
import net.corda.core.identity.Party
import net.corda.core.node.services.vault.QueryCriteria.LinearStateQueryCriteria
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import java.math.BigDecimal
import java.util.UUID


@StartableByRPC
class F1XIssueFlow(
    private val owner: Party,
    private val amount: BigDecimal
) : FlowLogic<Unit>() {
    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call(): Unit {
        val currentBalanceStateAndRef = serviceHub.vaultService.queryBy(
            F1XBalanceState::class.java, LinearStateQueryCriteria(
                linearId = listOf(owner.toF1XAccountId())
            )
        ).states.singleOrNull()

        val newBalanceState = F1XBalanceState(
            linearId = owner.toF1XAccountId(),
            amount = (currentBalanceStateAndRef?.state?.data?.amount ?: BigDecimal.ZERO).add(amount),
            owner = owner
        )

        val builder = TransactionBuilder(serviceHub.notary())
        currentBalanceStateAndRef?.let { builder.addInputState(it) }
        builder.addCommand(F1XCommand.Issue())
        builder.addOutputState(newBalanceState)

        builder.verify(serviceHub)
        val ptx = serviceHub.signInitialTransaction(builder)

        subFlow(FinalityFlow(ptx, emptyList()))
    }
}

fun Party.toF1XAccountId() = UniqueIdentifier(F1XBalanceContract.ID, UUID.nameUUIDFromBytes(owningKey.encoded))
