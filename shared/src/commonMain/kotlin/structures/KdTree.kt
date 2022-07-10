package structures

sealed class Orientation(private val name: String) {
    override fun toString(): String = name

    abstract fun switch(): Orientation
}

object Horizontal : Orientation("Horizontal") {
    override fun switch(): Orientation = Vertical
}

object Vertical : Orientation("Vertical") {
    override fun switch(): Orientation = Horizontal
}

data class RelativePosition(val x: Int, val y: Int) {
    override fun toString(): String = "($x/$y)"
}

data class Node(
    val position: RelativePosition,
    val orientation: Orientation,
    val left: Node? = null,
    val right: Node? = null
) {
    fun insert(newPosition: RelativePosition): Node {
        return when (orientation) {
            is Horizontal -> if (position.y > newPosition.y) {
                this.copy(left = left.insertOrNew(newPosition, orientation))
            } else {
                this.copy(right = right.insertOrNew(newPosition, orientation))
            }
            is Vertical -> if (position.x > newPosition.x) {
                this.copy(left = left.insertOrNew(newPosition, orientation))
            } else {
                this.copy(right = right.insertOrNew(newPosition, orientation))
            }
        }
    }

    fun size(): Int = 1 + (left?.size() ?: 0) + (right?.size() ?: 0)

    fun toList(): List<Node> = listOf(this) + (left?.toList() ?: emptyList()) + (right?.toList() ?: emptyList())

    override fun toString(): String =
        "{ position: $position, orientation: $orientation, left: ${left?.toString() ?: "noLeft"}, right: ${right?.toString() ?: "noRight"} }"

    override fun hashCode(): Int {
        var result = position.x
        result = 31 * result + position.y
        result = 31 * result + position.hashCode()
        result = 31 * result + orientation.hashCode()
        return result
    }
}

private fun Node?.insertOrNew(newPosition: RelativePosition, orientation: Orientation): Node =
    this?.insert(newPosition) ?: Node(newPosition, orientation.switch())

fun rebalance(root: Node, orientation: Orientation = Horizontal): Node =
    rebalance(root.toList().map { it.position }, orientation)

fun rebalance(positions: List<RelativePosition>, orientation: Orientation): Node {
    val sortedPositions = when (orientation) {
        Horizontal -> positions.sortedBy { it.y }
        Vertical -> positions.sortedBy { it.x }
    }

    val (median, left, right) = when (val size = sortedPositions.size) {
        1 -> Triple(sortedPositions[0], emptyList(), emptyList())
        2 -> Triple(sortedPositions[0], emptyList(), listOf(sortedPositions[1]))
        3 -> Triple(sortedPositions[1], listOf(sortedPositions[0]), listOf(sortedPositions[2]))
        else -> Triple(
            sortedPositions[size / 2],
            sortedPositions.subList(0, size / 2),
            sortedPositions.subList((size / 2) + 1, size)
        )
    }

    val parent = Node(median, orientation)

    val leftNode = if (left.isEmpty()) {
        null
    } else {
        rebalance(left, orientation.switch())
    }

    val rightNode = if (right.isEmpty()) {
        null
    } else {
        rebalance(right, orientation.switch())
    }

    return parent.copy(left = leftNode, right = rightNode)
}