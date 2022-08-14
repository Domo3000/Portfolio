package trippy

import canvas.*
import css.Classes
import emotion.react.css
import kotlinx.browser.window
import org.w3c.dom.events.Event
import react.*
import react.dom.html.ReactHTML.canvas
import utils.WrappingArray
import utils.mod
import utils.toArrayList
import kotlin.js.Date
import kotlin.random.Random

private typealias Position = Pair<Int, Int>

class ElementArray(x: Int, y: Int, s: Int) : WrappingArray<Int>(x, y) {
    var states = s
    private val random = Random(Date.now().toInt())

    override val elements = run {
        val graph = Graph(states)

        Array(sizeY) {
            Array(sizeX) {
                graph.get(random.nextInt() mod states)!!.value
            }.toArrayList()
        }.toArrayList()
    }

    private val withPosition
        get() = elements.mapIndexed { y, list ->
            list.mapIndexed { x, element -> Position(x, y) to element }
        }.flatten()

    fun setStates(s: Int) {
        states = s
        val graph = Graph(states)
        setAll { _, _ ->
            graph.get(random.nextInt() mod states)!!.value
        }
    }

    fun runStep() {
        val threshold = 1
        val graph = Graph(states)

        withPosition.map { (position, node) ->
            val incoming = (-1..1).map { offsetY ->
                (-1..1).mapNotNull { offsetX ->
                    if (offsetX == 0 && offsetY == 0) {
                        null
                    } else {
                        get(position.first + offsetX, position.second + offsetY)
                    }
                }
            }.flatten().filter { graph.get(node)!!.incoming.contains(it) }

            position to incoming
        }.forEach { (position, incoming) ->
            incoming
                .distinct()
                .map { n -> n to incoming.count { n == it } }
                .maxByOrNull { it.second }
                ?.let { (n, count) ->
                    if (count > threshold + (random.nextInt() mod 2)) {
                        set(position.first, position.second, n)
                    }
                }
        }
    }
}

//TODO optimize
class Trippy : ExternalCanvas() {
    override val name: String = "Trippy"

    private var intervalId: Int? = null

    override val component: FC<Props>
        get() = FC {
            val size = 150
            val (states, setStates) = useState(3)
            val (running, setRunning) = useState(true)
            val (state, _) = useState(ElementArray(size, size * 3 / 4, states))

            fun drawState() {
                val c = canvasElement
                for (y in 0 until state.sizeY) {
                    for (x in 0 until state.sizeX) {
                        val element = state.get(x, y)
                        val relativeX = c.getRelativeX(x, state.sizeX)
                        val relativeY = c.getRelativeY(y, state.sizeY)
                        val elementWidth = c.getElementWidth(state.sizeX)
                        val elementHeight = c.getElementHeight(state.sizeY)

                        renderingContext.fillStyle = "hsl(${(element * (360.0 / state.states)).toInt()},100%,50%)"
                        renderingContext.fillRect(
                            relativeX,
                            relativeY,
                            elementWidth,
                            elementHeight
                        )
                    }
                }
            }

            fun draw() {
                renderingContext.drawBackground()
                drawState()
            }

            fun run() {
                state.runStep()
                draw()
            }

            fun stop() {
                clearInterval()
                setRunning(false)
            }

            val resizeHandler: (Event) -> Unit = {
                canvasElement.resetDimensions()
                draw()
            }

            canvas {
                css(Classes.canvas)
                id = canvasId
            }

            sliderInput {
                value = states.toDouble().toString()
                min = 3.0
                max = 25.0
                step = 2.0
                onChange = {
                    stop()
                    setStates(it.target.value.toDouble().toInt())
                }
            }

            button {
                text = if (running) "Stop" else "Play"
                disabled = false
                width = 100.0
                onClick = {
                    setRunning(!running)
                    intervalId?.let { clearInterval() } ?: run {
                        intervalId = window.setInterval({ run() }, 0)
                    }
                }
            }

            useEffect(states) {
                state.setStates(states)
                draw()
            }

            useEffectOnce {
                addEventListener("resize" to resizeHandler)
                canvasElement.resetDimensions()
                draw()
                intervalId = window.setInterval({ run() }, 0)
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