import connect4.messages.Connect4Message
import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.websocket.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import util.inputMessage
import util.readMessages

class WebsocketState(private val socketHost: String, private val socketPort: Int, secure: Boolean) {
    private val scheme = if (secure) "wss" else "ws"
    var session: DefaultClientWebSocketSession? = null

    val scope = MainScope()

    val client = HttpClient {
        install(WebSockets) {
            pingInterval = 5000
        }
    }

    fun reset() {
        closeSession()
    }

    fun connectSession() {
        scope.launch {
            session = try {
                client.webSocketSession {
                    method = HttpMethod.Get
                    url(scheme, socketHost, socketPort, "/connect4")
                }
            } catch (e: Exception) {
                console.log(e.message)
                null
            }
            console.log(session)
        }
    }

    fun inputMessage(message: Connect4Message) {
        scope.launch {
            session?.inputMessage(message.encode())
        }
    }

    fun readMessages(handler: (Connect4Message) -> Unit) {
        scope.launch {
            session?.readMessages(handler)
        }
    }

    fun closeSession() {
        session?.let {
            scope.launch { it.close(CloseReason(CloseReason.Codes.GOING_AWAY, "")) }
            session = null
        }
    }
}
