import canvas.*
import css.ClassNames
import css.Classes
import web.cssom.*
import emotion.react.css
import props.Button
import props.button
import props.buttonRow
import react.*
import react.dom.html.ReactHTML
import web.canvas.CanvasRenderingContext2D
import web.events.Event
import web.html.HTMLCanvasElement
import web.uievents.KeyboardEvent
import kotlin.random.Random

typealias Position = Pair<Int, Int>

sealed class Direction {
    abstract val isHorizontal: Boolean
    abstract val opposite: Direction
}

object Directions {
    object UP : Direction() {
        override val isHorizontal = false
        override val opposite = Directions.DOWN
        override fun toString() = "UP"
    }

    object RIGHT : Direction() {
        override val isHorizontal = true
        override val opposite = Directions.LEFT
        override fun toString() = "RIGHT"
    }

    object DOWN : Direction() {
        override val isHorizontal = false
        override val opposite = Directions.UP
        override fun toString() = "DOWN"
    }

    object LEFT : Direction() {
        override val isHorizontal = true
        override val opposite = Directions.RIGHT
        override fun toString() = "LEFT"
    }
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
                    Directions.UP -> 1 to 0
                    Directions.RIGHT -> 2 to 1
                    Directions.DOWN -> 1 to 2
                    Directions.LEFT -> 0 to 1
                }
                drawPath(x, y, size, renderingContext, offsetX, offsetY)
            }
        }
    }
}

infix fun Int.mod(m: Int) = (this % m + m) % m

abstract class Entity(var position: Position, var sizeX: Int, var sizeY: Int) {
    var alive: Boolean = true
    abstract val color: Color
    fun draw(
        size: Int,
        canvasElement: HTMLCanvasElement,
        renderingContext: CanvasRenderingContext2D
    ) {
        if (alive) {
            val relX = canvasElement.getRelativeX(position.first, sizeX)
            val relY = canvasElement.getRelativeY(position.second, sizeY)
            val width = canvasElement.getElementWidth(sizeX)
            val half = width / 2.0
            renderingContext.drawCircle(relX + half, relY + half, width / 6.0, color)
        }
    }

    abstract fun move(direction: Direction, currentOpenings: Set<Direction>, nextOpenings: Set<Direction>)
}

abstract class SlidingEntity(position: Position, sizeX: Int, sizeY: Int) : Entity(position, sizeX, sizeY) {
    override fun move(direction: Direction, currentOpenings: Set<Direction>, nextOpenings: Set<Direction>) {
        if (!nextOpenings.contains(direction) || !currentOpenings.contains(direction.opposite)) {
            when (direction) {
                Directions.UP -> position = Position(position.first, (position.second - 1) mod sizeY)
                Directions.RIGHT -> position = Position((position.first + 1) mod sizeX, position.second)
                Directions.DOWN -> position = Position(position.first, (position.second + 1) mod sizeY)
                Directions.LEFT -> position = Position((position.first - 1) mod sizeX, position.second)
            }
        }
    }
}

abstract class MovingEntity(position: Position, sizeX: Int, sizeY: Int) : Entity(position, sizeX, sizeY) {
    override fun move(direction: Direction, currentOpenings: Set<Direction>, nextOpenings: Set<Direction>) {
        when (direction) {
            Directions.UP -> position = Position(position.first, (position.second - 1) mod sizeY)
            Directions.RIGHT -> position = Position((position.first + 1) mod sizeX, position.second)
            Directions.DOWN -> position = Position(position.first, (position.second + 1) mod sizeY)
            Directions.LEFT -> position = Position((position.first - 1) mod sizeX, position.second)
        }
    }
}

class Player(position: Position, sizeX: Int, sizeY: Int) : SlidingEntity(position, sizeX, sizeY) {
    override val color: Color = NamedColor.darkblue
}

class SlidingGoal(position: Position, sizeX: Int, sizeY: Int) : SlidingEntity(position, sizeX, sizeY) {
    override val color: Color = NamedColor.darkgreen
}

class MovingGoal(position: Position, sizeX: Int, sizeY: Int) : MovingEntity(position, sizeX, sizeY) {
    override val color: Color = NamedColor.darkred
}

