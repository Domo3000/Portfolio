import canvas.drawBackground
import canvas.resetDimensions
import org.w3c.dom.events.Event
import react.ExternalCanvas
import react.FC
import react.Props
import react.dom.html.ReactHTML.canvas
import react.useEffectOnce

class Automaton : ExternalCanvas() {
    override val name: String = "Automaton"

    override val component: FC<Props>
        get() = FC {
            val resizeHandler: (Event) -> Unit = { }

            canvas {
                className = Classnames.responsiveCanvas
                id = canvasId
            }

            useEffectOnce {
                addEventListener("resize" to resizeHandler)
                canvasElement.resetDimensions()
                renderingContext.drawBackground()
            }
        }

    override fun cleanUp() {}

    override fun initialize() {}

    init {
        initEventListeners()
    }
}