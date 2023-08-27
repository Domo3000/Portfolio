import canvas.ExternalCanvas
import canvas.clear
import canvas.setDimensions
import css.ClassNames
import css.Classes
import web.cssom.NamedColor
import web.cssom.pct
import web.cssom.px
import emotion.react.css
import web.events.Event
import org.w3c.dom.events.KeyboardEvent
import props.*
import react.*
import react.dom.html.ReactHTML
import web.cssom.Float

class StringHolder(var s: String)
class IntHolder(var n: Int)

// TODO move to own package
class Debug : ExternalCanvas() {
    override val name: String = "Debug"

    override val component: FC<Props>
        get() = FC {
            val (immutableString, setImmutableString) = useState("")
            var (mutableStringState, _) = useState("")
            val (stringHolderState, _) = useState(StringHolder(""))
            var mutableString = ""
            val (immutableList, setImmutableList) = useState(emptyList<Char>())
            val (mutableListState, _) = useState(mutableListOf<Char>())
            val mutableList = mutableListOf<Char>()
            val (pressesState, setPressesState) = useState(0)
            val (pressesHolderState, _) = useState(IntHolder(0))
            var mutablePresses = 0

            fun elements() = listOf(
                "immutableString" to immutableString,
                "mutableStringState" to mutableStringState,
                "stringHolderState" to stringHolderState.s,
                "mutableString" to mutableString,
                "immutableList" to immutableList.joinToString(""),
                "mutableListState" to mutableListState.joinToString(""),
                "mutableList" to mutableList.joinToString(""),
                "pressesState" to pressesState,
                "pressesHolderState" to pressesHolderState.n,
                "mutablePresses" to mutablePresses
            )

            fun draw() {
                renderingContext.clear()

                renderingContext.fillStyle = NamedColor.black
                val fontSize = canvasElement.height / 20
                renderingContext.font = "${fontSize}px Courier New"

                if (mutableListState.isEmpty()) {
                    renderingContext.fillText(
                        "Press any key or click", canvasElement.width / 4.0, canvasElement.height / 2.0 - 5.0 * fontSize
                    )
                } else {
                    elements().forEachIndexed { index, (text, value) ->
                        renderingContext.fillText(
                            "$text: $value", 5.0, (index + 1.0) * 1.9 * fontSize
                        )
                    }
                }
            }

            fun incrementPresses() {
                setPressesState(pressesState + 1)
                pressesHolderState.n += 1
                mutablePresses += 1
            }

            fun addChar(char: Char) {
                setImmutableString("$immutableString$char")
                mutableStringState += char
                stringHolderState.s += char
                mutableString += char
                setImmutableList(immutableList + char)
                mutableListState.add(char)
                mutableList.add(char)
            }

            val resizeHandler: (Event) -> Unit = {
                draw()
            }

            val keypressHandler: (Event) -> Unit = { event ->
                val char = (event as KeyboardEvent).key.lowercase()[0]

                addChar(char)
                incrementPresses()

                draw()
            }

            val keydownHandler: (Event) -> Unit = { event ->
                if( (event as KeyboardEvent).key == "Escape" ) {
                    setImmutableString("")
                    mutableStringState = ""
                    stringHolderState.s = ""
                    mutableString = ""
                    setImmutableList(emptyList())
                    mutableListState.clear()
                    mutableList.clear()
                    setPressesState(0)
                    pressesHolderState.n = 0
                    mutablePresses = 0
                    draw()
                }
            }

            ReactHTML.div {
                formattedCode {
                    lines = listOf(
                        CodeLine(
                            listOf(
                                "class".orange(),
                                " StringHolder(".white(),
                                "var".orange(),
                                " s".purple(),
                                ": String)".white()
                            )
                        ),
                        CodeLine(
                            listOf(
                                "class".orange(),
                                " IntHolder(".white(),
                                "var".orange(),
                                " n".purple(),
                                ": Int)".white()
                            )
                        ),
                        CodeLine(),
                        CodeLine(
                            listOf(
                                "val".orange(),
                                " (immutableString, setImmutableString) = useState(".white(),
                                "\"\"".green(),
                                ")".white()
                            )
                        ),
                        CodeLine(
                            listOf(
                                "var".orange(),
                                " (mutableStringState, _) = useState(".white(),
                                "\"\"".green(),
                                ")".white()
                            )
                        ),
                        CodeLine(
                            listOf(
                                "val".orange(),
                                " (stringHolderState, _) = useState(StringHolder(".white(),
                                "\"\"".green(),
                                "))".white()
                            )
                        ),
                        CodeLine(
                            listOf(
                                "var".orange(),
                                " mutableString = ".white(),
                                "\"\"".green()
                            )
                        ),
                        CodeLine(
                            listOf(
                                "val".orange(),
                                " (immutableList, setImmutableList) = useState(emptyList<Char>())".white()
                            )
                        ),
                        CodeLine(
                            listOf(
                                "val".orange(),
                                " (mutableListState, _) = useState(mutableListOf<Char>())".white()
                            )
                        ),
                        CodeLine(
                            listOf(
                                "val".orange(),
                                " mutableList = mutableListOf<Char>()".white()
                            )
                        ),
                        CodeLine(
                            listOf(
                                "val".orange(),
                                " (pressesState, setPressesState) = useState(".white(),
                                "0".blue(),
                                ")".white()
                            )
                        ),
                        CodeLine(
                            listOf(
                                "val".orange(),
                                " (pressesHolderState, _) = useState(IntHolder(".white(),
                                "0".blue(),
                                "))".white()
                            )
                        ),
                        CodeLine(
                            listOf(
                                "var".orange(),
                                " mutablePresses = ".white(),
                                "0".blue()
                            )
                        )
                    )
                }
            }

            ReactHTML.div {
                css(ClassNames.phoneFullWidth) {
                    width = 50.pct
                    float = Float.left
                    marginRight = 5.px
                }
                ReactHTML.canvas {
                    css(Classes.canvas)
                    id = canvasId
                    onClick = {
                        addChar('c')
                        incrementPresses()
                        draw()
                    }
                }
            }

            ReactHTML.div {
                if (pressesState == 0) {
                    ReactHTML.h4 {
                        +"Press any key or click on Canvas."
                    }
                } else {
                    elements().forEach { (text, value) ->
                        ReactHTML.p {
                            css {
                                margin = 0.px
                            }
                            ReactHTML.strong {
                                +text
                            }
                            +": $value"
                        }
                    }
                }
            }

            useEffectOnce {
                canvasElement.setDimensions()
                addEventListener("resize" to resizeHandler)
                addEventListener("keypress" to keypressHandler)
                addEventListener("keydown" to keydownHandler)
                draw()
            }
        }

    override fun initialize() {}

    override fun cleanUp() {}

    init {
        initEventListeners()
    }
}