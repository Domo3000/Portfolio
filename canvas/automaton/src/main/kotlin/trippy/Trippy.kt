package trippy

import canvas.ExternalCanvas
import canvas.drawRectangle
import canvas.setDimensions
import css.Classes
import csstype.NamedColor
import csstype.None
import csstype.pct
import emotion.react.css
import kotlinx.browser.window
import org.w3c.dom.events.Event
import react.*
import react.dom.html.InputType
import react.dom.html.ReactHTML.canvas
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.label
import utils.WrappingArray
import utils.mod
import kotlin.js.Date
import kotlin.random.Random

class ElementArray(x: Int, y: Int, s: Int) : WrappingArray<Int>(x, y) {
    var states = s
    var threshold = 1
    var randomMod = 2
    private val random = Random(Date.now().toInt())

    override fun init(x: Int, y: Int): Int = random.nextInt() mod states

    /*
        true if fighter wins
    */
    fun fight(defender: Int, fighter: Int): Boolean = if (defender == fighter) {
        false
    } else if (defender > fighter) {
        defender - fighter <= states / 2
    } else {
        states - fighter + defender <= states / 2
    }

    fun randomize() {
        setAll { _, _ ->
            random.nextInt() mod states
        }
    }

    fun setStates(s: Int) {
        states = s
        setAll { _, _ ->
            random.nextInt() mod states
        }
    }

    fun setSize(n: Int) {
        sizeX = n
        sizeY = n * 3 / 4

        elements.clear()
        elements.addAll(
            Array(sizeY) {
                Array(sizeX) {
                    random.nextInt() mod states
                }.toMutableList()
            }.toMutableList()
        )
    }

    fun runStep() {
        val next: List<List<Int>> = elements.mapIndexed { y, list ->
            list.mapIndexed { x, element ->
                val incoming = (-1..1).map { offsetY ->
                    (-1..1).mapNotNull { offsetX ->
                        if (offsetX == 0 && offsetY == 0) {
                            null
                        } else {
                            get(x + offsetX, y + offsetY)
                        }
                    }
                }.flatten().filter { fight(element, it) }

                val grouping = incoming.groupingBy { it }.eachCount()
                grouping.maxByOrNull { it.value }?.let { entry ->
                    val count = entry.value

                    val r = if (randomMod == 1) {
                        0
                    } else {
                        (random.nextInt() mod randomMod)
                    }

                    if (count > threshold + r) {
                        grouping.filter { it.value == count }.keys.random()
                    } else {
                        null
                    }
                } ?: element
            }
        }
        elements.clear()
        elements.addAll(next.map { ArrayList(it) })
    }
}

class Trippy : ExternalCanvas() {
    override val name: String = "Trippy"

    private var frameId: Int? = null

    override val component: FC<Props>
        get() = FC {
            val (size, setSize) = useState(100)
            val (threshold, setThreshold) = useState(1)
            val (randomMod, setRandomMod) = useState(2)
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

            /*
            TODO figure out why this doesn't work
            datalist {
                id = "list"
                listOf(40, 100, 160, 200, 400).forEach {
                    option {
                        value = it
                        label = "$it"
                    }
                }
            }
             */

            label {
                htmlFor = "sizeInput"
                +"Size: $size"
            }
            input {
                id = "sizeInput"
                type = InputType.range
                min = "40.0"
                max = "400.0"
                step = 50.0
                value = size.toString()
                list = "list"
                css {
                    appearance = None.none
                    width = 100.pct
                    outline = None.none
                    backgroundColor = NamedColor.darkgray
                }
                onChange = {
                    stop()

                    val allowedValues = listOf(40, 100, 160, 200, 400)
                    val newSize = it.target.value.toDouble().toInt()

                    allowedValues.mapIndexed { index, value ->
                        val previous: Int? = allowedValues.getOrNull(index - 1)?.let { prev ->
                            (value + prev) / 2
                        }
                        val next: Int? = allowedValues.getOrNull(index + 1)?.let { next ->
                            (value + next) / 2
                        }
                        (previous?: value)..(next?: value) to value
                    }.find { (range, _) -> newSize in range }?.let { (_, value) ->
                        setSize(value)
                        state.setSize(value)
                        drawState()
                    }
                }
            }

            label {
                htmlFor = "statesInput"
                +"States: $states"
            }
            sliderInput {
                id = "statesInput"
                value = states.toDouble().toString()
                min = 3.0
                max = 25.0
                step = 2.0
                onChange = {
                    stop()
                    setStates(it.target.value.toDouble().toInt())
                }
            }

            label {
                htmlFor = "thresholdInput"
                +"Threshold: $threshold"
            }
            sliderInput {
                id = "thresholdInput"
                value = threshold.toDouble().toString()
                min = 0.0
                max = 3.0
                step = 1.0
                onChange = {
                    stop()
                    val newValue = it.target.value.toDouble().toInt()
                    state.threshold = newValue
                    setThreshold(newValue)
                }
            }

            label {
                htmlFor = "randomInput"
                val r = if (randomMod == 1) {
                    "0"
                } else {
                    "0 - ${randomMod - 1}"
                }
                +"Random Threshold: $r"
            }
            sliderInput {
                id = "randomInput"
                value = randomMod.toDouble().toString()
                min = 1.0
                max = 4.0
                step = 1.0
                onChange = {
                    stop()
                    val newValue = it.target.value.toDouble().toInt()
                    state.randomMod = newValue
                    setRandomMod(newValue)
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

            button {
                text = "Randomize"
                disabled = false
                width = 100.0
                onClick = {
                    stop()
                    state.randomize()
                    drawState()
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