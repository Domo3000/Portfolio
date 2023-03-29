import connect4.ai.AI
import connect4.ai.AIs
import connect4.ai.BattleHandler
import connect4.ai.length.PlyLengthAI
import connect4.ai.length.SimpleLengthAI
import connect4.ai.monte.BalancedMonteCarloAI
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
    return if (size >= 2) {
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

    @Test
    fun info() {
        val ai1 = RandomNeuralAI(
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
        val ai2 = RandomNeuralAI(
            training = emptyList(),
            inputType = false,
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

        println(ai1.info())
        println(ai2.info())

        ai1.store("t1")
        ai2.store("t2")

        val loaded1 = StoredNeuralAI.fromStorage("t1")
        val loaded2 = StoredNeuralAI.fromStorage("t2")

        println(loaded1.info())
        println(loaded2.info())
    }

    @Test
    fun neuralPrefixedStrengthTest() {
        val handler = StoredHandler()
        handler.loadStored(emptyList(), "c", silent = true)

        val toEvaluate = listOf(
            "neural" to handler.allNeurals(),
            "simpleLength" to listOf(SimpleLengthAI()),
            "plyLength" to listOf(PlyLengthAI())
        )

        val battleHandler = BattleHandler(handler.allNeurals())

        toEvaluate.forEach { (aiName, ais) ->
            println(aiName)
            repeat(20) {
                battleHandler.battle(ais)
            }
            println(battleHandler.highest().name)
            battleHandler.resetBattles()
        }

        val toEvaluate2 = listOf(
            "monte500" to listOf(BalancedMonteCarloAI(500)),
            "monte1000" to listOf(BalancedMonteCarloAI(1000)),
        )

        toEvaluate2.forEach { (aiName, ais) ->
            println(aiName)
            repeat(10) {
                println(it)
                battleHandler.battle(ais)
            }
            println(battleHandler.highest().name)
            battleHandler.resetBattles()
        }
    }

    @Test
    fun neuralFilteredStrengthTest() {
        val allHighest = mutableListOf<AI>()

        //loads prefix0 to prefix199 in chunks of 20
        (0..9).forEach { add ->
            val toLoad = (0..19).map { index ->
                index + (20 * add)
            }.map { "sm$it" }

            val handler = StoredHandler()
            handler.loadStored(toLoad, silent = true)

            val toEvaluate = listOf(
                "neural" to handler.allNeurals(),
                "simpleLength" to listOf(SimpleLengthAI()),
                "plyLength" to listOf(PlyLengthAI()),
            )

            val battleHandler = BattleHandler(handler.allNeurals())

            toEvaluate.forEach { (aiName, ais) ->
                repeat(20) {
                    battleHandler.battle(ais)
                }
                println("$aiName: ${battleHandler.highest().name}")
                allHighest += battleHandler.highest()
                battleHandler.resetBattles()
            }
        }

        println("----")
        println("----")
        println("----")

        val toEvaluate = listOf(
            "neural" to allHighest,
            "simpleLength" to listOf(SimpleLengthAI()),
            "plyLength" to listOf(PlyLengthAI()),
            "monte500" to listOf(BalancedMonteCarloAI(500))
        )

        val battleHandler = BattleHandler(allHighest)

        toEvaluate.forEach { (aiName, ais) ->
            println(aiName)
            repeat(20) {
                battleHandler.battle(ais)
            }
            println(battleHandler.highest().name)
            battleHandler.resetBattles()
        }

        println("monte1000")
        repeat(10) {
            println(it)
            battleHandler.battle(listOf(BalancedMonteCarloAI(1000)))
        }
        println(battleHandler.highest().name)
    }
}