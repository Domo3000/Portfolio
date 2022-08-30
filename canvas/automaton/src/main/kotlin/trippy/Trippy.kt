package trippy

import canvas.ExternalCanvas
import canvas.drawRectangle
import canvas.setDimensions
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

class LookupMap(size: Int) { // TODO test if actually faster than directly accessing
    private val graph = Graph(size)
    private val map: HashMap<Pair<Int, Int>, Boolean> = hashMapOf()

    fun defeats(defender: Int, fighter: Int) = map.getOrPut(defender to fighter) {
        graph.get(defender)!!.incoming.contains(fighter)
    }
}

class ElementArray(x: Int, y: Int, s: Int) : WrappingArray<Int>(x, y) {
    var states = s
    var lookupMap = LookupMap(states)
    private val random = Random(Date.now().toInt())

    override val elements = run {
        Array(sizeY) {
            Array(sizeX) {
                random.nextInt() mod states
            }.toArrayList()
        }.toArrayList()
    }

    private val withPosition
        get() = elements.mapIndexed { y, list ->
            list.mapIndexed { x, element -> Position(x, y) to element }
        }.flatten()

    fun setStates(s: Int) {
        states = s
        lookupMap = LookupMap(states)
        setAll { _, _ ->
            random.nextInt() mod states
        }
    }

    fun runStep() {
        val threshold = 1

        withPosition.map { (position, node) ->
            val incoming = (-1..1).map { offsetY ->
                (-1..1).mapNotNull { offsetX ->
                    if (offsetX == 0 && offsetY == 0) {
                        null
                    } else {
                        get(position.first + offsetX, position.second + offsetY)
                    }
                }
            }.flatten().filter {
                lookupMap.defeats(node, it)
            }

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

    private var frameId: Int? = null

    override val component: FC<Props>
        get() = FC {
            val size = 200
            val (states, setStates) = useState(3)
            val (running, setRunning) = useState(true)
            val (state, _) = useState(ElementArray(size, size * 3 / 4, states))

            fun drawState() {
                val r = renderingContext
                val sizeX = state.sizeX
                val sizeY = state.sizeY
                val width = canvasElement.width
                val height = canvasElement.height
                for (y in 0 until state.sizeY) {
                    for (x in 0 until state.sizeX) {
                        r.drawRectangle(
                            x,
                            y,
                            sizeX,
                            sizeY,
                            width,
                            height,
                            "hsl(${(state.get(x, y) * (360.0 / state.states)).toInt()},100%,50%)"
                        )
                    }
                }
            }

            fun run() {
                state.runStep()
                drawState()
                frameId = window.requestAnimationFrame { run() }
            }

            fun stop() {
                clearInterval()
                setRunning(false)
            }

            val resizeHandler: (Event) -> Unit = {
                drawState()
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
                    frameId?.let { clearInterval() } ?: run {
                        frameId = window.requestAnimationFrame { run() }
                    }
                }
            }

            useEffect(states) {
                state.setStates(states)
                drawState()
            }

            useEffectOnce {
                canvasElement.setDimensions()
                addEventListener("resize" to resizeHandler)
                drawState()
                frameId = window.requestAnimationFrame { run() }
            }
        }

    private fun clearInterval() {
        frameId?.let { window.cancelAnimationFrame(it) }
        frameId = null
    }

    override fun cleanUp() {
        clearInterval()
    }

    override fun initialize() {}

    init {
        initEventListeners()
    }
}