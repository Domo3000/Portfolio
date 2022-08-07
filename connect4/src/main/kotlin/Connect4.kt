import canvas.drawBackground
import canvas.resetDimensions
import connect4.messages.Connect4Messages
import connect4.messages.NextMoveMessage
import csstype.NamedColor
import io.ktor.websocket.*
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.launch
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.events.Event
import org.w3c.dom.events.KeyboardEvent
import react.FC
import react.Props
import react.dom.html.ReactHTML
import react.useEffectOnce
import sockets.inputMessages
import sockets.readMessages

private fun draw(state: Connect4State, canvasElement: HTMLCanvasElement, renderingContext: CanvasRenderingContext2D) {
    canvasElement.resetDimensions()
    renderingContext.drawBackground()

    renderingContext.fillStyle = NamedColor.black
    val fontSize = canvasElement.height / 20

    renderingContext.font = "${fontSize}px Courier New"
    renderingContext.fillText(
        state.text,
        canvasElement.width / 3.0,
        canvasElement.height / 2.0 - 2.5 * fontSize
    )
}

fun x(state: Connect4State) = FC<Props> {
    val elementId = "layout-canvas"

    val canvasElement by lazy { document.getElementById(elementId) as HTMLCanvasElement }
    val renderingContext: CanvasRenderingContext2D by lazy { canvasElement.getContext("2d") as CanvasRenderingContext2D }

    val resizeEventHandler: (Event) -> Unit = {
        console.log("resize")
        draw(state, canvasElement, renderingContext)
    }

    val keypressEventHandler: (Event) -> Unit = { event ->
        when ((event as KeyboardEvent).key.lowercase()) {
            "c" -> {
                state.scope.launch {
                    state.session?.inputMessages(NextMoveMessage(0, 0).encode())
                }
                draw(state, canvasElement, renderingContext)
            }
        }
    }

    val closeEventHandler: (Event) -> Unit = {
        state.closeSession()
    }

    ReactHTML.canvas {
        className = Classnames.responsiveCanvas
        id = elementId
    }

    useEffectOnce {
        draw(state, canvasElement, renderingContext)
        state.connectSession()

        state.eventHandlers.clear()
        state.eventHandlers.putAll(
            mapOf(
                "resize" to resizeEventHandler,
                "keypress" to keypressEventHandler,
                "close" to closeEventHandler
            )
        )
        state.eventHandlers.forEach { (type, handler) -> window.addEventListener(type, handler) }

        state.intervalId = window.setInterval({
            state.scope.launch {
                state.session?.readMessages {
                    val message = Connect4Messages.decode(it.readText())
                    console.log(message)
                    state.text = message?.encode() ?: "empty"
                    draw(state, canvasElement, renderingContext)
                }
            }
        }, 1000)
    }
}