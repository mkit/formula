package com.formula.views

import kweb.ElementCreator

interface ComponentView {
    val name: String
    val icon: String

    fun renderTab(): ElementCreator<*>.() -> Unit
}