package com.formula.contracts

import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.transactions.LedgerTransaction
import java.math.BigDecimal

class F1XBalanceContract : Contract {
    companion object {
        const val ID = "com.formula.contracts.F1XBalanceContract"
    }

    data class F1XBalanceState(
        override val linearId: UniqueIdentifier,
        val owner: AbstractParty,
        val amount: BigDecimal
    ) : LinearState {
        override val participants: List<AbstractParty>
            get() = listOf(owner)
    }

    override fun verify(tx: LedgerTransaction) {
        // do nothing
    }

    // Used to indicate the transaction's intent.
    sealed class F1XCommand : CommandData {
        class Issue : F1XCommand()
        class Transfer : F1XCommand()
    }
}