package com.formula.flows

import co.paralleluniverse.fibers.Suspendable
import com.formula.contracts.F1ShareBalanceContract
import com.formula.contracts.F1ShareBalanceContract.F1ShareBalanceState
import com.formula.contracts.F1ShareBalanceContract.F1ShareCommand
import com.formula.contracts.ShareBalance
import com.formula.contracts.ShareId
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.FinalityFlow
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.StartableByRPC
import net.corda.core.identity.Party
import net.corda.core.node.services.vault.QueryCriteria.LinearStateQueryCriteria
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import java.util.UUID


@StartableByRPC
class IssueF1SharesIssueFlow(
    private val owner: Party,
    private val governanceBody: Party,
    private val shareId: ShareId,
    private val amount: Int
) : FlowLogic<Unit>() {
    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call() {
        val currentBalanceStateAndRef = serviceHub.vaultService.queryBy(
            F1ShareBalanceState::class.java, LinearStateQueryCriteria(
                linearId = listOf(owner.toShareAccountId())
            )
        ).states.singleOrNull()

        val newBalanceState = F1ShareBalanceState(
            linearId = owner.toShareAccountId(),
            balances = currentBalanceStateAndRef.addBalance(shareId, amount),
            owner = owner,
            governanceBody = governanceBody
        )

        val builder = TransactionBuilder(serviceHub.notary())
        currentBalanceStateAndRef?.let { builder.addInputState(it) }
        builder.addCommand(F1ShareCommand.Issue())
        builder.addOutputState(newBalanceState)

        builder.verify(serviceHub)
        val ptx = serviceHub.signInitialTransaction(builder)

        subFlow(FinalityFlow(ptx, emptyList()))
    }
}

fun Party.toShareAccountId() = UniqueIdentifier(F1ShareBalanceContract.ID, UUID.nameUUIDFromBytes(owningKey.encoded))

fun StateAndRef<F1ShareBalanceState>?.addBalance(shareId: ShareId, amount: Int): Set<ShareBalance> {
    val balances = this?.state?.data?.balances ?: emptySet()
    val currentBalance = balances.firstOrNull { it.shareId == shareId } ?: ShareBalance(shareId, 0)
    return (balances.filter { it.shareId != shareId } + listOf(currentBalance.copy(amount = currentBalance.amount + amount))).toSet()
}
