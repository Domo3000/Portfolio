import canvas.*
import css.Classes
import csstype.Color
import csstype.FontWeight
import csstype.TextAlign
import emotion.react.css
import kotlinx.browser.window
import org.w3c.dom.events.Event
import react.*
import react.dom.html.ReactHTML
import kotlin.math.log
import kotlin.math.pow

private data class ShuffleCounter(val deck: Deck, var counter: Long = 0, var finished: Boolean = false)

private data class Details(val position: Position? = null)

private data class Position(val x: Int = -1, val y: Int = -1) {
    override fun equals(other: Any?): Boolean {
        (other as? Position)?.let {
            if (x != it.x) return false
            if (y != it.y) return false
        } ?: return false

        return true
    }
}

private class State(initialSize: Int, private val setSize: StateSetter<Int>) {
    var size = initialSize

    val decks = Array(size) { y ->
        Array(y + 1) { x ->
            Position(x, y) to ShuffleCounter(Deck(y + 2))
        }.toList()
    }.toMutableList()

    val unfinished
        get() = decks.flatten().filter { !it.second.finished }

    val unfinishedCount
        get() = unfinished.count()

    val finished
        get() = decks.flatten().filter { it.second.finished }

    val finishedCount
        get() = finished.count()

    fun addRow() {
        size += 1
        val newRow = Array(size) { x -> Position(x, size - 1) to ShuffleCounter(Deck(size + 1)) }.toList()
        decks.add(newRow)
        setSize(size)
    }
}

class About : ExternalCanvas() {
    override val name: String = "ShuffleAbout"

    var frameId: Int? = null

    override val component: FC<Props>
        get() = FC {
            val (size, setSize) = useState(9)
            val (haveFinished, setHaveFinished) = useState(0)
            val (showDetails, setShowDetails) = useState(Details())
            val (state, _) = useState(State(size, setSize))

            fun getPower(size: Int) = when (size) {
                in 0..100 -> 10.0 + size / 10.0
                else -> 20.0
            }

            fun getColor(n: Long): String {
                if (n == 0L) {
                    return "DimGray"
                }

                val range = 240
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

            fun drawState() {
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
                drawState()
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
                ReactHTML.p {
                    css {
                        fontWeight = FontWeight.bold
                    }
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
                    val element = state.decks[y][x].second
                    ReactHTML.p {
                        +"${showDetails.position.y + 2}:${showDetails.position.x + 2} = ${element.counter}"
                        if (element.finished) {
                            +" has finished!"
                        } else {
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
                ReactHTML.details {
                    ReactHTML.summary {
                        +"k-Pile Shuffling Explanation"
                    }
                    list {
                        texts = listOf(
                            "Take n cards and reorganize them into k piles",
                            "First card on first pile, second card on second pile, ...",
                            "k'th card on k'th pile, k+1'th card on 1st pile, ...",
                            "Once all cards have been put into piles put those on top of each other."
                        )
                    }
                    ReactHTML.p {
                        +"Deck:[1, 2, 3, 4] with 2-pile shuffle => Pile1:[1, 3], Pile2:[2, 4] => Deck:[1, 3, 2, 4]"
                    }
                    ReactHTML.p {
                        +"Doing a 2-pile shuffle again would loop back to the starting order."
                    }
                    ReactHTML.p {
                        +"Deck:[1, 3, 2, 4] with 3-pile shuffle => Pile1:[1, 4], Pile2:[3], Pile3:[2] => Deck:[1, 4, 3, 2]"
                    }
                }
                ReactHTML.details {
                    ReactHTML.summary {
                        +"Notation"
                    }
                    list {
                        texts = listOf(
                            "\"n:k = x\" means n cards loop back after x times repeatedly using k pile shuffles",
                            "\"n:(2->3->4->5)\" = n cards using different k values"
                        )
                    }
                    ReactHTML.p {
                        +"Previous example would be 4:(2->3) and 4:2 = 2"
                    }
                    ReactHTML.p {
                        +"Cases that I find interesting are those where adding more pile shuffles leads to more order."
                    }
                    ReactHTML.p {
                        +"For example 99:(3-5), 99:(3-7) and 99:(5-7) all looks more 'random' than 99:(3-5-7)"
                    }
                }
                ReactHTML.details {
                    ReactHTML.summary {
                        +"Trivial Loops"
                    }
                    ReactHTML.p {
                        +"k = sqrt(n) always takes 2 repetitions to loop back, e.g. 36:6 or 100:10"
                    }
                    ReactHTML.p {
                        +"Simple math rules apply to those loops, e.g. 100:(10->10) == 100:(2->5->10) == 100:(2->5->2->5) == 100:(4->5->5) == 100:(5->20) == 100:(4->25)"
                    }
                    ReactHTML.p {
                        +"Order doesn't matter, e.g. 100:(4->5->5) == 100:(5->4->5) == 100:(5->5->4)"
                    }
                }
                ReactHTML.details {
                    ReactHTML.summary {
                        +"Non-Trivial Loops"
                    }
                    ReactHTML.p {
                        +"Sometimes loops finish after a couple of repetitions, sometimes after hundreds, and sometimes after millions."
                    }
                    ReactHTML.p {
                        +"My intuition would have assumed that for a given n the k with the largest loop would be related to prime numbers."
                    }
                    ReactHTML.p {
                        +"But then there's cases like 80:44 = 86940, 99:22 = 925680, 100:48 = 429660, 123:15 = 19920600 and 140:122 = 29350552"
                    }
                    ReactHTML.p {
                        +"Is it chaotic? Can we know which k would give the largest result for a given n? Can we know how long it would run?"
                    }
                }
                ReactHTML.details {
                    ReactHTML.summary {
                        +"Legend"
                    }

                    val power = getPower(size).toInt()

                    (0..power).forEach {
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
            }

            useEffectOnce {
                canvasElement.setDimensions()
                addEventListener("resize" to resizeHandler)
                draw()
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