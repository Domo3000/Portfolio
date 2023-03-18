import connect4.Connect4ConnectionHandler
import connect4.Connect4GameHandler
import connect4.createConnect4Websocket
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.css.CssBuilder
import kotlinx.html.HTML
import org.slf4j.LoggerFactory
import utils.logError
import utils.logInfo
import java.io.File
import java.security.KeyStore
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

suspend fun main(): Unit = coroutineScope {
    val keystoreFile = File("${System.getProperty("user.dir")}/documents/keystore.jks")
    val debug = try {
        System.getenv("DEBUG") == "true"
    } catch (_: Exception) {
        false
    }
    val alias = try {
        System.getenv("KEY_ALIAS")
    } catch (_: Exception) {
        "domo.software"
    }

    val environment = if (debug) {
        applicationEngineEnvironment {
            log = LoggerFactory.getLogger("ktor.application")
            connector {
                port = 8080
            }
            module {
                body(debug)
            }
        }
    } else {
        applicationEngineEnvironment {
            log = LoggerFactory.getLogger("ktor.application")
            connector {
                port = 8080
            }
            sslConnector(
                keyStore = KeyStore.getInstance(keystoreFile, System.getenv("KEYSTORE_PASSWORD").toCharArray()),
                keyAlias = alias,
                keyStorePassword = { System.getenv("KEYSTORE_PASSWORD").toCharArray() },
                privateKeyPassword = { System.getenv("KEYSTORE_PASSWORD").toCharArray() }) {
                port = 8443
                keyStorePath = keystoreFile
            }
            module {
                body(debug)
            }
        }
    }

    embeddedServer(Netty, environment).start(wait = true)
}

private suspend fun ApplicationCall.respondText(text: String) = respondText(text = text, status = HttpStatusCode.OK)

/**
 * https://ktor.io/docs/css-dsl.html#serve_css
 */
private suspend inline fun ApplicationCall.respondCss(css: CSS) {
    this.respondText(CssBuilder().apply(css).toString(), ContentType.Text.CSS)
}

private fun Application.body(debug: Boolean) {
    with(Connect4ConnectionHandler) {
        with(Connect4GameHandler) {
            with(environment) {
                if (debug) {
                    launch(Dispatchers.IO) {
                        var counter = 0

                        repeat(50) {
                            println(it)
                            battleMedium()
                            battleHard()
                        }
                        currentScore()

                        while (true) {
                            cleanUp(System.currentTimeMillis())
                            // TODO improve training and evaluation
                            try {
                                if (counter++ % 50 == 0) {
                                    logInfo("bigBattle")
                                    repeat(20) {
                                        battleMedium()
                                        battleHard()
                                    }
                                    currentScore()
                                    //logInfo("evolve")
                                    //evolve()
                                } else if (counter % 25 == 0) {
                                    logInfo("evaluate")
                                    storeHighest()
                                    evaluate()
                                } else if (counter % 5 == 0) {
                                    logInfo("BattleMedium")
                                    repeat(10) {
                                        battleMedium()
                                    }
                                    logInfo("BattleHard")
                                    repeat(20) {
                                        battleHard()
                                    }
                                } else {
                                    logInfo("Train")
                                    trainAll()
                                }
                            } catch (e: Exception) {
                                logError(e)
                            }

                            delay(5.seconds)
                        }
                    }
                }
                /*
                else {
                    launch(Dispatchers.IO) {
                        while(true) {
                            cleanUp(System.currentTimeMillis())
                        }
                    }
                }
                 */
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
                    pingPeriod = 5.seconds.toJavaDuration()
                    timeout = 15.seconds.toJavaDuration()
                    maxFrameSize = Long.MAX_VALUE
                    masking = false
                }
                routing {
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
                    static("/static") {
                        resources("assets")
                    }
                }
            }
        }
    }
}
