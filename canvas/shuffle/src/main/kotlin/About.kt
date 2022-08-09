import canvas.*
import csstype.FontWeight
import emotion.react.css
import kotlinx.browser.window
import org.w3c.dom.events.Event
import react.*
import react.dom.html.ReactHTML
import kotlin.math.log

private data class ShuffleCounter(val deck: Deck, var counter: Int = 0, var finished: Boolean = false)

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

    val finished
        get() = decks.flatten().filter { it.second.finished }.count()

    fun addRow() {
        size += 1
        val newRow = Array(size) { x -> Position(x, size - 1) to ShuffleCounter(Deck(size + 1)) }.toList()
        decks.add(newRow)
        setSize(size)
    }
}

class About : ExternalCanvas() {
    override val name: String = "ShuffleAbout"

    var intervalId: Int? = null

    override val component: FC<Props>
        get() = FC {
            val (size, setSize) = useState(9)
            val (haveFinished, setHaveFinished) = useState(0)
            val (showDetails, setShowDetails) = useState(Details())
            val (newState, _) = useState(State(size, setSize))

            fun getColor(n: Int): String {
                if (n == 0) {
                    return "DimGray"
                }

                val range = 240
                val power = when (newState.size) {
                    in 0..100 -> 10.0 + newState.size / 10.0
                    else -> 20.0
                }
                /*
                    180 / 15 => 2^15 is Red
                    240 / 20 => 2^20 is Red
                 */
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
                val x = canvasElement.getRelativeX(position.x, newState.size)
                val y = canvasElement.getRelativeY(position.y, newState.size)
                val elementWidth = canvasElement.getElementWidth(newState.size)
                val elementHeight = canvasElement.getElementHeight(newState.size)

                renderingContext.fillStyle = getColor(element?.counter ?: 0)
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
                for (i in 0 until newState.size) {
                    for (j in 0 until newState.size) {
                        drawElement(Position(i, j), newState.decks.getOrNull(j)?.getOrNull(i)?.second)
                    }
                }
            }

            fun draw() {
                renderingContext.drawBackground()
                drawState()
            }

            fun singleStep(position: Position, element: ShuffleCounter, repetitions: Int) {
                var c = repetitions

                do {
                    element.deck.pile(position.x + 2)
                    element.counter += 1
                    element.finished = element.deck.sorted()
                } while (!element.finished && c-- > 0)
            }

            fun newRunRandomSteps(amount: Int, repetitions: Int): Set<Pair<Position, ShuffleCounter>> {
                val toDraw = mutableSetOf<Pair<Position, ShuffleCounter>>()

                // run a lot for start and end
                newState.unfinished.firstOrNull()?.let { (position, element) ->
                    toDraw.add(position to element)
                    singleStep(position, element, amount * repetitions)
                }

                newState.unfinished.toList().lastOrNull()?.let { (position, element) ->
                    toDraw.add(position to element)
                    singleStep(position, element, amount * repetitions)
                }

                // run often for a few
                (0..repetitions).forEach { _ ->
                    newState.unfinished.toList().randomOrNull()?.let { (position, element) ->
                        toDraw.add(position to element)
                        singleStep(position, element, amount)
                    }
                }

                // run a bit for many
                (0..amount).forEach { _ ->
                    newState.unfinished.toList().randomOrNull()?.let { (position, element) ->
                        toDraw.add(position to element)
                        singleStep(position, element, repetitions)
                    }
                }

                return toDraw
            }

            fun runStep() {
                newRunRandomSteps(50, 10).forEach { (position, element) ->
                    drawElement(position, element)
                }

                setHaveFinished(newState.finished)

                if (newState.unfinished.size < 200) {
                    newState.addRow()
                    draw()
                }
            }

            val resizeHandler: (Event) -> Unit = {
                canvasElement.resetDimensions()
                draw()
            }

            ReactHTML.div {
                className = Classnames.text
                ReactHTML.p {
                    css {
                        fontWeight = FontWeight.bold
                    }
                    +"Pile-Shuffling Loops"
                }
                ReactHTML.p {
                    +"Which configurations cause the longest loop?"
                }
                ReactHTML.details {
                    ReactHTML.summary {
                        +"k-Pile Shuffling Explanation"
                    }
                    ReactHTML.ul {
                        ReactHTML.li {
                            +"Take n cards and reorganize them into k piles"
                        }
                        ReactHTML.li {
                            +"First card on first pile, second card on second pile, ..."
                        }
                        ReactHTML.li {
                            +"k'th card on k'th pile, k+1'th card on 1st pile, ..."
                        }
                        ReactHTML.li {
                            +"Once all cards have been put into piles put those on top of each other."
                        }
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
                    ReactHTML.ul {
                        ReactHTML.li {
                            +"n cards loop back after x times repeatedly using k pile shuffles => n:k = x"
                        }
                        ReactHTML.li {
                            +"n cards using different k values => n:(2->3->4->5)"
                        }
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
            }

            ReactHTML.canvas {
                className = Classnames.responsiveCanvas
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
                className = Classnames.text

                showDetails.position?.let { (x, y) ->
                    val element = newState.decks[y][x].second
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
                        +"$haveFinished/${(((size) / 2.0) * (size + 1)).toInt()} have finished!"
                    }
                }
            }

            useEffectOnce {
                addEventListener("resize" to resizeHandler)
                canvasElement.resetDimensions()
                draw()
                intervalId = window.setInterval({ runStep() }, 0)
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