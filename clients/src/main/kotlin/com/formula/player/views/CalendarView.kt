package com.formula.player.views

import com.formula.fullHeight
import com.formula.padding
import com.formula.views.ComponentView
import kweb.ElementCreator
import kweb.div
import kweb.h3
import kweb.new
import kweb.p
import kweb.plugins.fomanticUI.fomantic
import net.corda.core.messaging.CordaRPCOps

class CalendarView(private val proxy: CordaRPCOps) : ComponentView {
    override val icon = "calendar alternate outline"
    override val name = "Calendar"

    override fun renderTab(): ElementCreator<*>.() -> Unit = {
        div(fomantic.ui.fullHeight.padding(20, 20, 60, 20)).new {
            h3().text("Not Implemented (Preview)")
            p().text("18 March – Bahrain (Sakhir)")
            p().text("25 March – Saudi Arabian (Jeddah)")
            p().text("08 April – Australian (Melbourne)")
            p().text("22 April - Emilia Romangna (Imola)")
            p().text("06 May – Miami (Autodrome)")
            p().text("20 May – Spainish (Barcelona)")
            p().text("27 May – Monaco (Monaco)")
            p().text("10 June – Azerbaijan (Baku)")
            p().text("17 June – Canadian (Montreal)")
            p().text("01 July – British (Silverstone)")
            p().text("08 July – Austrian (Spielberg)")
            p().text("22 July – French (Le Castellet)")
            p().text("29 July – Hungarian (Budapest)")
            p().text("26 August – Belgium (Spa)")
            p().text("02 September – Dutch (Zandvoort)")
            p().text("09 September - Italian (Monza)")
            p().text("30 September - Singapore (Marina Bay)")
            p().text("07 October – Japanese (Suzuka)")
            p().text("21 October – USA (Austin)")
            p().text("31 October – Mexican (Mexico City)")
            p().text("11 November – Brazilian (Sao Paulo)")
            p().text("18 December - Abu Dhabi (Yas Island)")
        }
    }
}