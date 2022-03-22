package com.formula.governor.views

import com.formula.contracts.DriverData
import com.formula.contracts.RaceResults
import com.formula.flows.GetDriversFlow
import com.formula.flows.RecordRaceResultsFlow
import com.formula.fullHeight
import com.formula.iconClass
import com.formula.padding
import com.formula.views.ComponentView
import com.formula.width
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kweb.ButtonType.submit
import kweb.ElementCreator
import kweb.SelectElement
import kweb.button
import kweb.div
import kweb.h2
import kweb.i
import kweb.new
import kweb.option
import kweb.plugins.fomanticUI.fomantic
import kweb.select
import kweb.state.KVar
import kweb.state.render
import net.corda.core.messaging.CordaRPCOps
import java.time.Instant

class ResultsEnterView(private val proxy: CordaRPCOps) : ComponentView {
    companion object {
        val EMPTY = DriverData("Not Selected", "NA")
    }

    override val icon = "edit"
    override val name = "Enter Race Results"

    private var first: SelectElement? = null
    private var second: SelectElement? = null
    private var third: SelectElement? = null
    private var fourth: SelectElement? = null
    private var fifth: SelectElement? = null
    private var sixth: SelectElement? = null
    private var seventh: SelectElement? = null
    private var eighth: SelectElement? = null
    private var ninth: SelectElement? = null
    private var tenth: SelectElement? = null

    private val drivers: KVar<List<DriverData>> = KVar(emptyList())

    init {
        refresh()
    }

    private fun refresh() {
        GlobalScope.launch {
            drivers.value = listOf(EMPTY) + proxy.startFlowDynamic(GetDriversFlow::class.java).returnValue.get()
            first?.setValue(EMPTY.driverId)
            second?.setValue(EMPTY.driverId)
            third?.setValue(EMPTY.driverId)
            fourth?.setValue(EMPTY.driverId)
            fifth?.setValue(EMPTY.driverId)
            sixth?.setValue(EMPTY.driverId)
            seventh?.setValue(EMPTY.driverId)
            eighth?.setValue(EMPTY.driverId)
            ninth?.setValue(EMPTY.driverId)
            tenth?.setValue(EMPTY.driverId)
        }
    }

