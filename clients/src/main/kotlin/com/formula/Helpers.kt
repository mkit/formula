package com.formula

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kweb.Element
import kweb.ElementCreator
import kweb.button
import kweb.classes
import kweb.div
import kweb.i
import kweb.id
import kweb.new
import kweb.plugins.fomanticUI.FomanticUIClasses
import kweb.plugins.fomanticUI.fomantic
import net.corda.core.messaging.CordaRPCOps
import java.net.ServerSocket
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
    .withLocale(Locale.UK)
    .withZone(ZoneId.systemDefault())

fun getFreePort(): Int {
    return ServerSocket(0).use {
        it.localPort
    }
}

fun FomanticUIClasses.withDataTabAttribute(id: String) = withCustomAttribute("data-tab", id)
fun FomanticUIClasses.withDataContentAttribute(value: String) = withCustomAttribute("data-content", value)
fun FomanticUIClasses.withCustomAttribute(key: String, value: String) = this.also { this[key] = value }
fun FomanticUIClasses.withImageSource(source: String) = this.also { this["src"] = source }

fun FomanticUIClasses.padding(top: Int = 0, right: Int = 0, bottom: Int = 0, left: Int = 0): FomanticUIClasses {
    return addStyle("padding: ${top}px ${right}px ${bottom}px ${left}px;")
}

fun FomanticUIClasses.padding(space: Int): FomanticUIClasses {
    return addStyle("padding: ${space}px;")
}

fun FomanticUIClasses.iconClass(iconClasses: String): FomanticUIClasses {
    if (iconClasses.isNotBlank()) {
        classes(iconClasses.split(" ") + "icon")
    }
    return this
}

fun FomanticUIClasses.customClasses(customClasses: String): FomanticUIClasses {
    if (customClasses.isNotBlank()) {
        classes(customClasses.split(" "))
    }
    return this
}

fun FomanticUIClasses.margin(top: Int = 0, right: Int = 0, bottom: Int = 0, left: Int = 0): FomanticUIClasses {
    return addStyle("margin: ${top}px ${right}px ${bottom}px ${left}px;")
}

fun FomanticUIClasses.margin(space: Int): FomanticUIClasses {
    return addStyle("margin: ${space}px;")
}

fun FomanticUIClasses.height(height: Int): FomanticUIClasses {
    return addStyle("height: ${height}px;")
}

fun FomanticUIClasses.width(width: Int): FomanticUIClasses {
    return addStyle("width: ${width}px;")
}

val FomanticUIClasses.fullHeight: FomanticUIClasses
    get() {
        return addStyle("height: 100%;")
    }

val FomanticUIClasses.fullWidth: FomanticUIClasses
    get() {
        return addStyle("width: 100%;")
    }

val FomanticUIClasses.absolute: FomanticUIClasses
    get() {
        return addStyle("position: absolute;")
    }

val FomanticUIClasses.relative: FomanticUIClasses
    get() {
        return addStyle("position: relative;")
    }

val FomanticUIClasses.block: FomanticUIClasses
    get() {
        return addStyle("display: block;")
    }

fun FomanticUIClasses.position(
    top: Int? = null,
    left: Int? = null,
    bottom: Int? = null,
    right: Int? = null
): FomanticUIClasses {
    top?.let { addStyle("top: ${it}px;") }
    left?.let { addStyle("left: ${it}px;") }
    bottom?.let { addStyle("bottom: ${it}px;") }
    right?.let { addStyle("right: ${it}px;") }
    return this
}

fun FomanticUIClasses.addStyle(style: String): FomanticUIClasses {
    val existingStyle = this["style"]?.toString() ?: ""
    return withCustomAttribute("style", "$existingStyle$style")
}

fun FomanticUIClasses.hidden(): FomanticUIClasses {
    return fullWidth.fullHeight.addStyle("opacity: 0;overflow: hidden; position: absolute; display: none;")
}

fun ElementCreator<Element>.cancelButton(id: String, label: String, onClick: () -> Unit): Element {
    return button(fomantic.ui.large.red.right.floated.button.iconClass("power off").id(id)).apply {
        text(label)
        on.click {
            GlobalScope.launch {
                onClick()
            }
        }.also {
            new {
                i(fomantic.iconClass("power off").padding(left = 10, right = 15))
            }
        }
    }
}

fun ElementCreator<*>.progressBar(id: String): Element {
    return div(fomantic.ui.indicating.progress.id(id)).also {
        it.new {
            div(fomantic.ui.bar).new {
                div(nonFomanticClass("progress"))
            }
            div(nonFomanticClass("label"))
        }
    }
}

fun nonFomanticClass(value: String): Map<String, String> {
    return mapOf("class" to value)
}

fun CordaRPCOps.myName() = nodeInfo().legalIdentities.first().name.organisation