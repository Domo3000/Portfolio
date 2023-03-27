import connect4.ai.AIs
import connect4.ai.length.PlyLengthAI
import connect4.ai.length.SimpleLengthAI
import connect4.ai.neural.*
import connect4.ai.simple.BiasedRandomAI
import connect4.game.Player
import org.jetbrains.kotlinx.dl.api.core.activation.Activations
import org.jetbrains.kotlinx.dl.api.core.initializer.Constant
import org.jetbrains.kotlinx.dl.api.core.initializer.GlorotNormal
import org.jetbrains.kotlinx.dl.api.core.initializer.Zeros
import org.jetbrains.kotlinx.dl.api.core.loss.Losses
import org.jetbrains.kotlinx.dl.api.core.metric.Metrics
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

fun List<Int>.repeatLastInts(): List<Int> {
    return if(size >= 2) {
        this + takeLast(size / 2).repeatLastInts()
    } else {
        this
    }
}

class NeuralAITest {
    @Test
    fun repeatLast() {
        println(listOf(1).repeatLastInts().joinToString(","))
        println(listOf(1, 2, 3, 4, 5, 6, 7, 8).repeatLastInts().joinToString(","))
    }

    @Test
    fun testNormalizeMoves() {
        val moves = getTrainingMoves(AIs.highAIs.map { it() })

        val firstPlayerFloatArray = moves.map { move ->
            move.field.toFloatArraySingular(Player.FirstPlayer)
        }

        val firstPlayerDataset = moves.toDataset(Player.FirstPlayer, true)

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
            move.field.toFloatArraySingular(Player.SecondPlayer)
        }

        val secondPlayerDataset = moves.toDataset(Player.SecondPlayer, true)

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

    @Test
    fun storeAndLoad() {
        val ai = RandomNeuralAI(
            training = emptyList(),
            inputType = true,
            conv = listOf(
                NeuralAIFactory.conv2D(3, 4, Activations.LiSHT, GlorotNormal(1), Zeros())
            ),
            dense = listOf(
                NeuralAIFactory.dense(
                    28,
                    Activations.HardSigmoid,
                    GlorotNormal(1),
                    Constant(0.5f)
                ),
            ),
            output = NeuralAIFactory.dense(
                7,
                Activations.Linear,
                GlorotNormal(1),
                Constant(0.5f)
            ),
            losses = Losses.SOFT_MAX_CROSS_ENTROPY_WITH_LOGITS,
            metrics = Metrics.ACCURACY
        )

        val moves = getTrainingMoves(listOf(BiasedRandomAI(), SimpleLengthAI(), PlyLengthAI()))

        ai.store("t1")

        val untrained = ai.evaluate(moves)
        ai.train(moves)
        val trained = ai.evaluate(moves)

        ai.store("t2")

        val loaded1 = StoredNeuralAI.fromStorage("t1")
        val loaded2 = StoredNeuralAI.fromStorage("t2")

        val evalLoaded1 = loaded1.evaluate(moves)
        val evalLoaded2 = loaded2.evaluate(moves)

        println("$untrained = $evalLoaded1")
        assertEquals(untrained, evalLoaded1)
        println("$trained = $evalLoaded2")
        assertEquals(trained, evalLoaded2)
        assertNotEquals(evalLoaded1, evalLoaded2)
    }
}