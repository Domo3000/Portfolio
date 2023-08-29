package routing

import connect4.Connect4ConnectionHandler
import connect4.Connect4GameHandler
import connect4.createConnect4Websocket
import data.CSS
import data.index
import data.styles
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.css.CssBuilder
import kotlinx.html.HTML

private suspend fun ApplicationCall.respondText(text: String) = respondText(text = text, status = HttpStatusCode.OK)

/**
 * https://ktor.io/docs/css-dsl.html#serve_css
 */
private suspend inline fun ApplicationCall.respondCss(css: CSS) {
    this.respondText(CssBuilder().apply(css).toString(), ContentType.Text.CSS)
}

context(Connect4ConnectionHandler, Connect4GameHandler, ApplicationEnvironment)
fun Application.installRouting() = routing {
    createConnect4Websocket()
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
    staticResources("/static", "assets")
}