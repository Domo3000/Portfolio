import connect4.ai.AI
import connect4.ai.AIs
import connect4.ai.neural.Move
import connect4.ai.neural.toDataset
import connect4.ai.neural.toFloatArray
import connect4.game.Connect4Game
import connect4.game.Player
import org.junit.Test

private fun getTrainingMoves(players: List<AI>): List<Move> {
    val winningMoves = players.map { p1 ->
        players.mapNotNull { p2 ->
            val result = Connect4Game.runGame(p1, p2)
            if (result.first != null) {
                result.second to result.first
            } else {
                null
            }
        }
    }.flatten()

    return winningMoves.map { (g, p) ->
        val game = Connect4Game()

        g.map { m ->
            val move = Move(game.field.map { it.toList() }, m)

            val allMoves = game.availableColumns.map { column ->
                val maybeWinningGame = Connect4Game(game.field, game.currentPlayer)
                maybeWinningGame.makeMove(column)
                if (maybeWinningGame.hasFinished()) {
                    Move(game.field.map { it.toList() }, column)
                } else {
                    null
                }
            } + if (game.currentPlayer == p) {
                move
            } else {
                null
            }

            game.makeMove(m)

            allMoves.filterNotNull()
        }.flatten()
    }.flatten()
}

class NeuralAITest {
    @Test
    fun testNormalizeMoves() {
        val moves = getTrainingMoves(AIs.highAIs.map { it() })

        val firstPlayerFloatArray = moves.map { move ->
            move.field.toFloatArray(Player.FirstPlayer)
        }

        val firstPlayerDataset = moves.toDataset(Player.FirstPlayer)

        println("firstPlayer")
        var c = 0
        firstPlayerFloatArray.forEach {
            var counter = 0
            it.forEach {  f ->
                if(counter++ % 7 == 0) {
                    println()
                }
                when {
                    f > 0.5f -> print("A")
                    f < -0.5f -> print("B")
                    else -> print("X")
                }
            }
            println()
            println(firstPlayerDataset.getY(c++))
        }

        val secondPlayerFloatArray = moves.map { move ->
            move.field.toFloatArray(Player.SecondPlayer)
        }

        val secondPlayerDataset = moves.toDataset(Player.SecondPlayer)

        println("secondPlayer")
        c = 0
        secondPlayerFloatArray.forEach {
            var counter = 0
            it.forEach {  f ->
                if(counter++ % 7 == 0) {
                    println()
                }
                when {
                    f > 0.5f -> print("A")
                    f < -0.5f -> print("B")
                    else -> print("X")
                }
            }
            println()
            println(secondPlayerDataset.getY(c++))
        }
    }
}