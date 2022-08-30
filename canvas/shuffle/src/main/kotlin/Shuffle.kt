import canvas.*
import css.Classes
import emotion.react.css
import kotlinx.browser.window
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.events.Event
import react.*
import react.dom.html.ReactHTML

private fun drawState(
    deck: Deck,
    canvasElement: HTMLCanvasElement,
    renderingContext: CanvasRenderingContext2D
) {
    val size = deck.size

    deck.elements.forEachIndexed { i, e ->
        val elementHeight = e.toDouble() / size.toDouble() // TODO util functions
        val relativeHeight = canvasElement.height - (canvasElement.height * elementHeight)
        val elementWidth = canvasElement.width.toDouble() / size
        val relativeWidth = i * elementWidth

        renderingContext.fillStyle = "hsl(${elementHeight * 360},100%,50%)"
        renderingContext.fillRect(relativeWidth, relativeHeight, elementWidth, canvasElement.height - relativeHeight)
    }
}

class Shuffle : ExternalCanvas() {
    override val name: String = "Shuffle"

    var intervalId: Int? = null

    override val component: FC<Props>
        get() = FC {
            val (deckSizeState, setDeckSizeState) = useState(52)
            val (shufflerState, setShufflerState) = useState(2)
            val (delayState, setDelayState) = useState(200)
            val (loopState, setLoopState) = useState(false)
            /*
            using setDeckState with immutable Decks doesn't work in the resizeHandler as it would always draw the initial deck
             */
            val (deckState, _) = useState(Deck(deckSizeState))

            fun clearLoop() {
                clearInterval()
                setLoopState(false)
            }

            fun draw() {
                renderingContext.clear()
                drawState(deckState, canvasElement, renderingContext)
            }

            val resizeHandler: (Event) -> Unit = {
                draw()
            }

            sliderInput {
                value = deckSizeState.toDouble().toString()
                min = 2.0
                max = 100.0
                step = 1.0
                onChange = {
                    clearLoop()
                    setDeckSizeState(it.target.value.toDouble().toInt())
                    if (shufflerState > deckSizeState) {
                        setShufflerState(deckSizeState)
                    }
                }
            }

            ReactHTML.canvas {
                css(Classes.canvas)
                id = canvasId
            }

            sliderInput {
                value = shufflerState.toDouble().toString()
                min = 2.0
                max = deckSizeState.toDouble()
                step = 1.0
                onChange = {
                    clearLoop()
                    setShufflerState(it.target.value.toDouble().toInt())
                }
            }

            buttonRow {
                buttons = listOf(
                    Button("Sort $deckSizeState cards", loopState) {
                        deckState.sort()
                        draw()
                    },
                    Button("Randomize", loopState) {
                        deckState.randomize()
                        draw()
                    },
                    Button("$shufflerState-Pile", loopState) {
                        deckState.pile(shufflerState)
                        draw()
                    }
                )
            }

            buttonRow {
                buttons = listOf(
                    Button("${delayState}ms Loop", false) {
                        if (loopState) {
                            clearLoop()
                        } else {
                            setLoopState(true)
                            deckState.sort()
                            deckState.pile(shufflerState)
                            draw()
                            intervalId = window.setInterval({
                                if (!deckState.sorted()) {
                                    deckState.pile(shufflerState)
                                    draw()
                                } else {
                                    clearLoop()
                                }
                            }, delayState)
                        }
                    }
                )
            }

            sliderInput {
                value = delayState.toDouble().toString()
                min = 0.0
                max = 500.0
                step = 10.0
                onChange = {
                    clearLoop()
                    setDelayState(it.target.value.toDouble().toInt())
                }
            }

            useEffect(deckSizeState) {
                if (deckState.size != deckSizeState) {
                    deckState.reset(deckSizeState)
                    draw()
                }
            }

            useEffectOnce {
                canvasElement.setDimensions()
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