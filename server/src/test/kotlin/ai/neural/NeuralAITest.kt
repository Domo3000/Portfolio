package ai.neural

import org.junit.Test
import java.time.Instant
import kotlin.random.Random

private fun List<Int>.repeatLastInts(): List<Int> {
    return if (size >= 2) {
        this + takeLast(size / 2).repeatLastInts()
    } else {
        this
    }
}

// TODO cleanup
class NeuralAITest {
    private val random = Random(Instant.now().toEpochMilli())

    @Test
    fun repeatLast() {
        println(listOf(1).repeatLastInts().joinToString(","))
        println(listOf(1, 2, 3, 4, 5, 6, 7, 8).repeatLastInts().joinToString(","))
    }

    /*
    @Test
    fun testNormalizeMoves() {
        val moves = getTrainingMoves(AIs.highAIs)

        val firstPlayerFloatArray = moves.map { move ->
            move.field.toFloatArraySingular(Player.FirstPlayer)
        }

        val firstPlayerDataset = moves.toDataset(Player.FirstPlayer, true)

        println("firstPlayer")
        var c = 0
        firstPlayerFloatArray.forEach {
            var counter = 0
            it.forEach { f ->
                if (counter++ % 7 == 0) {
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
            move.field.toFloatArraySingular(Player.SecondPlayer)
        }

        val secondPlayerDataset = moves.toDataset(Player.SecondPlayer, true)

        println("secondPlayer")
        c = 0
        secondPlayerFloatArray.forEach {
            var counter = 0
            it.forEach { f ->
                if (counter++ % 7 == 0) {
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
    */
}
