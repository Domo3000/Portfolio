package react

import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.CanvasRenderingContext2DSettings
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.events.Event
import react.dom.client.hydrateRoot

typealias EventListener = Pair<String, (Event) -> Unit>

abstract class ExternalCanvas(private val id: String = "external-holder") {
    private val eventListeners = mutableListOf<EventListener>()

    val canvasId = "external-canvas"
    val canvasElement: HTMLCanvasElement
        get() = document.getElementById(canvasId) as HTMLCanvasElement // TODO why does by lazy not work here
    val renderingContext: CanvasRenderingContext2D
        get() = canvasElement.getContext("2d", CanvasRenderingContext2DSettings(false)) as CanvasRenderingContext2D



    abstract val name: String
    abstract val component: FC<Props>

    abstract fun initialize(): Unit
    abstract fun cleanUp(): Unit

    fun initEventListeners() {
        document.addEventListener(name, {
            initialize()
            try {
                hydrateRoot(document.getElementById(id)!!, component.create())
            } catch (e: Exception) {
                console.log(e)
            }
        })
        document.addEventListener("${name}Cleanup", {
            removeEventListeners()
            cleanUp()
        })
    }

    fun addEventListener(listener: EventListener) {
            eventListeners.add(listener)
            window.addEventListener(listener.first, listener.second)
    }

    private fun removeEventListeners() {
        eventListeners.forEach { (name, handler) -> window.removeEventListener(name, handler) }
    }
}