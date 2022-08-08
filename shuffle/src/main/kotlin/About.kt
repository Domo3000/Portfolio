import canvas.drawBackground
import canvas.resetDimensions
import csstype.FontWeight
import emotion.react.css
import kotlinx.browser.window
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.events.Event
import react.*
import react.dom.html.ReactHTML
import kotlin.math.log

private const val size = 79
private const val lineWidth = 1.0

private fun getColor(n: Int): String {
    if (n == 0) {
        return "DimGray"
    }

    val scale = (log(n.toDouble(), 2.0) * 12).toInt()

    if (scale >= 180) {
        return "Red"
    }

    return "hsl(${180 - scale},100%,50%)"
}

private typealias Counter = Triple<Deck, Int, Boolean>

private typealias Details = Triple<Int, Int, Boolean>

// TODO move to helper classes
private fun getElementWidth(width: Int) = (width - 2 * lineWidth) / size

private fun getRelativeX(j: Int, width: Int) = lineWidth + j * getElementWidth(width)

private fun getElementHeight(height: Int) = (height - 2 * lineWidth) / size

private fun getRelativeY(i: Int, height: Int) = lineWidth + i * getElementHeight(height)

private fun drawState(
    state: Array<Array<Counter>>,
    canvasElement: HTMLCanvasElement,
    renderingContext: CanvasRenderingContext2D
) {
    for (i in 0 until size) {
        for (j in 0 until size) {
            val element = state[i][j]
            val x = getRelativeX(j, canvasElement.width)
            val y = getRelativeY(i, canvasElement.height)
            val elementWidth = getElementWidth(canvasElement.width)
            val elementHeight = getElementHeight(canvasElement.height)

            renderingContext.fillStyle = getColor(element.second)
            renderingContext.fillRect(
                x,
                y,
                elementWidth,
                elementHeight
            )
            if (element.third) {
                renderingContext.fillStyle = "Black"
                renderingContext.fillRect(
                    x + elementWidth / 5.0 * 2.0,
                    y + elementHeight / 5.0 * 2.0,
                    elementWidth / 5.0,
                    elementHeight / 5.0
                )
            }
        }
    }
}

class About : ExternalCanvas() {
    override val name: String = "ShuffleAbout"

    var intervalId: Int? = null

    override val component: FC<Props>
        get() = FC {
            var finished = 0
            val (haveFinished, setHaveFinished) = useState(finished)
            val (showDetails, setShowDetails) = useState(Details(-1, -1, false))
            val (state, _) = useState(Array(size) { y -> Array(size) { Counter(Deck(y + 2), 0, false) } })
            val unfinished =
                Array(size) { y -> Array(size) { x -> y to x }.toList() }.toList().flatten().toMutableList()
            val total = (((size + 1) / 2) * (size + 1))

            fun draw() {
                renderingContext.drawBackground()
                drawState(state, canvasElement, renderingContext)
            }

            fun step(x: Int, y: Int, repetitions: Int) {
                if (x <= y && !state[y][x].third) {
                    val deck = state[y][x].first
                    var counter = state[y][x].second
                    var c = repetitions

                    do {
                        deck.pile(x + 2)
                        counter += 1
                    } while (!deck.sorted() && c-- > 0)

                    val sorted = deck.sorted()

                    if (sorted) {
                        finished += 1
                        unfinished.remove(y to x)
                    }

                    state[y][x] = Counter(deck, counter, sorted)
                } else {
                    unfinished.remove(y to x)
                }
            }

            fun runRandomSteps(amount: Int, repetitions: Int) {
                var count = 0
                while (count++ < amount) {
                    unfinished.random().let { (y, x) ->
                        step(x, y, repetitions)
                    }
                }
            }

            fun runStep() {
                runRandomSteps(500, 10)
                setHaveFinished(finished)
                draw()

                if(unfinished.isEmpty()) {
                    clearInterval()
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
                    val elementHeight = getElementHeight(canvasElement.height)
                    val elementWidth = getElementWidth(canvasElement.width)

                    val relativeX = ((x - lineWidth) / elementWidth).toInt()
                    val relativeY = ((y - lineWidth) / elementHeight).toInt()

                    if (relativeX in 0..relativeY &&
                        relativeY >= 0 &&
                        relativeX < size
                        && relativeY < size &&
                        (relativeX != showDetails.second ||
                                relativeY != showDetails.first)
                    ) {
                        setShowDetails(Details(relativeY, relativeX, true))
                    } else {
                        setShowDetails(Details(-1, -1, false))
                    }
                }
            }

            ReactHTML.div {
                className = Classnames.text
                if (showDetails.third) {
                    ReactHTML.p {
                        val element = state[showDetails.first][showDetails.second]
                        +"${showDetails.first + 2}:${showDetails.second + 2} = ${element.second}"
                        if (element.third) {
                            +" has finished!"
                        } else {
                            +"+"
                        }
                    }
                } else {
                    ReactHTML.p {
                        +"$haveFinished/$total have finished!"
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
