package automaton

import canvas.*
import css.Classes
import csstype.NamedColor
import csstype.px
import emotion.react.css
import kotlinx.browser.window
import org.w3c.dom.events.Event
import react.*
import react.dom.html.ReactHTML
import react.dom.html.ReactHTML.canvas
import utils.WrappingArray
import utils.mod
import kotlin.js.Date
import kotlin.random.Random

class BooleanArray(x: Int, y: Int) : WrappingArray<Boolean>(x, y) {
    override val elements: ArrayList<ArrayList<Boolean>> =
        ArrayList((0 until sizeY).map { ArrayList((0 until sizeX).map { false }) })

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

    fun clear() { // TODO flip all?
        (0..sizeY).forEach { addRow() }
    }
}

class State(size: Int) {
    val elements = BooleanArray(size, size * 3 / 4)

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
            val (state, _) = useState(State(149))
            val random = Random(Date.now().toInt())

            fun drawState() {
                val c = canvasElement
                for (y in 0 until state.elements.sizeY) {
                    for (x in -1..state.elements.sizeX) {
                        if (state.elements.get(x, y, default)) {
                            val relativeX = c.getRelativeX(x + 1, state.elements.sizeX + 2)
                            val relativeY = c.getRelativeY(y, state.elements.sizeY)
                            val elementWidth = c.getElementWidth(state.elements.sizeX + 2)
                            val elementHeight = c.getElementHeight(state.elements.sizeY)

                            renderingContext.fillStyle = NamedColor.black // TODO use everywhere
                            renderingContext.fillRect(
                                relativeX,
                                relativeY,
                                elementWidth,
                                elementHeight
                            )
                        }
                    }
                }
            }

            fun draw() {
                renderingContext.drawBackground()
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
                canvasElement.resetDimensions()
                draw()
            }

            ReactHTML.div {
                ReactHTML.div {
                    css {
                        maxWidth = 300.px
                    }
                    ruleRow {
                        active = rules
                        ruleSetter = setRules
                        wrapping = default
                        wrappingSetter = setDefault
                    }
                }
                ReactHTML.div {
                    ReactHTML.details {
                        css(Classes.text)

                        ReactHTML.summary {
                            +"Cellular Automata"
                        }
                        ReactHTML.span {
                            +"Simple rules can lead to complex behaviour."
                        }
                    }
                }
            }

            canvas {
                css(Classes.canvas)
                id = canvasId
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
                disabled = false
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

            useEffect(rules) {
                stop()
            }

            useEffect(default) {
                stop()
                draw()
            }

            useEffectOnce {
                addEventListener("resize" to resizeHandler)
                state.elements.flip(state.elements.sizeX / 2, 0)
                canvasElement.resetDimensions()
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