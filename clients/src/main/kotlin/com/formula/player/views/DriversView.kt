package com.formula.player.views

import com.formula.contracts.DriverData
import com.formula.contracts.RaceResults
import com.formula.contracts.ShareBalance
import com.formula.flows.BuyF1SharesFlow
import com.formula.flows.GetDriversFlow
import com.formula.flows.GetShareBalancesFlow
import com.formula.flows.RecordRaceResultsFlow
import com.formula.fullHeight
import com.formula.governor.views.ResultsEnterView
import com.formula.governor.views.ResultsEnterView.Companion
import com.formula.iconClass
import com.formula.padding
import com.formula.views.ComponentView
import com.formula.width
import com.ibm.icu.math.BigDecimal
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kweb.ButtonType.submit
import kweb.ElementCreator
import kweb.InputElement
import kweb.InputType
import kweb.SelectElement
import kweb.button
import kweb.div
import kweb.h2
import kweb.i
import kweb.input
import kweb.new
import kweb.option
import kweb.plugins.fomanticUI.fomantic
import kweb.select
import kweb.state.KVar
import kweb.state.render
import kweb.table
import kweb.tbody
import kweb.td
import kweb.th
import kweb.thead
import kweb.tr
import net.corda.core.identity.CordaX500Name
import net.corda.core.messaging.CordaRPCOps
import java.time.Instant

class DriversView(private val proxy: CordaRPCOps) : ComponentView {
    override val icon = "user circle"
    override val name = "Drivers"

    private val shares: KVar<Set<ShareBalance>> = KVar(emptySet())
    private val drivers: KVar<List<DriverData>> = KVar(emptyList())

    private var selection: SelectElement? = null
    private var amountInput: InputElement? = null

    init {
        refresh()
    }

    private fun refresh() {
        GlobalScope.launch {
            shares.value = proxy.startFlowDynamic(GetShareBalancesFlow::class.java).returnValue.get().firstOrNull()?.balances ?: emptySet()
            drivers.value =
                listOf(ResultsEnterView.EMPTY) + proxy.startFlowDynamic(GetDriversFlow::class.java).returnValue.get()
            selection?.setValue(ResultsEnterView.EMPTY.driverId)
            amountInput?.setValue("")
        }
    }

    override fun renderTab(): ElementCreator<*>.() -> Unit = {
        div(fomantic.ui.fullHeight.padding(20, 20, 60, 20)).new {
            h2(fomantic.ui.header).text("Driver Shares")
            div(fomantic.ui.grid).new {
                div(fomantic.ui.three.wide.column).new {
                    div(fomantic.ui.labeled.input.width(300)).new {
                        div(fomantic.ui.label).text("Buy Shares")
                        render(drivers, { select(fomantic.ui.selection.dropdown.padding(0)).also { selection = it } }) { list ->
                            list.forEach { driverData ->
                                option().text(driverData.driverId)
                            }
                        }
                    }
                }
                div(fomantic.ui.three.wide.column).new {
                    div(fomantic.ui.labeled.input.width(300)).new {
                        div(fomantic.ui.label).text("Amount")
                        input(type = InputType.text, placeholder = "0").also { amountInput = it }
                    }
                }
                div(fomantic.ui.three.wide.column).new {
                    button(fomantic.ui.button, submit).text("Buy").also {
                        it.on.click {
                            GlobalScope.launch {
                                val driverId = selection?.getValue()?.get()?.takeIf {
                                    it != ResultsEnterView.EMPTY.driverId
                                } ?: return@launch
                                val amount = amountInput?.getValue()?.get()?.takeIf {
                                    it.isNotEmpty()
                                }?.toInt() ?: return@launch
                                async {
                                    proxy.startFlowDynamic(
                                        BuyF1SharesFlow::class.java,
                                        CordaX500Name.parse("O=F1 Governor,L=London,C=GB"),
                                        driverId,
                                        amount
                                    ).returnValue.get()
                                    refresh()
                                }
                            }
                        }
                    }
                }
                div(fomantic.ui.three.wide.column).new {
                    div(fomantic.ui.vertical.animated.button).new {
                        div(fomantic.ui.hidden.content).text("Refresh").on.click {
                            refresh()
                        }
                        div(fomantic.ui.visible.content).new {
                            i(fomantic.ui.iconClass("sync"))
                        }
                    }
                }
                div(fomantic.ui.four.wide.column)
            }
            table(fomantic.ui.celled.table).new {
                thead().new {
                    tr().new {
                        th().text("Driver ID")
                        th().text("Current Amount")
                    }
                }
                render(shares, { tbody() }) { list ->
                    list.forEach { shareData ->
                        tr().new {
                            td().text(shareData.shareId)
                            td().text(shareData.amount.toString())
                        }
                    }
                }
            }
        }
    }
}