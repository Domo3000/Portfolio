package trippy

import canvas.ExternalCanvas
import canvas.clear
import canvas.drawCircle
import canvas.setDimensions
import css.ClassNames
import css.Classes
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
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.text.Typography.nbsp

external interface ColorProps : Props {
    var text: String
    var color: Int
}

val colorBlock = FC<ColorProps> { props ->
    val colour = props.color.hslColor()
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
            renderingContext.drawCircle(
                getX(element.value, canvasElement.width, 2.1),
                getY(element.value, canvasElement.width, 2.1),
                10.0
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
                        +"This is another example of a cellular automaton."
                    }
                    ReactHTML.p {
                        +"The next state is calculated by looking at the neighbors of each cell and playing Rock Paper Scissors."
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
                        +"For each cell it checks if there's more than the threshold of a hand sign that defeats it in its neighborhood."
                    }
                    ReactHTML.p {
                        +"If there's the same amount of different hands of winning neighbors it chooses a random one."
                    }
                }

                ReactHTML.details {
                    ReactHTML.summary {
                        +"Scalable Rock Paper Scissors"
                    }
                    ReactHTML.p {
                        +"The graphic shows how winning hands are calculated for a Rock Paper Scissor game with $size hand signs."
                    }
                    val node = state.elements[0]
                    ReactHTML.p {
                        colorBlock {
                            color = getColor(node.value, size)
                            text = "$nbsp$nbsp"
                        }
                        +" defeats "
                        node.outgoing.forEach {
                            colorBlock {
                                color = getColor(state.elements[it].value, state.elements.size)
                                text = "$nbsp$nbsp"
                            }
                        }
                    }
                    ReactHTML.p {
                        colorBlock {
                            color = getColor(node.value, size)
                            text = "$nbsp$nbsp"
                        }
                        +" gets defeated by "
                        node.incoming.reversed().forEach {
                            colorBlock {
                                color = getColor(state.elements[it].value, state.elements.size)
                                text = "$nbsp$nbsp"
                            }
                        }
                    }
                }

                ReactHTML.details {
                    ReactHTML.summary {
                        +"Idea"
                    }
                    ReactHTML.a {
                        href = "https://softologyblog.wordpress.com/2018/03/23/rock-paper-scissors-cellular-automata/"
                        +"Softology's Blog"
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