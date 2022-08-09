import canvas.drawBackground
import canvas.resetDimensions
import csstype.NamedColor
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.events.Event
import org.w3c.dom.events.KeyboardEvent
import react.*
import react.dom.html.ReactHTML.canvas
import react.dom.html.ReactHTML.div
import kotlin.random.Random

private fun drawRules(canvasElement: HTMLCanvasElement, renderingContext: CanvasRenderingContext2D) {
    renderingContext.fillStyle = NamedColor.black
    val fontSize = canvasElement.height / 20

    renderingContext.font = "${fontSize}px Courier New"
    renderingContext.fillText("Click anywhere!", canvasElement.width / 3.0, canvasElement.height / 2.0 - 2.5 * fontSize)
    renderingContext.fillText( // TODO different text for mobile layout (check if button is hidden?)
        "Press B to rebalance",
        canvasElement.width / 3.0,
        canvasElement.height / 2.0 - fontSize / 2
    )
    renderingContext.fillText(
        "Press C to clear",
        canvasElement.width / 3.0,
        canvasElement.height / 2.0 + fontSize * 1.5
    )
}

// TODO cleanup more like Shuffle
private fun draw(tree: Node?, canvasElement: HTMLCanvasElement, renderingContext: CanvasRenderingContext2D) {
    canvasElement.resetDimensions()
    renderingContext.drawBackground()
    tree?.draw(canvasElement, renderingContext) ?: drawRules(canvasElement, renderingContext)
}

class KdTree : ExternalCanvas() {
    override val name: String = "KdTree"

    override val component: FC<Props>
        get() = FC {
            var orientation: Orientation = Horizontal
            var tree: Node? = null

            val balance: () -> Unit = {
                orientation = orientation.switch()
                if (tree != null) {
                    tree = rebalance(tree!!, orientation)
                    draw(tree, canvasElement, renderingContext)
                }
            }

            val clear: () -> Unit = {
                tree = null
                draw(tree, canvasElement, renderingContext)
            }

            val resizeHandler: (Event) -> Unit = {
                draw(tree, canvasElement, renderingContext)
            }

            val keypressHandler: (Event) -> Unit = { event ->
                when ((event as KeyboardEvent).key.lowercase()) {
                    "b" -> balance()
                    "c" -> clear()
                }
            }

            canvas {
                className = Classnames.responsiveCanvas
                id = canvasId
                onClick = {
                    if (tree == null || tree!!.size() < 1000) {
                        val bounds = canvasElement.getBoundingClientRect()
                        val x = ((it.clientX - bounds.left) / canvasElement.width.toDouble() * 100.0).toInt()
                        val y = ((it.clientY - bounds.top) / canvasElement.height.toDouble() * 100.0).toInt()
                        if (x > 0 && y > 0 && x < 100 && y < 100) {
                            if (tree == null) {
                                tree = Node(RelativePosition(x, y), orientation)
                            } else {
                                tree = tree!!.insert(RelativePosition(x, y))
                            }
                            draw(tree, canvasElement, renderingContext)
                        }
                    }
                }
            }

            div {
                className = Classnames.phoneElement
                buttonRow {
                    buttons = listOf(
                        Button("Balance", false) {
                            balance()
                        },
                        Button("Clear", false) {
                            clear()
                        }
                    )
                }
            }

            useEffectOnce {
                addEventListener("resize" to resizeHandler)
                addEventListener("keypress" to keypressHandler)
                draw(tree, canvasElement, renderingContext)
            }
        }

    override fun cleanUp() {}

    override fun initialize() {}

    init {
        initEventListeners()
    }
}

private fun pseudoRandomColor(random: Random) = when (random.nextInt() % 11) {
    0 -> NamedColor.tomato
    2 -> NamedColor.dodgerblue
    1 -> NamedColor.mediumseagreen
    3 -> NamedColor.orange
    4 -> NamedColor.mediumpurple
    5 -> NamedColor.darkturquoise
    else -> NamedColor.white
}

private fun Node.draw(
    canvasElement: HTMLCanvasElement,
    renderingContext2D: CanvasRenderingContext2D,
    minX: Double = 0.0,
    maxX: Double? = null,
    minY: Double = 0.0,
    maxY: Double? = null
) {
    val width = canvasElement.width
    val height = canvasElement.height

    val split = when (orientation) {
        Horizontal -> position.y
        Vertical -> position.x
    }.toDouble()

    val absoluteMinX = width * minX / 100.0
    val absoluteMaxX = maxX?.let { width * it / 100.0 } ?: width.toDouble()
    val absoluteMinY = height * minY / 100.0
    val absoluteMaxY = maxY?.let { height * it / 100.0 } ?: height.toDouble()
    val absolutePosX = width * position.x / 100.0
    val absolutePosY = height * position.y / 100.0
    val offset = 1.0
    val lineWidth = 2.0

    val random = Random(hashCode())

    renderingContext2D.fillStyle = pseudoRandomColor(random)

    when (orientation) {
        Horizontal -> {
            renderingContext2D.fillRect(
                absoluteMinX + offset,
                absoluteMinY + offset,
                (absoluteMaxX - absoluteMinX) - lineWidth,
                (absolutePosY - absoluteMinY) - lineWidth
            )
            renderingContext2D.fillStyle = pseudoRandomColor(random)
            renderingContext2D.fillRect(
                absoluteMinX + offset,
                absolutePosY + offset,
                (absoluteMaxX - absoluteMinX) - lineWidth,
                (absoluteMaxY - absolutePosY) - lineWidth
            )
            renderingContext2D.fillStyle = NamedColor.black
            renderingContext2D.fillRect(absoluteMinX, absolutePosY - offset, absoluteMaxX - absoluteMinX, lineWidth)
            left?.draw(canvasElement, renderingContext2D, minX, maxX, minY, split)
            right?.draw(canvasElement, renderingContext2D, minX, maxX, split, maxY)
        }
        Vertical -> {
            renderingContext2D.fillRect(
                absoluteMinX + offset,
                absoluteMinY + offset,
                (absolutePosX - absoluteMinX) - lineWidth,
                (absoluteMaxY - absoluteMinY) - lineWidth
            )
            renderingContext2D.fillStyle = pseudoRandomColor(random)
            renderingContext2D.fillRect(
                absolutePosX + offset,
                absoluteMinY + offset,
                (absoluteMaxX - absolutePosX) - lineWidth,
                (absoluteMaxY - absoluteMinY) - lineWidth
            )
            renderingContext2D.fillStyle = NamedColor.black
            renderingContext2D.fillRect(absolutePosX - offset, absoluteMinY, lineWidth, absoluteMaxY - absoluteMinY)
            left?.draw(canvasElement, renderingContext2D, minX, split, minY, maxY)
            right?.draw(canvasElement, renderingContext2D, split, maxX, minY, maxY)
        }
    }
}