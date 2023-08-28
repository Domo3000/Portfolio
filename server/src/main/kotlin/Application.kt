import connect4.Connect4ConnectionHandler
import connect4.Connect4GameHandler
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.cachingheaders.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.websocket.*
import io.ktor.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import routing.installRouting
import utils.logError
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

private fun Application.body(debug: Boolean) {
    with(Connect4ConnectionHandler) {
        with(Connect4GameHandler) {
            with(environment) {
                launch(Dispatchers.IO) {
                    loadStored()
                }
                if (debug) {
                    launch(Dispatchers.IO) {
                        while (true) {
                            // TODO test this in more detail
                            cleanUp(System.currentTimeMillis())
                        }
                    }
                }
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
                if (!debug) {
                    install(CachingHeaders) {
                        options { _, outgoingContent ->
                            when (outgoingContent.contentType?.withoutParameters()) {
                                ContentType.Text.CSS, ContentType.Image.JPEG, ContentType.Image.PNG ->
                                    CachingOptions(CacheControl.MaxAge(maxAgeSeconds = 24 * 60 * 60))

                                ContentType.Application.JavaScript, ContentType.Application.Json ->
                                    CachingOptions(CacheControl.MaxAge(maxAgeSeconds = 365 * 24 * 60 * 60))

                                else -> null
                            }
                        }
                    }
                }
                installRouting()
            }
        }
    }
}
