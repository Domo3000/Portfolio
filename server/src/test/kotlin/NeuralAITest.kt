import connect4.ai.AIs
import connect4.ai.neural.getTrainingMoves
import connect4.ai.neural.toDataset
import connect4.ai.neural.toFloatArray
import connect4.game.Player
import org.junit.Test

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