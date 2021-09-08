package com.formula.contracts

import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.transactions.LedgerTransaction

class F1ShareBalanceContract : Contract {
    companion object {
        const val ID = "com.formula.contracts.F1ShareBalanceContract"
    }

    data class F1ShareBalanceState(
        override val linearId: UniqueIdentifier,
        val owner: AbstractParty,
        val amount: Int
    ) : LinearState {
        override val participants: List<AbstractParty>
            get() = listOf(owner)
    }

    override fun verify(tx: LedgerTransaction) {
        // do nothing
    }

    // Used to indicate the transaction's intent.
    sealed class F1ShareCommand : CommandData {
        class Issue : F1ShareCommand()
        class Transfer : F1ShareCommand()
    }
}