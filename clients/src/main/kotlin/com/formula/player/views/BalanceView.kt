package com.formula.player.views

import com.formula.flows.F1XBalanceData
import com.formula.flows.F1XIssueFlow
import com.formula.flows.GetF1XBalanceFlow
import com.formula.flows.GetF1XBalancesFlow
import com.formula.fullHeight
import com.formula.iconClass
import com.formula.padding
import com.formula.views.ComponentView
import com.formula.width
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kweb.ElementCreator
import kweb.InputType
import kweb.button
import kweb.div
import kweb.i
import kweb.input
import kweb.new
import kweb.plugins.fomanticUI.fomantic
import kweb.span
import kweb.state.KVar
import kweb.state.render
import kweb.table
import kweb.tbody
import kweb.td
import kweb.th
import kweb.thead
import kweb.tr
import net.corda.core.messaging.CordaRPCOps

class BalanceView(private val proxy: CordaRPCOps) : ComponentView {
    override val icon = "coins"
    override val name = "Balance"


    private var currentBalance = KVar("0")
    private val oldBalances: KVar<List<F1XBalanceData>> = KVar(emptyList())

    init {
        refreshBalance()
        refreshOldBalances()
    }

    override fun renderTab(): ElementCreator<*>.() -> Unit = {
        div(fomantic.ui.fullHeight.padding(20, 20, 60, 20)).new {
            div(fomantic.ui.raised.segment).new {
                div(fomantic.ui.grid).new {
                    div(fomantic.ui.five.wide.column).new {
                        span().text("Current Balance (F1X):")
                    }
                    div(fomantic.ui.two.wide.column).new {
                        div(fomantic.ui.blue.horizontal.label).text = currentBalance
                    }
                    div(fomantic.ui.two.wide.column).new {
                        div(fomantic.ui.vertical.animated.button.padding(top = 5, bottom = 5, right = 20, left = 20)).new {
                            div(fomantic.ui.hidden.content).text("Refresh").on.click {
                                GlobalScope.launch {
                                    refreshBalance()
                                    refreshOldBalances()
                                }
                            }
                            div(fomantic.ui.visible.content).new {
                                i(fomantic.ui.iconClass("sync"))
                            }
                        }
                    }
                    div(fomantic.ui.seven.wide.column)
                }
            }
            div(fomantic.ui.left.action.input).new {
                val button = button(fomantic.ui.teal.labeled.icon.button.width(170)).also {
                    it.text = KVar("Buy F1 Tokens")
                    it.new {
                        i(fomantic.ui.iconClass("coins"))
                    }
                }
                val input = input(type = InputType.text, placeholder = "0")
                button.on.click {
                    GlobalScope.launch {
                        val value = input.getValue().get().toInt()
                        if (value != 0) {
                            proxy.startFlowDynamic(F1XIssueFlow::class.java, value).returnValue.get()
                            refreshBalance()
                            refreshOldBalances()
                            input.setValue("")
                        }
                    }
                }
            }
            table(fomantic.ui.celled.table.padding(20)).new {
                thead().new {
                    tr().new {
                        th().text("Previous Balance")
                        th().text("Notes")
                    }
                }
                render(oldBalances, { tbody() }) { list ->
                    list.forEach { balance ->
                        tr().new {
                            td().text(balance.amount.toString())
                            td().text(balance.note)
                        }
                    }
                }
            }
        }
    }

    private fun refreshBalance() {
        currentBalance.value =
            (proxy.startFlowDynamic(GetF1XBalanceFlow::class.java).returnValue.get() as F1XBalanceData).amount.toString()
    }

    private fun refreshOldBalances() {
        oldBalances.value =
            proxy.startFlowDynamic(GetF1XBalancesFlow::class.java).returnValue.get() as List<F1XBalanceData>
    }
}