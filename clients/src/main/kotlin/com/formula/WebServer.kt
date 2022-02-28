package com.formula

import com.formula.views.ComponentView
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.AutoHeadResponse
import io.ktor.features.CallLogging
import io.ktor.features.Compression
import io.ktor.features.DefaultHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.cio.websocket.pingPeriod
import io.ktor.http.cio.websocket.timeout
import io.ktor.request.path
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.jetty.Jetty
import io.ktor.websocket.WebSockets
import kweb.Kweb
import kweb.a
import kweb.div
import kweb.h1
import kweb.i
import kweb.id
import kweb.img
import kweb.meta
import kweb.new
import kweb.p
import kweb.plugins.fomanticUI.fomantic
import kweb.plugins.fomanticUI.fomanticUIPlugin
import kweb.respondKweb
import org.slf4j.event.Level
import java.time.Duration

fun startWebServer(port: Int, role: String, componentsViews: List<ComponentView>) {
    println("Go to: https://0.0.0.0:$port")
    embeddedServer(Jetty, port = port) {
        kwebFeature(role, componentsViews)
    }.start()
}

private fun Application.kwebFeature(role: String, componentViews: List<ComponentView>) {

    // Respond for HEAD verb
    install(AutoHeadResponse)

    // Load each request
    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
    }

    install(DefaultHeaders)
    install(Compression)
    install(Routing)
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(10)
        timeout = Duration.ofSeconds(3000)

    }

    install(Kweb) {
        plugins = listOf(fomanticUIPlugin)
    }

    routing {
        get("/health") {
            call.respond(HttpStatusCode.OK)
        }
        get("/") {
            //call.isUserAuthenticated()

            call.respondKweb {
                doc.head.new {
                    // Not required, but recommended by HTML spec
                    meta(name = "Description", content = "F1 Admin")
                }
                doc.body.new {
                    doc.body.new {
                        div(
                            fomantic.ui.large.left.fixed.vertical.pointing.menu.padding(top = 81).id("main-menu")
                        ).new {
                            componentViews.forEach {
                                a(fomantic.item.withDataTabAttribute(it.javaClass.name)).text(it.name).new {
                                    if (it.icon.isNotBlank()) {
                                        i(fomantic.iconClass(it.icon))
                                    }
                                }
                            }
                        }
                        div(fomantic.ui.top.fixed.menu).new {
                            img(
                                fomantic.ui.width(70)
                                    .height(70).image.withImageSource("/static/fomantic/images/F1-logo.png")
                            )
                            div(fomantic.ui.middle.aligned.fullHeight.padding(top = 20, left = 20)).new {
                                h1(fomantic.ui.header).text("F1 Platform - $role")
                            }
                            div(fomantic.right.menu).new {
                                a(href = "/logout", attributes = fomantic.item).new(position = 0) {
                                    i(fomantic.ui.iconClass("sign out alternate"))
                                    p().text("Logout")
                                }
                            }
                        }
                        div(fomantic.ui.fluid.fullHeight.padding(top = 81, left = 251)).new {
                            componentViews.forEach {
                                div(fomantic.ui.fluid.fullHeight.tab.withDataTabAttribute(it.javaClass.name)).new {
                                    it.renderTab()()
                                }
                            }
                        }
                        execute("\$('#main-menu .item').tab()")
                        execute("\$('.activating.element').popup()")
                        execute(
                            "\$('#main-menu .column.segment').on('click', function() {\n" +
                                    "    var tabName = \$(this).attr('data-tab');\n" +
                                    "    \$.tab('change tab', tabName);\n" +
                                    "    \$('#main-menu .item').removeClass('active');\n" +
                                    "    \$('#main-menu .item[data-tab=\"'+ tabName +'\"]').addClass('active');\n" +
                                    "  })"
                        )
//                execute("\$('.ui.accordion').accordion()")
                    }
                }
            }
        }
    }

}