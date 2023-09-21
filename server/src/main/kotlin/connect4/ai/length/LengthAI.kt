package connect4.ai.length

import connect4.ai.AI
import connect4.game.Connect4Game
import connect4.game.Player
import connect4.game.sizeY
import java.time.Instant
import kotlin.random.Random

data class Position(val row: Int, val column: Int)

data class Score(val points: Int, val column: Int)

fun List<Score>.highest(): List<Score> = groupBy { it.points }.maxByOrNull { it.key }?.let { (_, entries) ->
    entries
} ?: emptyList()

fun List<Score>.randomHighest(random: Random): Score? = highest().randomOrNull(random)

abstract class LengthAI(seed: Long?) : AI() {
    abstract val ply: Boolean
    override val name
        get() = "${if(ply) "Ply" else ""}${javaClass.simpleName}"
    protected val random = Random(seed ?: Instant.now().toEpochMilli())

    private fun checkRecursively(
        element: Position,
        positions: Map<Position, Player?>,
        player: Player,
        offset: Position
    ): Pair<Int, Int> {
        val next = Position(element.row + offset.row, element.column + offset.column)
        return if (positions.containsKey(next)) {
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

    private fun nextHighestOpponentScore(column: Int, field: List<List<Player?>>, player: Player): Int {
        val game = Connect4Game(field, player)
        game.makeMove(column)
        return if (game.availableColumns.contains(column)) {
            getScoresForPlayer(game.field, listOf(column), game.currentPlayer).randomHighest(random)?.points ?: 0
        } else {
            0
        }
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

    private fun filterLosses(score: Score, field: List<List<Player?>>, player: Player) =
        if (ply && score.points < 4 && nextHighestOpponentScore(score.column, field, player) >= 4) {
            Score(-1, score.column)
        } else {
            score
        }


    override fun nextMove(field: List<List<Player?>>, availableColumns: List<Int>, player: Player): Int {
        val mine = getScoresForPlayer(field, availableColumns, player)
            .map { filterLosses(it, field, player) }
        val opponent = getScoresForPlayer(field, availableColumns, player.switch())
            .map { filterLosses(it, field, player) }

        return decide(mine, opponent, availableColumns)
    }

    private fun decide(mine: List<Score>, opponent: List<Score>, availableColumns: List<Int>): Int {
        val myHighestScore = mine.randomHighest(random)
        val opponentHighestScore = opponent.randomHighest(random)

        return when {
            mine.isEmpty() -> opponentHighestScore?.column ?: availableColumns.random(random)
            opponent.isEmpty() -> myHighestScore!!.column
            myHighestScore!!.points >= 4 -> myHighestScore.column
            opponentHighestScore!!.points >= 4 -> opponentHighestScore.column
            else -> decision(mine, opponent)
        }
    }

    abstract fun decision(mine: List<Score>, opponent: List<Score>): Int
}

class BalancedLengthAI(override val ply: Boolean, seed: Long?) : LengthAI(seed) {
    override fun decision(mine: List<Score>, opponent: List<Score>): Int {
        val myHighest = mine.randomHighest(random)!!
        val opponentHighest = opponent.randomHighest(random)!!

        return when {
            opponentHighest.points < 2 && myHighest.points < 2 -> mine[mine.size / 2].column
            myHighest.points >= opponentHighest.points -> myHighest.column
            else -> opponentHighest.column
        }
    }
}

class SimpleLengthAI(override val ply: Boolean, seed: Long?) : LengthAI(seed) {
    override fun decision(mine: List<Score>, opponent: List<Score>): Int {
        val myHighest = mine.randomHighest(random)!!
        val opponentHighest = opponent.randomHighest(random)!!

        return when {
            myHighest.points >= opponentHighest.points -> myHighest.column
            else -> opponentHighest.column
        }
    }
}

class AggressiveLengthAI(override val ply: Boolean = false, seed: Long?) : LengthAI(seed) {
    override fun decision(mine: List<Score>, opponent: List<Score>): Int =
        mine.randomHighest(random)!!.column
}

class DefensiveLengthAI(override val ply: Boolean = false, seed: Long?) : LengthAI(seed) {
    override fun decision(mine: List<Score>, opponent: List<Score>): Int =
        opponent.randomHighest(random)!!.column
}
