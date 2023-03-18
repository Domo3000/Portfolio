package connect4.ai.length

import connect4.game.Connect4Game
import connect4.game.Player

// TODO also check if length 4 would even be possible
class PlyLengthAI: LengthAI() {
    private fun nextHighestOpponentScore(column: Int, field: List<List<Player?>>, player: Player): Int {
        val game = Connect4Game(field, player)
        game.makeMove(column)
        return game.availableColumns.find { it == column }?.let {
            val newField = game.field
            val newOpponentScore = getHighestForPlayer(newField, listOf(column), player.switch())
            newOpponentScore?.points ?: 0
        } ?: 0
    }

    private fun filteredAndSorted(scores: List<Score>, field: List<List<Player?>>, player: Player) =
        scores.filter { nextHighestOpponentScore(it.column, field, player) < 4 }
            .sortedByDescending { it.points }

    override fun nextMove(field: List<List<Player?>>, availableColumns: List<Int>, player: Player): Int {
        val mine = getScoresForPlayer(field, availableColumns, player)
        val opponent = getScoresForPlayer(field, availableColumns, player.switch())

        val myHighestScore = mine.randomHighest()?.points ?: 0
        val opponentHighestScore = opponent.randomHighest()?.points ?: 0

        return when {
            myHighestScore >= 4 -> mine.randomHighest()!!.column
            opponentHighestScore >= 4 -> opponent.randomHighest()!!.column
            myHighestScore == 1 && opponentHighestScore == 1 -> {
                availableColumns[availableColumns.size / 2]
            }
            else -> {
                val nextFromMine = filteredAndSorted(mine, field, player)
                val nextFromOpponent = filteredAndSorted(opponent, field, player)

                nextFromMine.filter { it.points >= (nextFromOpponent.firstOrNull()?.points ?: 0) }.randomHighest()?.let {
                    return it.column
                }

                nextFromOpponent.randomHighest()?.let {
                    return it.column
                }

                availableColumns.random()
            }
        }
    }

    // unused
    override fun decision(mine: Score, opponent: Score, availableColumns: List<Int>): Int = 0
}