    override fun renderTab(): ElementCreator<*>.() -> Unit = {
        div(fomantic.ui.fullHeight.padding(20, 20, 60, 20)).new {
            div(fomantic.ui.vertical.animated.button).new {
                div(fomantic.ui.hidden.content).text("Refresh").on.click {
                    refresh()
                }
                div(fomantic.ui.visible.content).new {
                    i(fomantic.ui.iconClass("sync"))
                }
            }
            h2(fomantic.ui.header).text("Enter Race Results:")
            div(fomantic.ui.grid).new {
                div(fomantic.ui.sixteen.wide.column).new {
                    div(fomantic.ui.labeled.input.padding(top = 20).width(200)).new {
                        div(fomantic.ui.label).text("First Place")
                        render(
                            drivers,
                            { select(fomantic.ui.selection.dropdown.padding(0)).also { first = it } }) { list ->
                            list.forEach { driverData ->
                                option().text(driverData.driverId)
                            }
                        }
                    }
                    div(fomantic.ui.labeled.input.padding(top = 20).width(200)).new {
                        div(fomantic.ui.label).text("Second Place")
                        render(
                            drivers,
                            { select(fomantic.ui.selection.dropdown.padding(0)).also { second = it } }) { list ->
                            list.forEach { driverData ->
                                option().text(driverData.driverId)
                            }
                        }
                    }
                    div(fomantic.ui.labeled.input.padding(top = 20).width(200)).new {
                        div(fomantic.ui.label).text("Third Place")
                        render(
                            drivers,
                            { select(fomantic.ui.selection.dropdown.padding(0)).also { third = it } }) { list ->
                            list.forEach { driverData ->
                                option().text(driverData.driverId)
                            }
                        }
                    }
                    div(fomantic.ui.labeled.input.padding(top = 20).width(200)).new {
                        div(fomantic.ui.label).text("Fourth Place")
                        render(
                            drivers,
                            { select(fomantic.ui.selection.dropdown.padding(0)).also { fourth = it } }) { list ->
                            list.forEach { driverData ->
                                option().text(driverData.driverId)
                            }
                        }
                    }
                    div(fomantic.ui.labeled.input.padding(top = 20).width(200)).new {
                        div(fomantic.ui.label).text("Fifth Place")
                        render(
                            drivers,
                            { select(fomantic.ui.selection.dropdown.padding(0)).also { fifth = it } }) { list ->
                            list.forEach { driverData ->
                                option().text(driverData.driverId)
                            }
                        }
                    }
                    div(fomantic.ui.labeled.input.padding(top = 20).width(200)).new {
                        div(fomantic.ui.label).text("Sixth Place")
                        render(
                            drivers,
                            { select(fomantic.ui.selection.dropdown.padding(0)).also { sixth = it } }) { list ->
                            list.forEach { driverData ->
                                option().text(driverData.driverId)
                            }
                        }
                    }
                    div(fomantic.ui.labeled.input.padding(top = 20).width(200)).new {
                        div(fomantic.ui.label).text("Seventh Place")
                        render(
                            drivers,
                            { select(fomantic.ui.selection.dropdown.padding(0)).also { seventh = it } }) { list ->
                            list.forEach { driverData ->
                                option().text(driverData.driverId)
                            }
                        }
                    }
                    div(fomantic.ui.labeled.input.padding(top = 20).width(200)).new {
                        div(fomantic.ui.label).text("Eighth Place")
                        render(
                            drivers,
                            { select(fomantic.ui.selection.dropdown.padding(0)).also { eighth = it } }) { list ->
                            list.forEach { driverData ->
                                option().text(driverData.driverId)
                            }
                        }
                    }
                    div(fomantic.ui.labeled.input.padding(top = 20).width(300)).new {
                        div(fomantic.ui.label).text("Ninth Place")
                        render(
                            drivers,
                            { select(fomantic.ui.selection.dropdown.padding(0)).also { ninth = it } }) { list ->
                            list.forEach { driverData ->
                                option().text(driverData.driverId)
                            }
                        }
                    }
                    div(fomantic.ui.labeled.input.padding(top = 20).width(300)).new {
                        div(fomantic.ui.label).text("Tenth Place")
                        render(
                            drivers,
                            { select(fomantic.ui.selection.dropdown.padding(0)).also { tenth = it } }) { list ->
                            list.forEach { driverData ->
                                option().text(driverData.driverId)
                            }
                        }
                    }
                }
                div(fomantic.ui.twelve.wide.column)
            }
            div(fomantic.ui.padding(top = 20)).new {
                button(fomantic.ui.button, submit).text("Enter Results").also {
                    it.on.click {
                        GlobalScope.launch {
                            val results = listOfNotNull(
                                first!!.getValue().get().takeIf { it != EMPTY.driverId },
                                second!!.getValue().get().takeIf { it != EMPTY.driverId },
                                third!!.getValue().get().takeIf { it != EMPTY.driverId },
                                fourth!!.getValue().get().takeIf { it != EMPTY.driverId },
                                fifth!!.getValue().get().takeIf { it != EMPTY.driverId },
                                sixth!!.getValue().get().takeIf { it != EMPTY.driverId },
                                seventh!!.getValue().get().takeIf { it != EMPTY.driverId },
                                eighth!!.getValue().get().takeIf { it != EMPTY.driverId },
                                ninth!!.getValue().get().takeIf { it != EMPTY.driverId },
                                tenth!!.getValue().get().takeIf { it != EMPTY.driverId }
                            )
                            if (results.isEmpty() || results.toSet().size != results.size) return@launch
                            async {
                                proxy.startFlowDynamic(
                                    RecordRaceResultsFlow::class.java,
                                    RaceResults(results, Instant.now())
                                ).returnValue.get()
                            }
                            refresh()
                        }
                    }
                }
            }
        }
    }
}