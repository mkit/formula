package com.formula.flows

import co.paralleluniverse.fibers.Suspendable
import com.formula.contracts.DriverId
import com.formula.contracts.F1ShareBalanceContract.F1ShareBalanceState
import com.formula.contracts.F1ShareBalanceContract.F1ShareCommand
import com.formula.contracts.F1XBalanceContract.F1XBalanceState
import com.formula.contracts.F1XBalanceContract.F1XCommand
import net.corda.core.contracts.requireThat
import net.corda.core.flows.CollectSignaturesFlow
import net.corda.core.flows.FinalityFlow
import net.corda.core.flows.FlowException
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.FlowSession
import net.corda.core.flows.InitiatedBy
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.ReceiveFinalityFlow
import net.corda.core.flows.SignTransactionFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.identity.CordaX500Name
import net.corda.core.node.services.vault.QueryCriteria.LinearStateQueryCriteria
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import java.math.BigDecimal


@StartableByRPC
@InitiatingFlow
class BuyF1SharesFlow(
    private val governanceBody: CordaX500Name,
    private val shareId: DriverId,
    private val amount: Int
) : FlowLogic<Unit>() {
    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call() {
        val currentBuyerShareBalanceStateAndRef = serviceHub.vaultService.queryBy(
            F1ShareBalanceState::class.java, LinearStateQueryCriteria(
                linearId = listOf(ourIdentity.toShareAccountId())
            )
        ).states.singleOrNull()

        val governorParty = serviceHub.identityService.wellKnownPartyFromX500Name(governanceBody)!!

        val newBuyerShareBalanceState = F1ShareBalanceState(
            linearId = ourIdentity.toShareAccountId(),
            balances = currentBuyerShareBalanceStateAndRef.addBalance(shareId, amount),
            owner = ourIdentity,
            governanceBody = governorParty
        )

        val currentBuyerF1XBalanceStateAndRef = serviceHub.vaultService.queryBy(
            F1XBalanceState::class.java, LinearStateQueryCriteria(
                linearId = listOf(ourIdentity.toF1XAccountId())
            )
        ).states.singleOrNull() ?: throw FlowException("Buyer has no F1X account -> Balance = Zero")

        val newBuyerF1XBalanceState = F1XBalanceState(
            linearId = ourIdentity.toF1XAccountId(),
            amount = currentBuyerF1XBalanceStateAndRef.state.data.amount.minus(
                BigDecimal.valueOf(
                    amount.toLong()
                )
            ),
            owner = ourIdentity,
            note = "Buying $amount shares of $shareId"
        )

        val builder = TransactionBuilder(serviceHub.notary())
        currentBuyerShareBalanceStateAndRef?.let { builder.addInputState(it) }
        builder.addInputState(currentBuyerF1XBalanceStateAndRef)
        builder.addCommand(F1ShareCommand.Transfer(), ourIdentity.owningKey, governorParty.owningKey)
        builder.addCommand(F1XCommand.Transfer(), ourIdentity.owningKey, governorParty.owningKey)
        builder.addOutputState(newBuyerShareBalanceState)
        builder.addOutputState(newBuyerF1XBalanceState)

        builder.verify(serviceHub)
        val ptx = serviceHub.signInitialTransaction(builder)
        val sessions = listOf(initiateFlow(governorParty))
        val stx = subFlow(CollectSignaturesFlow(ptx, sessions))

        subFlow(FinalityFlow(stx, sessions))
    }
}

@InitiatedBy(BuyF1SharesFlow::class)
class BuyF1SharesResponderFlow(val counterpartySession: FlowSession) : FlowLogic<SignedTransaction>() {
    @Suspendable
    override fun call(): SignedTransaction {
        val signTransactionFlow = object : SignTransactionFlow(counterpartySession) {
            override fun checkTransaction(stx: SignedTransaction) = requireThat {
                val output = stx.tx.outputs
                "Transaction has outputs." using (output.isNotEmpty())
            }
        }
        val txId = subFlow(signTransactionFlow).id
        return subFlow(ReceiveFinalityFlow(counterpartySession, expectedTxId = txId))
    }
}
