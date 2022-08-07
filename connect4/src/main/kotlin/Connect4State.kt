import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.w3c.dom.events.Event

class Connect4State {
    var intervalId: Int? = null
    var text: String = "Not Connected"
    var session: DefaultClientWebSocketSession? = null

    val eventHandlers = mutableMapOf<String, (Event) -> Unit>()
    val scope = MainScope()

    val client = HttpClient {
        install(WebSockets)
    }

    fun reset() {
        text = "Not Connected"
        removeEventHandlers()
        clearInterval()
        closeSession()
    }

    fun connectSession() {
        scope.launch {
            session = try {
                client.webSocketSession(path = "/connect4") // TODO no longer works
            } catch (e: Exception) {
                console.log(e.message)
                console.log(e.cause?.message)
                null
            }
            console.log(session)
        }
    }

    fun closeSession() {
        session?.let {
            scope.launch { it.close(CloseReason(CloseReason.Codes.GOING_AWAY, "")) }
            session = null
        }
    }

    private fun clearInterval() {
        intervalId?.let {
            window.clearInterval(it)
            intervalId = null
        }
    }

    private fun removeEventHandlers() {
        eventHandlers.forEach { (type, handler) -> window.removeEventListener(type, handler) }
        eventHandlers.clear()
    }
}
