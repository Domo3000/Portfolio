import canvas.*
import css.Classes
import csstype.NamedColor
import emotion.react.css
import kotlinx.browser.window
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.events.Event
import org.w3c.dom.events.KeyboardEvent
import react.*
import react.dom.html.ReactHTML
import kotlin.random.Random

enum class Direction {
    UP,
    RIGHT,
    DOWN,
    LEFT
}

class Part(val openings: MutableSet<Direction> = mutableSetOf()) {
    fun set(directions: Set<Direction>) {
        openings.clear()
        openings.addAll(directions)
    }

    private fun drawPath(
        x: Double,
        y: Double,
        size: Double,
        renderingContext: CanvasRenderingContext2D,
        offsetX: Int = 1,
        offsetY: Int = 1
    ) {
        val third = size / 3.0
        renderingContext.fillRect(
            x + offsetX * third,
            y + offsetY * third,
            third,
            third
        )
    }

    fun draw(
        x: Double,
        y: Double,
        size: Double,
        renderingContext: CanvasRenderingContext2D
    ) {
        renderingContext.fillStyle = NamedColor.sienna
        renderingContext.fillRect(
            x,
            y,
            size,
            size
        )
        if (openings.isNotEmpty()) {
            renderingContext.fillStyle = NamedColor.sandybrown
            drawPath(x, y, size, renderingContext)

            openings.forEach { direction ->
                val (offsetX, offsetY) = when (direction) {
                    Direction.UP -> 1 to 0
                    Direction.RIGHT -> 2 to 1
                    Direction.DOWN -> 1 to 2
                    Direction.LEFT -> 0 to 1
                }
                drawPath(x, y, size, renderingContext, offsetX, offsetY)
            }
        }
    }
}

/*
class LabyrinthPart(
    val up: Boolean = false,
    val right: Boolean = false,
    val down: Boolean = false,
    val left: Boolean = false
) {
    private fun drawPath(
        x: Double,
        y: Double,
        size: Double,
        renderingContext: CanvasRenderingContext2D,
        offsetX: Int = 1,
        offsetY: Int = 1
    ) {
        val third = size / 3.0
        renderingContext.fillRect(
            x + offsetX * third,
            y + offsetY * third,
            third,
            third
        )
    }

    fun draw(
        x: Double,
        y: Double,
        size: Double,
        renderingContext: CanvasRenderingContext2D
    ) {
        renderingContext.fillStyle = NamedColor.sienna
        renderingContext.fillRect(
            x,
            y,
            size,
            size
        )
        if (up || right || down || left) {
            renderingContext.fillStyle = NamedColor.sandybrown
            drawPath(x, y, size, renderingContext)
            if (up) {
                drawPath(x, y, size, renderingContext, offsetY = 0)
            }
            if (right) {
                drawPath(x, y, size, renderingContext, offsetX = 2)
            }
            if (down) {
                drawPath(x, y, size, renderingContext, offsetY = 2)
            }
            if (left) {
                drawPath(x, y, size, renderingContext, offsetX = 0)
            }
        }
    }
}

 */


// TODO move to automaton
infix fun Int.mod(m: Int) = (this % m + m) % m

class World(var sizeX: Int, var sizeY: Int) {
    val elements: ArrayList<ArrayList<Part>> = ArrayList(Array(sizeY) {
        ArrayList(Array(sizeX) {
            Part()
        }.toList())
    }.toList())

    fun get(x: Int, y: Int): Part = elements[y mod sizeY][x mod sizeX]

    fun set(x: Int, y: Int, directions: Set<Direction>) {
        //elements[y][x] = Part(directions.toMutableSet())
        elements[y][x].set(directions.toMutableSet())
    }

    fun setAll(method: (Int, Int) -> Set<Direction>) {
        (0 until sizeY).map { y ->
            (0 until sizeX).map { x ->
                elements[y][x].set(method(x, y))
            }
        }
    }

    fun setAllRandomly() {
        val random = Random(0)
        setAll { _, _ ->
            val openings = mutableSetOf<Direction>()
            if (random.nextBoolean()) {
                openings += Direction.UP
            }
            if (random.nextBoolean()) {
                openings += Direction.RIGHT
            }
            if (random.nextBoolean()) {
                openings += Direction.DOWN
            }
            if (random.nextBoolean()) {
                openings += Direction.LEFT
            }
            openings
        }
    }

