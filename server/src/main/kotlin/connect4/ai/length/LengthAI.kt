package connect4.ai.length

import connect4.ai.AI
import connect4.game.Player
import connect4.game.sizeY

data class Position(val row: Int, val column: Int)

data class Score(val points: Int, val column: Int)

fun List<Score>.randomHighest() = groupBy { it.points }.maxByOrNull { it.key }?.let { (_, entries) ->
    entries.random()
}

abstract class LengthAI : AI() {
    override val name: String = javaClass.simpleName

    private fun checkRecursively(
        element: Position,
        positions: Map<Position, Player?>,
        player: Player,
        offset: Position
    ): Pair<Int, Int> {
        val next = Position(element.row + offset.row, element.column + offset.column)
        return if(positions.containsKey(next)) {
            positions[next]?.let {
                if (it == player) {
                    val nextStep = checkRecursively(next, positions, player, offset)
                    1 + nextStep.first to 1 + nextStep.second
                } else {
                    0 to 0
                }
            } ?: run {
                val nextStep = checkRecursively(next, positions, player, offset)
                0 to 1 + nextStep.second
            }
        } else {
            0 to 0
        }
    }

    private fun getHighestPositions(field: List<List<Player?>>, availableColumns: List<Int>) =
        availableColumns.map { column ->
            val row = (0 until sizeY)
                .filter { row -> field[row][column] == null }
                .maxBy { it }

            Position(row, column)
        }

    private fun getScores(highestColumns: List<Position>, positions: Map<Position, Player?>, player: Player) =
        highestColumns.mapNotNull { position ->
            (0..3).map { direction ->
                val offsetX = if (direction == 0) 0 else 1
                val offsetY = when (direction) {
                    2 -> 0
                    3 -> 1
                    else -> -1
                }

                val checkLeft = checkRecursively(
                    position,
                    positions,
                    player,
                    Position(offsetX, offsetY)
                )

                val checkRight = checkRecursively(
                    position,
                    positions,
                    player,
                    Position(-offsetX, -offsetY)
                )

                (1 + checkLeft.first + checkRight.first) to (1 + checkLeft.second + checkRight.second)
            }.filter { (_, maxScore) -> maxScore >= 4 }.map { (score, _) -> score }.maxByOrNull { it }
                ?.let { highest -> Score(highest, position.column) }
        }

    fun getScoresForPlayer(
        field: List<List<Player?>>,
        availableColumns: List<Int>,
        player: Player
    ): List<Score> {
        val highestColumns = getHighestPositions(field, availableColumns)

        val playerPositions = field.mapIndexed { y, row ->
            row.mapIndexed { x, element ->
                Position(y, x) to element
            }
        }.flatten().toMap()

        return getScores(highestColumns, playerPositions, player)
    }

    fun getHighestForPlayer(
        field: List<List<Player?>>,
        availableColumns: List<Int>,
        player: Player
    ): Score? = getScoresForPlayer(field, availableColumns, player).randomHighest()

    override fun nextMove(field: List<List<Player?>>, availableColumns: List<Int>, player: Player): Int {
        val mine = getHighestForPlayer(field, availableColumns, player)
        val opponent = getHighestForPlayer(field, availableColumns, player.switch())

        mine ?: run {
            return opponent?.column ?: availableColumns[availableColumns.size / 2]
        }

        opponent ?: run {
            return mine.column
        }

        return decision(mine, opponent, availableColumns)
    }

    abstract fun decision(mine: Score, opponent: Score, availableColumns: List<Int>): Int
}

class BalancedLengthAI : LengthAI() {
    override fun decision(mine: Score, opponent: Score, availableColumns: List<Int>): Int = when {
        opponent.points < 2 && mine.points < 2 -> availableColumns[availableColumns.size / 2]
        mine.points > opponent.points -> mine.column
        else -> opponent.column
    }
}

class SimpleLengthAI : LengthAI() {
    override fun decision(mine: Score, opponent: Score, availableColumns: List<Int>): Int =
        if (mine.points >= opponent.points) {
            mine.column
        } else {
            opponent.column
        }
}

class DumbLengthAI : LengthAI() {
    override fun decision(mine: Score, opponent: Score, availableColumns: List<Int>): Int =
        if (mine.points >= opponent.points) {
            opponent.column
        } else {
            mine.column
        }
}

class AggressiveLengthAI : LengthAI() {
    override fun decision(mine: Score, opponent: Score, availableColumns: List<Int>): Int =
        mine.column
}

class DefensiveLengthAI : LengthAI() {
    override fun decision(mine: Score, opponent: Score, availableColumns: List<Int>): Int =
        opponent.column
}