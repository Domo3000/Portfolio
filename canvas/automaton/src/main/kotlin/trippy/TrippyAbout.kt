package trippy

import canvas.ExternalCanvas
import canvas.clear
import canvas.setDimensions
import css.Classes
import csstype.pct
import csstype.px
import emotion.react.css
import kotlinx.browser.window
import org.w3c.dom.events.Event
import react.*
import react.dom.html.ReactHTML
import react.dom.html.ReactHTML.canvas
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class TrippyAbout : ExternalCanvas() {
    override val name: String = "TrippyAbout"

    private var intervalId: Int? = null

    override val component: FC<Props>
        get() = FC {
            val (size, setSize) = useState(3)
            val (state, _) = useState(Graph(size))

            fun getX(x: Int, width: Int) =
                (width / 2.0) + ((width / 2.1) * cos(((x * (360.0 / size)) - 90.0) * PI / 180.0))

            fun getY(y: Int, height: Int) =
                (height / 2.0) + ((height / 2.1) * sin(((y * (360.0 / size)) - 90.0) * PI / 180.0))

            fun getColor(n: Int) = (n * (360.0 / size)).toInt()

            fun drawLine(fromX: Double, fromY: Double, toX: Double, toY: Double, color: String) {
                renderingContext.beginPath()
                renderingContext.moveTo(fromX, fromY)
                renderingContext.lineTo(toX, toY)
                renderingContext.lineWidth = 2.0
                renderingContext.strokeStyle = color
                renderingContext.stroke()
            }

            fun drawState() {
                val c = canvasElement
                state.elements.forEach { element ->
                    element.outgoing.forEach { to ->
                        val fromX = getX(element.value, c.width)
                        val fromY = getY(element.value, c.height)
                        val toX = getX(to, c.width)
                        val toY = getY(to, c.height)
                        drawLine(fromX, fromY, toX, toY, "hsl(${getColor(element.value)},100%,50%)")
                    }
                }
            }

            fun draw() {
                renderingContext.clear()
                drawState()
            }

            val resizeHandler: (Event) -> Unit = {
                draw()
            }

            canvas {
                css(Classes.canvas)
                id = canvasId
            }

            ReactHTML.div {
                css {
                    width = 100.pct
                    maxWidth = 800.px
                }
                sliderInput {
                    value = size.toDouble().toString()
                    min = 3.0
                    max = 25.0
                    step = 2.0
                    onChange = {
                        setSize(it.target.value.toDouble().toInt())
                    }
                }
            }

            useEffect(size) {
                state.reset(size)
                draw()
            }

            useEffectOnce {
                canvasElement.setDimensions(800, 800)
                addEventListener("resize" to resizeHandler)
                draw()
            }
        }

    private fun clearInterval() {
        intervalId?.let { window.clearInterval(it) }
        intervalId = null
    }

    override fun cleanUp() {
        clearInterval()
    }

    override fun initialize() {}

    init {
        initEventListeners()
    }
}