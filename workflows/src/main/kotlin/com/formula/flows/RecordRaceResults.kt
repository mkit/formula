package com.formula.flows

import co.paralleluniverse.fibers.Suspendable
import com.formula.contracts.F1ShareBalanceContract.F1ShareBalanceState
import com.formula.contracts.RaceResultContract
import com.formula.contracts.RaceResultContract.RaceResultCommand
import com.formula.contracts.RaceResultContract.RaceResultState
import com.formula.contracts.RaceResults
import com.formula.contracts.RaceResults.Companion
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.FinalityFlow
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.FlowSession
import net.corda.core.flows.InitiatedBy
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.ReceiveTransactionFlow
import net.corda.core.flows.SendTransactionFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.node.StatesToRecord
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.unwrap
import java.math.BigDecimal
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import java.util.UUID

@StartableByRPC
@InitiatingFlow
class RecordRaceResultsFlow(
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

        val stx = subFlow(FinalityFlow(ptx, emptyList()))

        val partyShareBalances = serviceHub.vaultService.queryBy(F1ShareBalanceState::class.java).states.map {
            it.state.data
        }.filter { partyShareBalances ->
            partyShareBalances.balances.any { shareBalance ->
                shareBalance.shareId in raceResults.orderedResults
            }
        }
        serviceHub.networkMapCache.allNodes.map { it.legalIdentitiesAndCerts.first().party }
            .filter { it != serviceHub.notary() }.forEach {
                subFlow(SendTransaction(stx, it))
            }
        calculatePayouts(partyShareBalances, raceResults).forEach { (winningShareHolder, payoutAmount) ->
            subFlow(OrderF1XTokenIssuanceFlow(winningShareHolder, payoutAmount))
        }
    }
}

@InitiatingFlow
class OrderF1XTokenIssuanceFlow(private val owner: AbstractParty, private val amount: BigDecimal) : FlowLogic<Unit>() {
    @Suspendable
    override fun call() {
        val session = initiateFlow(owner)
        session.send(amount)
    }
}

private val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
    .withLocale(Locale.UK)
    .withZone(ZoneId.systemDefault())

@InitiatedBy(OrderF1XTokenIssuanceFlow::class)
class OrderF1XTokenIssuanceResponder(
    private val counterpartySession: FlowSession
) : FlowLogic<Unit>() {

    @Suspendable
    override fun call() {
        val amount = counterpartySession.receive<BigDecimal>().unwrap { it }
        subFlow(F1XIssueFlow(amount.toInt(), "Race winnings for owned shares"))
    }
}


@InitiatingFlow
class SendTransaction(val signedTransaction: SignedTransaction, val receiver: Party) : FlowLogic<Unit>() {
    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call() {
        val session = initiateFlow(receiver)
        subFlow(SendTransactionFlow(session, signedTransaction))
    }
}

@InitiatedBy(SendTransaction::class)
class ReportManuallyResponder(
    private val counterpartySession: FlowSession
) : FlowLogic<Unit>() {
    @Suspendable
    override fun call() {
        // The national regulator records all of the transaction's states using
        // `recordTransactions` with the `ALL_VISIBLE` flag.
        subFlow(ReceiveTransactionFlow(counterpartySession, true, StatesToRecord.ALL_VISIBLE))
    }
}

private fun calculatePayouts(
    partyShareBalances: List<F1ShareBalanceState>,
    raceResults: RaceResults
): Map<AbstractParty, BigDecimal> {
    val winningSharesTotals = raceResults.orderedResults.associate { winningDriverId ->
        winningDriverId to partyShareBalances.sumBy { f1ShareBalanceState ->
            f1ShareBalanceState.balances.firstOrNull { it.shareId == winningDriverId }?.amount ?: 0
        }
    }
    return partyShareBalances.associate { f1ShareBalanceState ->
        f1ShareBalanceState.owner to f1ShareBalanceState.balances.fold(BigDecimal.ZERO) { currentPayoutAmount, balance ->
            val (shareId, shareBalanceAmount) = raceResults.orderedResults.indexOf(balance.shareId).takeIf { it > -1 }
                ?.let { place ->
                    balance.shareId to balance.amount
                } ?: return@fold currentPayoutAmount
            val winningTotal = winningSharesTotals[shareId]!!
            val placePoints = Companion.RACE_POINTS_DISTRIBUTION[raceResults.orderedResults.indexOf(balance.shareId)]
            return@fold currentPayoutAmount.add(
                BigDecimal(shareBalanceAmount).multiply(BigDecimal(placePoints)).divide(BigDecimal(winningTotal))
            )
        }
    }
}
