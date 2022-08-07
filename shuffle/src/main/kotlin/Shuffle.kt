import canvas.drawBackground
import canvas.resetDimensions
import csstype.*
import emotion.react.css
import kotlinx.browser.window
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.events.Event
import react.*
import react.dom.events.MouseEventHandler
import react.dom.html.InputType
import react.dom.html.ReactHTML

private fun drawState(
    deck: Deck,
    canvasElement: HTMLCanvasElement,
    renderingContext: CanvasRenderingContext2D
) {
    val size = deck.size
    val lineWidth = 1.0

    deck.elements.forEachIndexed { i, e ->
        val elementHeight = e.toDouble() / size.toDouble()
        val maxHeight = canvasElement.height - 2 * lineWidth
        val relativeHeight = lineWidth + maxHeight - (maxHeight * elementHeight)
        val maxWidth = canvasElement.width - 2 * lineWidth
        val elementWidth = maxWidth / size
        val relativeWidth = lineWidth + i * elementWidth

        renderingContext.fillStyle = "hsl(${elementHeight * 360},100%,50%)"
        renderingContext.fillRect(relativeWidth, relativeHeight, elementWidth, lineWidth + maxHeight - relativeHeight)
    }
}

external interface ButtonProps : Props {
    var text: String
    var disabled: Boolean
    var onClick: MouseEventHandler<HTMLButtonElement>
    var width: Double
}

typealias Button = Triple<String, Boolean, MouseEventHandler<HTMLButtonElement>>

external interface ButtonRowProps : Props {
    var buttons: List<Triple<String, Boolean, MouseEventHandler<HTMLButtonElement>>>
}

val buttonRow = FC<ButtonRowProps> { props -> // TODO move to common
    ReactHTML.div {
        props.buttons.forEach {
            button {
                text = it.first
                disabled = it.second
                onClick = it.third
                width = (99.99 / props.buttons.size)
            }
        }
    }
}

val button = FC<ButtonProps> { props ->
    ReactHTML.button {
        +props.text
        css {
            width = props.width.pct
            padding = 15.px
            float = Float.left
            if (props.disabled) {
                textDecoration = TextDecoration.lineThrough
            }
        }
        onClick = props.onClick
        disabled = props.disabled
    }
}

class Shuffle : ExternalCanvas() {
    override val name: String = "Shuffle"

    var intervalId: Int? = null

    override val component: FC<Props>
        get() = FC {
            val (deckSizeState, setDeckSizeState) = useState(52) // TODO better name (deckSizeSlider? too long)
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
                renderingContext.drawBackground()
                drawState(deckState, canvasElement, renderingContext)
            }

            val resizeHandler: (Event) -> Unit = {
                canvasElement.resetDimensions()
                draw()
            }

            ReactHTML.input {
                id = "slider-input"
                type = InputType.range
                min = 2.0
                max = 100.0
                step = 1.0
                value = deckSizeState.toDouble().toString()
                css {
                    appearance = None.none
                    width = 100.pct
                    outline = None.none
                    backgroundColor = NamedColor.darkgray
                }
                onChange = {
                    clearLoop()
                    setDeckSizeState(it.target.value.toDouble().toInt())
                    val max = run {
                        val maybe = (deckSizeState / 2)
                        if (maybe <= 1) {
                            2
                        } else {
                            maybe
                        }
                    }
                    if (shufflerState > max) {
                        setShufflerState(max)
                    }
                }
            }

            ReactHTML.canvas {
                className = Classnames.responsiveCanvas
                id = canvasId
            }

            ReactHTML.input {
                id = "shuffler-input"
                type = InputType.range
                min = 2.0
                max = (deckSizeState / 2).toDouble()
                step = 1.0
                value = shufflerState.toDouble().toString()
                css {
                    appearance = None.none
                    width = 100.pct
                    outline = None.none
                    backgroundColor = NamedColor.darkgray
                }
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

            ReactHTML.input {
                id = "delay-input"
                type = InputType.range
                min = 0.0
                max = 500.0
                step = 10.0
                value = delayState.toDouble().toString()
                css {
                    appearance = None.none
                    width = 100.pct
                    outline = None.none
                    backgroundColor = NamedColor.darkgray
                }
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
                addEventListener("resize" to resizeHandler)
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