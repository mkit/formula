package com.formula.flows

import co.paralleluniverse.fibers.Suspendable
import com.formula.contracts.F1XBalanceContract
import com.formula.contracts.F1XBalanceContract.F1XBalanceState
import com.formula.contracts.F1XBalanceContract.F1XCommand
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.FinalityFlow
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.StartableByRPC
import net.corda.core.identity.AbstractParty
import net.corda.core.node.services.vault.QueryCriteria.LinearStateQueryCriteria
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import java.math.BigDecimal
import java.util.UUID


@StartableByRPC
class F1XIssueFlow(
    private val amount: BigDecimal,
    private val note: String = "Issue",
    private val owner: AbstractParty?
) : FlowLogic<Unit>() {
    constructor(amount: Int) : this(BigDecimal(amount), "Issue", null)
    constructor(amount: Int, note: String) : this(BigDecimal(amount), note, null)

    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call() {
        val ourIdentity = owner ?: serviceHub.myInfo.legalIdentities.single()
        val notary = serviceHub.networkMapCache.notaryIdentities.first()

        val currentBalanceStateAndRef = serviceHub.vaultService.queryBy(
            F1XBalanceState::class.java, LinearStateQueryCriteria(
                linearId = listOf(ourIdentity.toF1XAccountId())
            )
        ).states.singleOrNull()

        val newBalanceState = F1XBalanceState(
            linearId = ourIdentity.toF1XAccountId(),
            amount = (currentBalanceStateAndRef?.state?.data?.amount ?: BigDecimal.ZERO).add(amount),
            owner = ourIdentity,
            note = note
        )

        val builder = TransactionBuilder(notary)
        currentBalanceStateAndRef?.let { builder.addInputState(it) }
        builder.addCommand(F1XCommand.Issue(), ourIdentity.owningKey)
        builder.addOutputState(newBalanceState)

        builder.verify(serviceHub)
        val ptx = serviceHub.signInitialTransaction(builder)

        subFlow(FinalityFlow(ptx, emptyList()))
    }
}

fun AbstractParty.toF1XAccountId() = UniqueIdentifier(F1XBalanceContract.ID, UUID.nameUUIDFromBytes(owningKey.encoded))
