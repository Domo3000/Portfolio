package canvas

import react.FC
import react.Props
import react.create
import react.dom.client.hydrateRoot
import web.canvas.CanvasRenderingContext2D
import web.canvas.RenderingContextId
import web.dom.document
import web.events.Event
import web.events.EventType
import web.html.HTMLCanvasElement
import web.window.window
import web.timers.setTimeout

typealias EventListener = Pair<String, (Event) -> Unit>

abstract class ExternalCanvas(private val id: String = "external-holder") {
    private val eventListeners = mutableListOf<EventListener>()

    val canvasId = "external-canvas"
    val canvasElement: HTMLCanvasElement
        get() = document.getElementById(canvasId) as HTMLCanvasElement
    val renderingContext: CanvasRenderingContext2D
        get() = canvasElement.getContext(RenderingContextId.canvas) as CanvasRenderingContext2D

    abstract val name: String
    abstract val component: FC<Props>

    abstract fun initialize(): Unit
    abstract fun cleanUp(): Unit

    fun initEventListeners() {
        document.addEventListener(EventType(name), {
            initialize()
            try {
                hydrateRoot(document.getElementById(id)!!, component.create())
            } catch (e: Exception) {
                console.log(e)
            }
        })
        document.addEventListener(EventType("${name}Cleanup"), {
            removeEventListeners()
            cleanUp()
        })
        document.dispatchEvent(Event(EventType("${name}Initialized")))
        setTimeout(
            {
                document.dispatchEvent(Event(EventType("${name}Initialized")))
            }, 3000
        )
    }

    fun addEventListener(listener: EventListener) {
        eventListeners.add(listener)
        window.addEventListener(EventType(listener.first), listener.second)
    }

    private fun removeEventListeners() {
        eventListeners.forEach { (name, handler) -> window.removeEventListener(EventType(name), handler) }
    }
}