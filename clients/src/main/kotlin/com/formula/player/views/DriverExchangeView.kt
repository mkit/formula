package com.formula.player.views

import com.formula.fullHeight
import com.formula.padding
import com.formula.views.ComponentView
import kweb.ElementCreator
import kweb.div
import kweb.h3
import kweb.new
import kweb.plugins.fomanticUI.fomantic
import net.corda.core.messaging.CordaRPCOps

class DriverExchangeView(private val proxy: CordaRPCOps) : ComponentView {
    override val icon = "people arrows"
    override val name = "Driver Exchange"

    override fun renderTab(): ElementCreator<*>.() -> Unit = {
        div(fomantic.ui.fullHeight.padding(20, 20, 60, 20)).new {
            h3().text("Not Implemented")
        }
    }
}