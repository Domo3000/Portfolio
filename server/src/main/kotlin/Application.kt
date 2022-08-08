import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.html.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import kotlinx.coroutines.coroutineScope
import kotlinx.html.*
import utils.logError
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

fun HTML.index() {
    head {
        title("Domo")
        meta {
            name = "viewport"
            content = "width=device-width, initial-scale=1.0"
        }
        link {
            rel = "stylesheet"
            href = "/static/shared.css"
        }
    }
    body {
        div {
            id = "script-holder"
            script(src = "/static/browser.js") { }
        }
        /*
        div("hidden") {
            id = "connect4-script-holder"
            script(src = "/static/connect4.js") { async = true }
        }
         */
        script(src = "/static/automaton.js") { async = true }
        script(src = "/static/kdtree.js") { async = true }
        script(src = "/static/shuffle.js") { async = true }
    }
}

suspend fun main(): Unit = coroutineScope {
    /*
    with(Connect4) {
        launch(Dispatchers.IO) {
            while (true) {
                cleanUp()
                delay(10.minutes)
            }
        }
         */
    embeddedServer(Netty, port = 8080) {
        with(environment) {
            install(StatusPages) {
                exception<Throwable> { call, cause ->
                    logError(cause)
                    call.respondText(text = "500: $cause.", status = HttpStatusCode.InternalServerError)
                }
                status(HttpStatusCode.NotFound) { call, code ->
                    call.respondText(text = "Page Not Found", status = code)
                }
            }
            install(WebSockets) {
                pingPeriod = 15.seconds.toJavaDuration()
                timeout = 15.seconds.toJavaDuration()
                maxFrameSize = Long.MAX_VALUE
                masking = false
            }
            routing {
                //createConnect4Websocket()

                get("/") {
                    call.respondHtml(HttpStatusCode.OK, HTML::index)
                }
                get("/health") {
                    call.respondText("Healthy!")
                }
                get("/robots.txt") {
                    call.respondText(
                        """User-agent: *
                              |Allow: /""".trimMargin()
                    )
                }
                static("/static") {
                    resources()
                }
            }
        }
    }.start(wait = true)
}

private suspend fun ApplicationCall.respondText(text: String) = respondText(text = text, status = HttpStatusCode.OK)
