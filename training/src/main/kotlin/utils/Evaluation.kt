package utils

import ai.AI
import ai.BattleCounter
import ai.BattleHandler
import connect4.game.Connect4Game
import connect4.game.Player
import neural.Move

fun evaluateBattles(
    ai: AI,
    battlePlayers: List<() -> AI>
): Double = battlePlayers.map { opponent ->
    val counter = BattleCounter(ai)

    repeat(25) {
        BattleHandler.fight(counter, opponent)
    }

    counter.score.toDouble() / counter.maxScore.toDouble()
}.average()

fun evaluateWinningMoves(
    ai: AI,
    winningMoves: List<Pair<Move, Player>>
): Double {
    val counter = BattleCounter(ai)
    counter.maxScore = winningMoves.size

    winningMoves.map { (move, player) ->
        val game = Connect4Game(move.field, player)

        if (ai.nextMove(game) == move.move) {
            counter.score++
        }
    }

    return counter.score.toDouble() / counter.maxScore.toDouble()
}