class World(var sizeX: Int, var sizeY: Int, val setLevel: (Int) -> Unit) {
    val player = Player(Position(sizeX - 2, sizeY - 2), sizeX, sizeY)
    var level = 0

    val entities: MutableList<Entity> = mutableListOf(player)

    val elements: ArrayList<ArrayList<Part>> = ArrayList(Array(sizeY) {
        ArrayList(Array(sizeX) {
            Part()
        }.toList())
    }.toList())

    fun get(x: Int, y: Int): Part = elements[y mod sizeY][x mod sizeX]

    fun set(x: Int, y: Int, directions: Set<Direction>) {
        elements[y][x].set(directions.toMutableSet())
    }

    private fun setAll(method: (Int, Int) -> Set<Direction>) {
        (0 until sizeY).map { y ->
            (0 until sizeX).map { x ->
                elements[y][x].set(method(x, y))
            }
        }
    }

    private fun getRandomDirections(random: Random): Set<Direction> {
        val openings = mutableSetOf<Direction>()
        if (random.nextBoolean()) {
            openings += Directions.UP
        }
        if (random.nextBoolean()) {
            openings += Directions.RIGHT
        }
        if (random.nextBoolean()) {
            openings += Directions.DOWN
        }
        if (random.nextBoolean()) {
            openings += Directions.LEFT
        }
        return openings
    }

    private fun setEntityPaths(random: Random) {
        entities.forEach {
            while (get(it.position.first, it.position.second).openings.isEmpty()) {
                set(
                    it.position.first,
                    it.position.second,
                    getRandomDirections(random)
                )
            }
        }
    }

    private fun setAllRandomly(random: Random) {
        setAll { _, _ ->
            getRandomDirections(random)
        }
        setEntityPaths(random)
    }

    fun reset() {
        val random = Random(level)
        player.position = Position(sizeX - 2, sizeY - 2)
        entities.clear()
        entities.addAll(
            listOf(
                player,
                SlidingGoal(Position(1, 1), sizeX, sizeY)
            )
        )
        if (level > 0) {
            entities.add(MovingGoal(Position(sizeX - 2, 1), sizeX, sizeY))
        }
        if (level > 1) {
            entities.add(MovingGoal(Position(1, sizeY - 2), sizeX, sizeY))
        }
        (0..level).forEach {
            val x = random.nextInt() mod sizeX
            val y = random.nextInt() mod sizeY
            val moving = random.nextBoolean()
            if (entities.none { it.position.first == x && it.position.second == y }) {
                val entity = if (moving) {
                    MovingGoal(Position(x, y), sizeX, sizeY)
                } else {
                    SlidingGoal(Position(x, y), sizeX, sizeY)
                }

                entities.add(
                    entity
                )
            }
        }
        entities.forEach { it.alive = true }
        setAllRandomly(random)
        setEntityPaths(random)
    }

    private fun moveParts(index: Int, direction: Direction) {
        if (direction == Directions.UP) {
            val top = get(index, 0).openings.toSet()
            (0 until sizeY - 1).forEach { y ->
                val next = get(index, y + 1).openings.toSet()
                set(index, y, next)
            }
            set(index, sizeY - 1, top)
        }
        if (direction == Directions.RIGHT) {
            val right = get(sizeX - 1, index).openings.toSet()
            (sizeX - 1 downTo 1).forEach { x ->
                val next = get(x - 1, index).openings.toSet()
                set(x, index, next)
            }
            set(0, index, right)
        }
        if (direction == Directions.DOWN) { // TODO choose one style
            val bottom = elements[sizeY - 1][index].openings.toSet()
            (sizeY - 1 downTo 1).forEach { y ->
                val next = elements[y - 1][index].openings.toSet()
                elements[y][index].set(next)
            }
            elements[0][index].set(bottom)
        }
        if (direction == Directions.LEFT) {
            val left = elements[index][0].openings.toSet()
            (0 until sizeX - 1).forEach { x ->
                val next = elements[index][x + 1].openings.toSet()
                elements[index][x].set(next)
            }
            elements[index][sizeX - 1].set(left)
        }
    }

    fun incrementLevel() {
        level++
        setLevel(level + 1)
        reset()
    }

