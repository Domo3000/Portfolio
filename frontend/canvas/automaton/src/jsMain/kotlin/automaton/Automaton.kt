package automaton

import canvas.ExternalCanvas
import canvas.clear
import canvas.drawRectangle
import canvas.setDimensions
import css.ClassNames
import css.Classes
import emotion.react.css
import kotlinx.browser.window
import props.Button
import props.button
import props.buttonRow
import props.sliderInput
import react.*
import react.dom.html.ReactHTML
import react.dom.html.ReactHTML.canvas
import utils.WrappingArray
import utils.mod
import web.cssom.Auto
import web.cssom.Float
import web.cssom.pct
import web.cssom.px
import web.events.Event
import kotlin.js.Date
import kotlin.random.Random

class BooleanArray(x: Int, y: Int) : WrappingArray<Boolean>(x, y) {
    override fun init(x: Int, y: Int): Boolean = false

    fun getParentValue(x: Int, y: Int, default: Boolean?) = Rule.toNumber(
        get(x - 1, y - 1, default),
        get(x, y - 1, default),
        get(x + 1, y - 1, default)
    )

    fun flip(x: Int, y: Int) {
        elements[y][x] = !elements[y][x]
    }

    fun addRow() {
        elements.removeAt(0)
        elements.add(ArrayList((0 until sizeX).map { false }))
    }

    fun clear() {
        (0..sizeY).forEach { addRow() }
    }
}

class State(size: Int) {
    val elements = BooleanArray(size, size + 2)

    private var currentRow = 0

    fun reset() {
        currentRow = 0
        elements.clear()
    }

    fun run(rules: Int, default: Boolean?) {
        if (currentRow < elements.sizeY - 1) {
            currentRow++
        } else {
            elements.addRow()
        }

        (0 until elements.sizeX).map { i ->
            if (rules.contains(elements.getParentValue(i, currentRow, default))) {
                elements.flip(i, currentRow)
            }
        }
    }
}

class Automaton : ExternalCanvas() {
    override val name: String = "Automaton"

    private var intervalId: Int? = null

    override val component: FC<Props>
        get() = FC {
            val (default, setDefault) = useState<Boolean?>(null)
            val (rules, setRules) = useState(30)
            val (delay, setDelay) = useState(50)
            val (randomNr, setRandom) = useState(50)
            val (running, setRunning) = useState(false)
            val (state, _) = useState(State(138))
            val random = Random(Date.now().toInt())

            fun drawState() {
                val r = renderingContext
                val sizeX = state.elements.sizeX + 2
                val sizeY = state.elements.sizeY
                val width = canvasElement.width
                val height = canvasElement.height
                for (y in 0 until state.elements.sizeY) {
                    for (x in -1..state.elements.sizeX) {
                        if (state.elements.get(x, y, default)) {
                            r.drawRectangle(
                                x + 1,
                                y,
                                sizeX,
                                sizeY,
                                width,
                                height,
                                "Black"
                            )
                        }
                    }
                }
            }

            fun draw() {
                renderingContext.clear()
                drawState()
            }

            fun runStep() {
                state.run(rules, default)
                draw()
            }

            fun stop() {
                clearInterval()
                setRunning(false)
            }

            val resizeHandler: (Event) -> Unit = {
                draw()
            }

            ReactHTML.div {
                css(Classes.project)

                canvas {
                    css(Classes.canvas)
                    id = canvasId
                }

                ReactHTML.div {
                    css(ClassNames.phoneFullWidth) {
                        width = 50.pct
                        minWidth = 300.px
                        float = Float.left
                    }

                    ReactHTML.div {
                        css {
                            width = 282.px // 3 * 90 + 3 * 4 (margin left + right)
                            margin = Auto.auto
                        }

                        ruleRow {
                            active = rules
                            ruleSetter = setRules
                            wrapping = default
                            wrappingSetter = setDefault
                        }
                    }
                }

                ReactHTML.div {
                    css(ClassNames.phoneFullWidth) {
                        width = 50.pct
                        float = Float.left
                    }

                    sliderInput {
                        value = delay.toDouble().toString()
                        min = 0.0
                        max = 200.0
                        step = 10.0
                        onChange = {
                            stop()
                            setDelay(it.target.value.toDouble().toInt())
                        }
                    }
                    button {
                        text = if (running) "Stop" else "Play at ${delay}ms"
                        width = 100.0
                        onClick = {
                            setRunning(!running)
                            intervalId?.let { clearInterval() } ?: run {
                                intervalId = window.setInterval({ runStep() }, delay)
                            }
                        }
                    }

                    buttonRow {
                        buttons = listOf(
                            Button("Clear", false) {
                                stop()
                                state.reset()
                                state.elements.clear()
                                draw()
                            },
                            Button("Middle", false) {
                                stop()
                                state.reset()
                                state.elements.flip(state.elements.sizeX / 2, 0)
                                draw()
                            },
                            Button("Random $randomNr", false) {
                                stop()
                                state.reset()
                                (1..randomNr).forEach { _ ->
                                    state.elements.flip(random.nextInt() mod state.elements.sizeX, 0)
                                }
                                draw()
                            }
                        )
                    }

                    sliderInput {
                        value = randomNr.toDouble().toString()
                        min = 1.0
                        max = (state.elements.sizeX - 1).toDouble()
                        step = 1.0
                        onChange = {
                            setRandom(it.target.value.toDouble().toInt())
                        }
                    }
                }
            }

            useEffect(rules) {
                stop()
            }

            useEffect(default) {
                stop()
                draw()
            }

            useEffectOnce {
                canvasElement.setDimensions(700, 700)
                addEventListener("resize" to resizeHandler)
                state.elements.flip(state.elements.sizeX / 2, 0)
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