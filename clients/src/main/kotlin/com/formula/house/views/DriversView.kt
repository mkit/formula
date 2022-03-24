package com.formula.house.views

import com.formula.contracts.DriverData
import com.formula.flows.DriverIssueFlow
import com.formula.flows.GetDriversFlow
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
import kweb.h2
import kweb.i
import kweb.input
import kweb.new
import kweb.plugins.fomanticUI.fomantic
import kweb.state.KVar
import kweb.state.render
import kweb.table
import kweb.tbody
import kweb.td
import kweb.th
import kweb.thead
import kweb.tr
import net.corda.core.identity.CordaX500Name
import net.corda.core.internal.concurrent.doOnComplete
import net.corda.core.messaging.CordaRPCOps

class DriversView(private val proxy: CordaRPCOps, private val cordaName: CordaX500Name) : ComponentView {
    override val icon = "user circle"
    override val name = "Drivers"

    private val drivers: KVar<List<DriverData>> = KVar(emptyList())

    init {
        refresh()
    }

    private fun refresh() {
        GlobalScope.launch {
            drivers.value = proxy.startFlowDynamic(GetDriversFlow::class.java).returnValue.get()
                .filter { it.house == cordaName.toString() }
        }
    }

    override fun renderTab(): ElementCreator<*>.() -> Unit = {
        div(fomantic.ui.fullHeight.padding(20, 20, 60, 20)).new {
            h2(fomantic.ui.header).text("Add Driver")
            div(fomantic.ui.left.action.input).new {
                val button = button(fomantic.ui.teal.labeled.icon.button.width(170)).also {
                    it.text = KVar("Driver ID")
                    it.new {
                        i(fomantic.ui.iconClass("user circle"))
                    }
                }
                val input = input(type = InputType.text, placeholder = "Driver ID")
                button.on.click {
                    GlobalScope.launch {
                        val driverId = input.getValue().get()
                        if (!driverId.isNullOrEmpty()) {
                            proxy.startFlowDynamic(DriverIssueFlow::class.java, driverId).returnValue.doOnComplete {
                                input.setValue("")
                                refresh()
                            }
                        }
                    }
                }
            }
            h2(fomantic.ui.header.padding(top = 20)).text("List of Drivers")
            div(fomantic.ui.vertical.animated.button).new {
                div(fomantic.ui.hidden.content).text("Refresh").on.click {
                    refresh()
                }
                div(fomantic.ui.visible.content).new {
                    i(fomantic.ui.iconClass("sync"))
                }
            }
            table(fomantic.ui.celled.table).new {
                thead().new {
                    tr().new {
                        th().text("Driver ID")
                        th().text("Manufacturer")
                    }
                }
                render(drivers, { tbody() }) { list ->
                    list.forEach { driverData ->
                        tr().new {
                            td().text(driverData.driverId)
                            td().text(driverData.house)
                        }
                    }
                }
            }
        }
    }
}