    fun move(direction: Direction) {
        val (xIndex, yIndex) = if (direction.isHorizontal) {
            null to player.position.second
        } else {
            player.position.first to null
        }

        val currentOpenings = entities.mapNotNull {
            if (xIndex == it.position.first || yIndex == it.position.second) {
                it to get(it.position.first, it.position.second).openings.toSet()
            } else {
                null
            }
        }

        if (direction.isHorizontal) {
            moveParts(player.position.second, direction)
        } else {
            moveParts(player.position.first, direction)
        }

        val nextOpenings = entities.mapNotNull {
            if (xIndex == it.position.first || yIndex == it.position.second) {
                it to get(it.position.first, it.position.second).openings.toSet()
            } else {
                null
            }
        }

        currentOpenings.map { current ->
            nextOpenings.find { next -> next.first == current.first }?.let { next ->
                current.first.move(direction, current.second, next.second)
            }
        }

        entities.forEach {
            if (it != player && it.position.first == player.position.first && it.position.second == player.position.second) {
                it.alive = false
            }
        }

        if (entities.filterNot { it == player || !it.alive }.isEmpty()) {
            incrementLevel()
        }
    }
}

class Labyrinth : ExternalCanvas() {
    override val name: String = "Labyrinth"

    override val component: FC<Props>
        get() = FC {
            val (size, _) = useState(8)
            val (levelState, setLevelState) = useState(1)
            val (worldState, _) = useState(World(size, size) { setLevelState(it) })

            fun draw() {
                renderingContext.clear()
                worldState.elements.forEachIndexed { y, list ->
                    list.forEachIndexed { x, part ->
                        val relX = canvasElement.getRelativeX(x, size)
                        val relY = canvasElement.getRelativeY(y, size)
                        val width = canvasElement.getElementWidth(size)

                        part.draw(relX, relY, width, renderingContext)
                    }
                }
                worldState.entities.forEach { it.draw(size, canvasElement, renderingContext) }
            }

            val keypressHandler: (Event) -> Unit = { event ->
                when ((event as KeyboardEvent).key.lowercase()) {
                    "w" -> worldState.move(Directions.UP)
                    "a" -> worldState.move(Directions.LEFT)
                    "s" -> worldState.move(Directions.DOWN)
                    "d" -> worldState.move(Directions.RIGHT)
                    "r" -> worldState.reset()
                    "k" -> worldState.incrementLevel()
                }
                draw()
            }

            val resizeHandler: (Event) -> Unit = {
                draw()
            }

            button {
                text = "Reset Level $levelState"
                onClick = {
                    worldState.reset()
                    draw()
                }
            }

            ReactHTML.canvas {
                css(Classes.canvas)
                id = canvasId
            }

            ReactHTML.div {
                className = ClassNames.phoneElement
                ReactHTML.div {
                    ReactHTML.button {
                        css {
                            width = 25.0.pct
                            color = backgroundColor
                            border = None.none
                        }
                    }
                    button {
                        text = "Up"
                        width = 50.0
                        onClick = {
                            worldState.move(Directions.UP)
                            draw()
                        }
                    }
                }
                ReactHTML.div {
                    css {
                        clear = Clear.left
                        margin = Auto.auto
                    }
                    buttonRow {
                        buttons = listOf(
                            Button("Left", false) {
                                worldState.move(Directions.LEFT)
                                draw()
                            },
                            Button("Right", false) {
                                worldState.move(Directions.RIGHT)
                                draw()
                            })
                    }
                }
                ReactHTML.div {
                    ReactHTML.button {
                        css {
                            width = 25.0.pct
                            color = backgroundColor
                            border = None.none
                        }
                    }
                    button {
                        text = "Down"
                        width = 50.0
                        onClick = {
                            worldState.move(Directions.DOWN)
                            draw()
                        }
                    }
                }
            }

            useEffectOnce {
                canvasElement.setDimensions(800, 800)
                worldState.reset()
                addEventListener("resize" to resizeHandler)
                addEventListener("keypress" to keypressHandler)
                draw()
            }
        }

    override fun cleanUp() {}

    override fun initialize() {}

    init {
        initEventListeners()
    }
}