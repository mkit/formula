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

class SportExchangeView(private val proxy: CordaRPCOps) : ComponentView {
    override val icon = "exchange alternate"
    override val name = "Sport Exchange"

    override fun renderTab(): ElementCreator<*>.() -> Unit = {
        div(fomantic.ui.fullHeight.padding(20, 20, 60, 20)).new {
            h3().text("Not Implemented")
        }
    }
}