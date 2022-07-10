import canvas.drawBackground
import canvas.resetDimensions
import connect4.messages.Connect4Messages
import connect4.messages.NextMoveMessage
import csstype.NamedColor
import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.events.KeyboardEvent
import react.FC
import react.Props
import react.dom.html.ReactHTML
import react.useEffectOnce
import react.useState
import sockets.inputMessages
import sockets.readMessages
import sockets.webSocketSession

private const val ratio = 3.0 / 4.0

private fun draw(text: String, canvasElement: HTMLCanvasElement, renderingContext: CanvasRenderingContext2D) {
    canvasElement.resetDimensions(ratio)
    renderingContext.drawBackground()

    renderingContext.fillStyle = NamedColor.black
    val fontSize = canvasElement.height / 20

    renderingContext.font = "${fontSize}px Courier New"
    renderingContext.fillText(
        text,
        canvasElement.width / 3.0,
        canvasElement.height / 2.0 - 2.5 * fontSize
    )
}

val Connect4 = FC<Props> {
    val elementId = "layout-canvas"
    val (state, setState) = useState("Not Connected")
    var session: DefaultClientWebSocketSession? = null

    val scope = MainScope()

    val client = HttpClient {
        install(WebSockets)
    }

    val canvasElement by lazy { document.getElementById(elementId) as HTMLCanvasElement }

    val renderingContext: CanvasRenderingContext2D by lazy { canvasElement.getContext("2d") as CanvasRenderingContext2D }

    window.addEventListener("resize", {
        console.log("resize")
        draw(state, canvasElement, renderingContext)
    })

    window.addEventListener("keypress", { event ->
        when ((event as KeyboardEvent).key.lowercase()) {
            "c" -> {
                scope.launch {
                    session!!.inputMessages(NextMoveMessage(0, 0).encode())
                }
                draw(state, canvasElement, renderingContext)
            }
        }
    })

    window.addEventListener("close", {
        scope.launch {
            session!!.close(CloseReason(CloseReason.Codes.GOING_AWAY, ""))
        }
    })

    window.setInterval({
        console.log("interval")
        scope.launch {
            session!!.readMessages {
                val message = Connect4Messages.decode(it.readText())
                setState(message?.encode() ?: "empty")
                draw(state, canvasElement, renderingContext)
            }
        }
    }, 3000)

    ReactHTML.canvas {
        className = Classnames.responsiveCanvas
        id = elementId
    }

    useEffectOnce {
        draw(state, canvasElement, renderingContext)
        scope.launch {
            if (session == null) {
                session = client.webSocketSession(path = "/connect4")
            }
        }
    }
}