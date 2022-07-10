package projects.kdtree

import Classnames
import canvas.drawBackground
import canvas.resetDimensions
import csstype.Float
import csstype.NamedColor
import csstype.pct
import csstype.px
import emotion.react.css
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.events.KeyboardEvent
import react.FC
import react.Props
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.canvas
import react.dom.html.ReactHTML.div
import react.useEffect
import structures.*
import kotlin.random.Random

private const val borderWidth = 1.0
private const val ratio = 3.0 / 4.0

external interface CanvasProps : Props {
    var tree: Node?
}

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

private fun draw(tree: Node?, canvasElement: HTMLCanvasElement, renderingContext: CanvasRenderingContext2D) {
    canvasElement.resetDimensions(ratio)
    renderingContext.drawBackground()
    tree?.draw(canvasElement, renderingContext) ?: drawRules(canvasElement, renderingContext)
}

val Canvas = FC<CanvasProps> { props ->
    val elementId = "layout-canvas"
    var orientation: Orientation = Horizontal

    val canvasElement by lazy { document.getElementById(elementId) as HTMLCanvasElement }

    val renderingContext: CanvasRenderingContext2D by lazy { canvasElement.getContext("2d") as CanvasRenderingContext2D }

    window.addEventListener("resize", {
        draw(props.tree, canvasElement, renderingContext)
    })

    window.addEventListener("keypress", { event ->
        when ((event as KeyboardEvent).key.lowercase()) {
            "b" -> {
                orientation = orientation.switch()
                props.tree = rebalance(props.tree!!, orientation)
                draw(props.tree, canvasElement, renderingContext)
            }
            "c" -> {
                props.tree = null
                draw(props.tree, canvasElement, renderingContext)
            }
        }
    })

    div {
        canvas {
            className = Classnames.responsiveCanvas
            id = elementId
            //width = 800.0
            //height = 600.0
            onClick = {
                if (props.tree == null || props.tree!!.size() < 1000) {
                    val bounds = canvasElement.getBoundingClientRect()
                    val x = ((it.clientX - bounds.left) / canvasElement.width.toDouble() * 100.0).toInt()
                    val y = ((it.clientY - bounds.top) / canvasElement.height.toDouble() * 100.0).toInt()
                    if (x > 1 && y > 1 && x < 99 && y < 99) {
                        if (props.tree == null) {
                            props.tree = Node(RelativePosition(x, y), Horizontal)
                        } else {
                            props.tree = props.tree!!.insert(RelativePosition(x, y))
                        }
                        draw(props.tree, canvasElement, renderingContext)
                    }
                }
            }
        }
    }

    div {
        className = Classnames.phoneElement
        button {
            +"Balance"
            css {
                width = 50.pct
                padding = 15.px
                float = Float.left
            }
            onClick = {
                orientation = orientation.switch()
                props.tree = rebalance(props.tree!!, orientation)
                draw(props.tree, canvasElement, renderingContext)
            }
        }
        button {
            +"Clear"
            css {
                width = 50.pct
                padding = 15.px
                float = Float.left
            }
            onClick = {
                props.tree = null
                draw(props.tree, canvasElement, renderingContext)
            }
        }
    }

    useEffect {
        draw(props.tree, canvasElement, renderingContext)
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

    val random by lazy { Random(hashCode()) }

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