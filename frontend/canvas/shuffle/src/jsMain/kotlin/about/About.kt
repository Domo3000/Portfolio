package about

import Requests
import Result
import canvas.*
import css.Classes
import emotion.react.css
import kotlinx.browser.window
import props.button
import react.*
import react.dom.html.ReactHTML
import web.cssom.Color
import web.cssom.TextAlign
import web.events.Event
import kotlin.math.log
import kotlin.math.pow

private data class Details(val position: Position? = null)

private fun Pair<Position, ShuffleCounter>.toPrettyString() = "${first.toPrettyString()} = ${second.counter}"

private fun List<Pair<Position, ShuffleCounter>>.highest(n: Int) = this.sortedByDescending { it.second.counter }.take(n)

private class ShuffleState(var outShuffle: Boolean, initialSize: Int, setSize: StateSetter<Int>) {
    val outState = State(initialSize, true, setSize)
    val inState = State(initialSize, false, setSize)

    fun getState() = if (outShuffle) outState else inState
}

class About : ExternalCanvas() {
    override val name: String = "ShuffleAbout"

    var frameId: Int? = null

    override val component: FC<Props>
        get() = FC {
            val (size, setSize) = useState(9)
            val (haveFinished, setHaveFinished) = useState(0)
            val (showDetails, setShowDetails) = useState(Details())
            val (outShuffle, setOutShuffle) = useState(true)
            val (shuffleState, _) = useState(ShuffleState(outShuffle, size, setSize))
            val (precalculatedInResults, setPrecalculatedInResults) = useState(Result(mutableListOf()))
            val (precalculatedOutResults, setPrecalculatedOutResults) = useState(Result(mutableListOf()))

            fun getPower(size: Int) = when (size) {
                in 0..100 -> 10.0 + size / 10.0
                in 100..200 -> 20.0 + (size - 100) / 20.0
                else -> 25.0
            }

            fun getColor(n: Long): String {
                val state = shuffleState.getState()

                if (n == 0L) {
                    return "DimGray"
                }

                val range = 300
                val power = getPower(state.size)
                val scaled = (log(n.toDouble(), 2.0) * (range / power)).toInt()

                if (scaled >= range) {
                    return "Red"
                }

                return "hsl(${range - scaled},100%,50%)"
            }

            fun drawElement(
                position: Position,
                element: ShuffleCounter?
            ) {
                val state = shuffleState.getState()

                val x = canvasElement.getRelativeX(position.x, state.size)
                val y = canvasElement.getRelativeY(position.y, state.size)
                val elementWidth = canvasElement.getElementWidth(state.size)
                val elementHeight = canvasElement.getElementHeight(state.size)

                renderingContext.fillStyle = getColor(element?.counter ?: 0L)
                renderingContext.fillRect(
                    x,
                    y,
                    elementWidth,
                    elementHeight
                )

                if (element?.finished == true) {
                    renderingContext.fillStyle = "Black"
                    renderingContext.fillRect(
                        x + elementWidth / 5.0 * 2.0,
                        y + elementHeight / 5.0 * 2.0,
                        elementWidth / 5.0,
                        elementHeight / 5.0
                    )
                }
            }

            fun drawState(state: State) {
                state.decks.forEach { list ->
                    list.forEach { (position, counter) ->
                        drawElement(Position(position.y, position.x), null)
                        drawElement(position, counter)
                    }
                }
            }

            fun draw() {
                canvasElement.resetDimensions()
                renderingContext.clear()
                drawState(shuffleState.getState())
            }

            fun singleStep(position: Position, element: ShuffleCounter, repetitions: Int) {
                var c = repetitions

                while (!element.finished && c-- > 0) {
                    element.deck.pile(position.x + 2)
                    element.counter += 1
                    element.finished = element.deck.sorted()
                }
            }

            fun runStep() {
                val state = shuffleState.getState()

                if (state.unfinishedCount < 10 && state.size < 199) {
                    state.addRow()
                    draw()
                } else if (state.unfinished.isEmpty()) {
                    clearInterval()
                }

                val repetitions = 50000 / state.unfinishedCount

                state.unfinished.forEach { (position, element) ->
                    singleStep(position, element, repetitions)
                    drawElement(position, element)
                }

                setHaveFinished(state.finishedCount)

                frameId = window.requestAnimationFrame { runStep() }
            }

            val resizeHandler: (Event) -> Unit = {
                draw()
            }

            ReactHTML.div {
                ReactHTML.strong {
                    +"Pile-Shuffling Loops"
                }
                ReactHTML.p {
                    +"Which configurations cause the longest loop?"
                }
            }

            ReactHTML.canvas {
                css(Classes.canvas)
                id = canvasId
                onClick = {
                    val bounds = canvasElement.getBoundingClientRect()
                    val x = it.clientX - bounds.left
                    val y = it.clientY - bounds.top

                    val relativeX = canvasElement.getX(x, size)
                    val relativeY = canvasElement.getY(y, size)

                    if (relativeX in 0..relativeY &&
                        relativeY >= 0 &&
                        relativeX < size
                        && relativeY < size &&
                        (relativeX != showDetails.position?.x ||
                                relativeY != showDetails.position.y)
                    ) {
                        setShowDetails(Details(Position(relativeX, relativeY)))
                    } else {
                        setShowDetails(Details())
                    }
                }
            }

            ReactHTML.div {
                showDetails.position?.let { (x, y) ->
                    val element = shuffleState.getState().decks[y][x].second
                    ReactHTML.p {
                        +(showDetails.position to element).toPrettyString()
                        if (!element.finished) {
                            +"+"
                        }
                    }
                } ?: run {
                    ReactHTML.p {
                        +"Click an element to see its progress."
                    }
                }

                ReactHTML.p {
                    +"$haveFinished/${(((size) / 2.0) * (size + 1)).toInt()} have finished!"
                }
            }

            ReactHTML.div {
                ShuffleDescription {
                    this.outShuffle = outShuffle
                }

                ReactHTML.details {
                    ReactHTML.summary {
                        +"Legend"
                    }

                    val power = getPower(size).toInt()

                    (0..(power + 1)).forEach {
                        val n = 2.0.pow(it).toLong()
                        ReactHTML.div {
                            css {
                                backgroundColor = Color(getColor(n))
                                textAlign = TextAlign.center
                            }
                            +"$n"
                        }
                    }
                }

                if (precalculatedInResults.rows.isNotEmpty() && precalculatedOutResults.rows.isNotEmpty()) {
                    ReactHTML.details {
                        ReactHTML.summary {
                            +"Highest"
                        }

                        HighestTable {
                            outHighest = shuffleState.outState.decks.flatten().highest(10)
                            inHighest = shuffleState.inState.decks.flatten().highest(10)
                        }
                    }
                }

                if (precalculatedInResults.rows.isEmpty()) {
                    button {
                        text = "Use precalculated results instead"
                        onClick = {
                            clearInterval()

                            Requests.getMessage("/static/in-counters.json") {
                                setPrecalculatedInResults(it as Result)
                            }

                            Requests.getMessage("/static/out-counters.json") {
                                setPrecalculatedOutResults(it as Result)
                            }
                        }
                    }
                }
            }

            button {
                text = "Switch to ${if (outShuffle) "In" else "Out"}-Shuffle"
                onClick = {
                    setOutShuffle(!outShuffle)
                }
            }

            fun handlePrecalculatesResults(result: Result, state: State) {
                if (result.rows.isNotEmpty()) {
                    state.loadFromResult(result)
                    setHaveFinished(result.rows.sumOf { it.counters.size + 1 } + 1)
                    draw()
                }
            }

            useEffect(outShuffle) {
                setShowDetails(Details())
                shuffleState.outShuffle = outShuffle
                setSize(shuffleState.getState().size)
                draw()
            }

            useEffect(precalculatedInResults) {
                if(precalculatedOutResults.rows.isNotEmpty()) {
                    handlePrecalculatesResults(precalculatedInResults, shuffleState.inState)
                    handlePrecalculatesResults(precalculatedOutResults, shuffleState.outState)
                }
            }

            useEffect(precalculatedOutResults) {
                if(precalculatedInResults.rows.isNotEmpty()) {
                    handlePrecalculatesResults(precalculatedInResults, shuffleState.inState)
                    handlePrecalculatesResults(precalculatedOutResults, shuffleState.outState)
                }
            }

            useEffectOnce {
                canvasElement.setDimensions()
                addEventListener("resize" to resizeHandler)
                frameId = window.requestAnimationFrame { runStep() }
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