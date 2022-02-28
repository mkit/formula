package com.formula.governor.views

import com.formula.contracts.RaceResults
import com.formula.flows.GetRaceResultsFlow
import com.formula.fullHeight
import com.formula.iconClass
import com.formula.padding
import com.formula.player.views.RaceView
import com.formula.views.ComponentView
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kweb.ElementCreator
import kweb.div
import kweb.h2
import kweb.h4
import kweb.i
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
import net.corda.core.messaging.CordaRPCOps
import java.time.ZoneId

import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale


class RaceView(private val proxy: CordaRPCOps) : ComponentView {
    override val icon = "flag checkered"
    override val name = "Historical Races"

    private val races: KVar<List<RaceResults>> = KVar(emptyList())

    private val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
        .withLocale(Locale.UK)
        .withZone(ZoneId.systemDefault())

    init {
        refresh()
    }

    private fun refresh() {
        GlobalScope.launch {
            races.value = proxy.startFlowDynamic(GetRaceResultsFlow::class.java).returnValue.get()
        }
    }

    override fun renderTab(): ElementCreator<*>.() -> Unit = {
        div(fomantic.ui.fullHeight.padding(20, 20, 60, 20)).new {
            h2(fomantic.ui.header).text("Historical Race Results")
            h4(fomantic.ui.header).text("Winnings distribution: ${RaceResults.TOTAL_RACE_POINTS}")
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
                        th().text("Timestamp")
                        th().text("Winners")
                    }
                }
                render(races, { tbody() }) { list ->
                    list.forEach { raceResults ->
                        tr().new {
                            td().text(formatter.format(raceResults.raceStartTime))
                            td().text(raceResults.orderedResults.toString())
                        }
                    }
                }
            }
        }
    }
}