    fun move(index: Int, direction: Direction) {
        if (direction == Direction.UP) {
            val top = elements[0][index].openings.toSet()
            (0 until sizeY - 1).forEach { y ->
                val next = elements[y + 1][index].openings.toSet()
                elements[y][index].set(next)
            }
            elements[sizeY - 1][index].set(top)
        }
        if (direction == Direction.RIGHT) {
            val right = get(sizeX - 1, index).openings.toSet()
            (sizeX - 1 downTo  1).forEach { x ->
                val next = get(x - 1, index).openings.toSet()
                set(x, index, next)
            }
            set(0, index, right)
        }
        if (direction == Direction.DOWN) {
            val bottom = elements[sizeY - 1][index].openings.toSet()
            (1 until sizeY).forEach { y ->
                val next = elements[y - 1][index].openings.toSet()
                elements[y][index].set(next)
            }
            elements[0][index].set(bottom)
        }
        if (direction == Direction.LEFT) {
            val left = elements[index][0].openings.toSet()
            (1 until sizeX).forEach { x ->
                val next = elements[index][x - 1].openings.toSet()
                elements[index][x].set(next)
            }
            elements[index][sizeX - 1].set(left)
        }
    }
}

class Labyrinth : ExternalCanvas() {
    override val name: String = "Labyrinth"

    private var frameId: Int? = null

    override val component: FC<Props>
        get() = FC {
            val (size, setSize) = useState(8)
            val (worldState, _) = useState(World(size, size))

            fun drawArrow(direction: Int) {
                when (direction) {
                    0 -> Unit
                    1 -> Unit
                    2 -> Unit
                    3 -> Unit
                }
            }

            fun drawBorders() {

            }

            fun draw() {
                renderingContext.clear()
                drawBorders()
                worldState.elements.forEachIndexed { y, list ->
                    list.forEachIndexed { x, part ->
                        val sizeWithBorder = size + 4
                        val relX = canvasElement.getRelativeX(x + 2, sizeWithBorder)
                        val relY = canvasElement.getRelativeY(y + 2, sizeWithBorder)
                        val width = canvasElement.getElementWidth(sizeWithBorder)

                        part.draw(relX, relY, width, renderingContext)
                    }
                }
            }

            fun run() {
                draw()
                frameId = window.requestAnimationFrame { run() }
            }

            val keypressHandler: (Event) -> Unit = { event ->
                when ((event as KeyboardEvent).key.lowercase()) {
                    "b" -> worldState.move(3, Direction.UP)
                }
                draw()
            }

            val resizeHandler: (Event) -> Unit = {
                draw()
            }

            ReactHTML.canvas {
                css(Classes.canvas)
                id = canvasId
                onClick = {
                    val bounds = canvasElement.getBoundingClientRect()
                    val x = it.clientX - bounds.left
                    val y = it.clientY - bounds.top

                    val absX = canvasElement.getRelativeX(2, size + 4)
                    val absY = canvasElement.getRelativeY(2, size + 4)
                    val width = canvasElement.getElementWidth(size + 4)

                    if (x >= absX && x <= absX + 8 * width && y < absY) {
                        val relX = x - absX
                        worldState.move((relX / width).toInt(), Direction.UP)
                    }
                    if (x >= absX && x <= absX + 8 * width && y > absY + 8 * width) {
                        val relX = x - absX
                        worldState.move((relX / width).toInt(), Direction.DOWN)
                    }
                    if (y >= absY && y <= absY + 8 * width && x < absX) {
                        val relY = y - absY
                        worldState.move((relY / width).toInt(), Direction.LEFT)
                    }
                    if (y >= absY && y <= absY + 8 * width && x > absX + 8 * width) {
                        val relY = y - absY
                        worldState.move((relY / width).toInt(), Direction.RIGHT)
                    }
                    draw()
                }
            }

            useEffectOnce {
                canvasElement.setDimensions(800, 800)
                worldState.setAllRandomly()
                addEventListener("resize" to resizeHandler)
                addEventListener("keypress" to keypressHandler)
                draw()
            }
        }

    private fun cancelAnimationFrame() {
        frameId?.let { window.cancelAnimationFrame(it) }
        frameId = null
    }

    override fun cleanUp() {
        cancelAnimationFrame()
    }

    override fun initialize() {}

    init {
        initEventListeners()
    }
}