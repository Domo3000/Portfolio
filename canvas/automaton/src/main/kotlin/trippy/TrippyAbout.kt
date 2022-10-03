package trippy

import canvas.ExternalCanvas
import canvas.clear
import canvas.setDimensions
import css.ClassNames
import css.Classes
import csstype.Color
import csstype.Float
import csstype.pct
import csstype.px
import emotion.react.css
import kotlinx.browser.window
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.events.Event
import react.*
import react.dom.html.ReactHTML
import react.dom.html.ReactHTML.canvas
import kotlin.math.*

external interface ColorProps : Props {
    var text: String
    var color: Int
}

val colorBlock = FC<ColorProps> { props ->
    val colour = "hsl(${props.color},100%,50%)".unsafeCast<Color>() // TODO move to utils
    ReactHTML.span {
        css {
            width = 20.px
            height = 20.px
            color = colour
            backgroundColor = colour
        }
        +props.text
    }
}

fun getColor(n: Int, size: Int) = (n * (360.0 / size)).toInt()

class State(size: Int = 3) {
    private val graph = Graph(size)

    fun reset(size: Int) {
        graph.reset(size)
    }

    val elements
        get() = graph.elements

    fun getX(x: Int, width: Int, factor: Double = 2.2) =
        (width / 2.0) + ((width / factor) * cos(((x * (360.0 / graph.size())) - 90.0) * PI / 180.0))

    fun getY(y: Int, height: Int, factor: Double = 2.2) =
        (height / 2.0) + ((height / factor) * sin(((y * (360.0 / graph.size())) - 90.0) * PI / 180.0))

    fun getColor(n: Int) = getColor(n, graph.size())

    fun drawLine(
        renderingContext: CanvasRenderingContext2D,
        fromX: Double,
        fromY: Double,
        toX: Double,
        toY: Double,
        color: String
    ) {
        renderingContext.beginPath()
        renderingContext.moveTo(fromX, fromY)
        renderingContext.lineTo(toX, toY)
        renderingContext.lineWidth = 2.0
        renderingContext.strokeStyle = color
        renderingContext.stroke()
    }

    // TODO move to utils
    fun drawCircle(renderingContext: CanvasRenderingContext2D, x: Double, y: Double, color: String) {
        renderingContext.beginPath()
        renderingContext.arc(x, y, 5.0, 0.0, 2 * PI)
        renderingContext.lineWidth = 5.0
        renderingContext.stroke()
    }

    fun draw(canvasElement: HTMLCanvasElement, renderingContext: CanvasRenderingContext2D) {
        graph.elements.forEach { element ->
            val color = "hsl(${getColor(element.value)},100%,50%)"
            val fromX = getX(element.value, canvasElement.width)
            val fromY = getY(element.value, canvasElement.height)
            element.outgoing.forEach { to ->
                val toX = getX(to, canvasElement.width)
                val toY = getY(to, canvasElement.height)
                drawLine(renderingContext, fromX, fromY, toX, toY, color)
            }
            drawCircle(
                renderingContext,
                getX(element.value, canvasElement.width, 2.1),
                getY(element.value, canvasElement.width, 2.1),
                color
            )
        }
    }
}

class TrippyAbout : ExternalCanvas() {
    override val name: String = "TrippyAbout"

    private var intervalId: Int? = null

    override val component: FC<Props>
        get() = FC {
            val (size, setSize) = useState(3)
            val (state, _) = useState(State(size))

            fun draw() {
                renderingContext.clear()
                state.draw(canvasElement, renderingContext)
            }

            val resizeHandler: (Event) -> Unit = {
                draw()
            }

            ReactHTML.div {
                css(ClassNames.phoneFullWidth) {
                    width = 50.pct
                    float = Float.left
                    marginRight = 5.px
                }

                canvas {
                    css(Classes.canvas)
                    id = canvasId
                }

                sliderInput {
                    value = size.toDouble().toString()
                    min = 3.0
                    max = 25.0
                    step = 2.0
                    onChange = {
                        val newSize = it.target.value.toDouble().toInt()
                        state.reset(newSize)
                        setSize(newSize)
                        draw()
                    }
                }
            }

            ReactHTML.div {
                ReactHTML.details {
                    ReactHTML.summary {
                        +"Cellular Automata"
                    }
                    ReactHTML.p {
                        +"This is another example of a cellular automaton, where the next state is calculated by looking at the neighbors of each cell."
                    }
                }

                ReactHTML.details {
                    ReactHTML.summary {
                        +"Moore Neighborhood"
                    }
                    ReactHTML.p {
                        +"This CA uses a "
                        ReactHTML.a {
                            href = "https://en.wikipedia.org/wiki/Moore_neighborhood"
                            +"Moore Neighborhood"
                        }
                        +" which looks at the 8 cells surrounding a cell."
                    }
                    ReactHTML.p {
                        +"For each cell it checks if there's more than the threshold of a hand sign that defeats it in its neighborhood." // TODO rewrite
                    }
                    ReactHTML.p {
                        +"For each battle the threshold will randomly be set to 2 or 3." // TODO slider?
                    }
                }

                ReactHTML.details {
                    ReactHTML.summary {
                        +"Scalable Rock Paper Scissors"
                    }
                    ReactHTML.p {
                        +"The graphic shows how winning hands are calculated for a Rock Paper Scissor game with $size hand signs."
                    }
                    ReactHTML.p {
                        colorBlock {
                            color = getColor(state.elements[0].value, size)
                            text = "se"
                        }
                        +" defeats "
                        colorBlock {
                            color = getColor(state.elements[round(size / 3.0).toInt()].value, state.elements.size)
                            text = "cr"
                        }
                        +" and gets defeated by "
                        colorBlock {
                            color = getColor(state.elements[round(size * 2 / 3.0).toInt()].value, state.elements.size)
                            text = "et"
                        }
                    }
                }
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