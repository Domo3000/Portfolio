import data.CSS
import data.index
import data.styles
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
import kotlinx.css.*
import kotlinx.html.*
import utils.logError
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

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
                get("/health") {
                    call.respondText("Healthy!")
                }
                get("/robots.txt") {
                    call.respondText(
                        """User-agent: *
                              |Allow: /""".trimMargin()
                    )
                }
                get("/static/styles.css") {
                    call.respondCss(CssBuilder::styles)
                }
                get("/{...}") {
                    call.respondHtml(HttpStatusCode.OK, HTML::index)
                }
                static("/static") {
                    resources("assets")
                }
            }
        }
    }.start(wait = true)
}

private suspend fun ApplicationCall.respondText(text: String) = respondText(text = text, status = HttpStatusCode.OK)

/**
 * https://ktor.io/docs/css-dsl.html#serve_css
 */
private suspend inline fun ApplicationCall.respondCss(css: CSS) {
    this.respondText(CssBuilder().apply(css).toString(), ContentType.Text.CSS)
}