package com.formula.flows

import co.paralleluniverse.fibers.Suspendable
import com.formula.contracts.F1ShareBalanceContract.F1ShareBalanceState
import com.formula.contracts.F1ShareBalanceContract.F1ShareCommand
import com.formula.contracts.F1XBalanceContract.F1XBalanceState
import com.formula.contracts.F1XBalanceContract.F1XCommand
import com.formula.contracts.ShareId
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
import net.corda.core.identity.Party
import net.corda.core.node.services.vault.QueryCriteria.LinearStateQueryCriteria
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import java.math.BigDecimal


@StartableByRPC
@InitiatingFlow
class BuyF1SharesFlow(
    private val buyer: Party,
    private val governanceBody: Party,
    private val unitPrice: BigDecimal,
    private val shareId: ShareId,
    private val amount: Int
) : FlowLogic<Unit>() {
    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call() {

        val currentBuyerShareBalanceStateAndRef = serviceHub.vaultService.queryBy(
            F1ShareBalanceState::class.java, LinearStateQueryCriteria(
                linearId = listOf(buyer.toShareAccountId())
            )
        ).states.singleOrNull()

        val newBuyerShareBalanceState = F1ShareBalanceState(
            linearId = buyer.toShareAccountId(),
            balances = currentBuyerShareBalanceStateAndRef.addBalance(shareId, amount),
            owner = buyer,
            governanceBody = governanceBody
        )

        val currentBuyerF1XBalanceStateAndRef = serviceHub.vaultService.queryBy(
            F1XBalanceState::class.java, LinearStateQueryCriteria(
                linearId = listOf(buyer.toF1XAccountId())
            )
        ).states.singleOrNull() ?: throw FlowException("Buyer has no F1X account -> Balance = Zero")

        val newBuyerF1XBalanceState = F1XBalanceState(
            linearId = buyer.toF1XAccountId(),
            amount = currentBuyerF1XBalanceStateAndRef.state.data.amount.minus(
                unitPrice.multiply(
                    BigDecimal.valueOf(
                        amount.toLong()
                    )
                )
            ),
            owner = buyer
        )

        val builder = TransactionBuilder(serviceHub.notary())
        currentBuyerShareBalanceStateAndRef?.let { builder.addInputState(it) }
        builder.addInputState(currentBuyerF1XBalanceStateAndRef)
        builder.addCommand(F1ShareCommand.Transfer(), ourIdentity.owningKey, buyer.owningKey)
        builder.addCommand(F1XCommand.Transfer(), ourIdentity.owningKey, buyer.owningKey)
        builder.addOutputState(newBuyerShareBalanceState)
        builder.addOutputState(newBuyerF1XBalanceState)

        builder.verify(serviceHub)
        val ptx = serviceHub.signInitialTransaction(builder)
        val sessions = listOf(initiateFlow(governanceBody))
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
                //Addition checks
            }
        }
        val txId = subFlow(signTransactionFlow).id
        return subFlow(ReceiveFinalityFlow(counterpartySession, expectedTxId = txId))
    }
}