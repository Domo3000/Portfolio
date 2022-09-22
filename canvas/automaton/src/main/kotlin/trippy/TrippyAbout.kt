package trippy

import canvas.ExternalCanvas
import canvas.clear
import canvas.setDimensions
import css.ClassNames
import css.Classes
import csstype.Float
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
                state.elements.forEach { element ->
                    val color = "hsl(${getColor(element.value)},100%,50%)"
                    val fromX = getX(element.value, canvasElement.width)
                    val fromY = getY(element.value, canvasElement.height)
                    element.outgoing.forEach { to ->
                        val toX = getX(to, canvasElement.width)
                        val toY = getY(to, canvasElement.height)
                        drawLine(fromX, fromY, toX, toY, color)
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
                        setSize(it.target.value.toDouble().toInt())
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