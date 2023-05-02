import connect4.ai.AI
import connect4.ai.AIs
import connect4.ai.BattleHandler
import connect4.ai.length.BalancedLengthAI
import connect4.ai.length.PlyLengthAI
import connect4.ai.length.SimpleLengthAI
import connect4.ai.monte.BalancedMonteCarloAI
import connect4.ai.neural.*
import connect4.ai.simple.BiasedRandomAI
import connect4.ai.simple.RandomAI
import connect4.game.Player
import kotlinx.coroutines.*
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

fun combinations(neurals: List<NeuralAI>): List<List<NeuralAI>> {
    val result = mutableListOf<List<NeuralAI>>()

    // TODO recursive
    neurals.forEach { initial ->
        val remaining = neurals.filterNot { it == initial }
        remaining.forEach { remainder ->
            val two = listOf(initial, remainder)
            result += two
            val remainin = remaining.filterNot { it == remainder }
            remainin.forEach { remainde ->
                val three = two + remainde
                result += three
            }
        }
    }

    return result.map { l -> l.sortedBy { it.name } }.toSet().toList()
}

object CombinationCreator {
    fun fromNeurals(neurals: List<NeuralAI>): List<AI> {
        val combined = combinations(neurals).map { l -> l.sortedBy { it.name } }.toSet().toList()

        return neurals + combined.map {
            listOf(
                MostCommonAI(it), OverallHighestAI(it)
            )
        }.flatten()
    }
}

// TODO cleanup
class NeuralAITest {
    @Test
    fun repeatLast() {
        println(listOf(1).repeatLastInts().joinToString(","))
        println(listOf(1, 2, 3, 4, 5, 6, 7, 8).repeatLastInts().joinToString(","))
    }

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

        val moves = getTrainingMoves(listOf(BiasedRandomAI(), SimpleLengthAI(), PlyLengthAI()).map { { it } })

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
    fun neuralCombinationStrengthTest() {
        val handler = StoredHandler()
        handler.loadStored()

        val allCombinations = CombinationCreator.fromNeurals(handler.allNeurals())
        println(allCombinations.size)

        val battleHandler = BattleHandler(CombinationCreator.fromNeurals(handler.allNeurals()))

        val allHighest = mutableListOf<AI>()

        println("neurals")
        battleHandler.battle(handler.allNeurals())
        battleHandler.currentScore()
        println("highest:")
        println(battleHandler.highest().name)
        battleHandler.highestRanking(5).forEach { allHighest += it }
        battleHandler.resetBattles()

        val toEvaluate = listOf(
            "biasedRandom" to BiasedRandomAI(),
            "simpleLength" to SimpleLengthAI(),
            "balancedLength" to BalancedLengthAI(),
            "plyLength" to PlyLengthAI()
        )

        toEvaluate.forEach { (aiName, ai) ->
            println(aiName)
            repeat(20) {
                battleHandler.battle(listOf(ai))
            }
            battleHandler.currentScore(false, true)
            battleHandler.highestRanking(5).forEach { allHighest += it }
            battleHandler.resetBattles()
        }

        val toEvaluate2 = listOf(
            "monte500" to BalancedMonteCarloAI(500),
            "monte1000" to BalancedMonteCarloAI(1000),
        )

        val battleHandler2 = BattleHandler(handler.allNeurals() + allHighest.toSet().toList())

        toEvaluate2.forEach { (aiName, ai) ->
            println(aiName)
            repeat(10) {
                println(it)
                battleHandler2.battle(listOf(ai))
            }
            battleHandler2.currentScore()
            battleHandler2.resetBattles()
        }
    }

    @Test
    fun neuralChunkedStrengthTest() {
        val coarseHighest = mutableSetOf<AI>()

        val handler = StoredHandler()
        handler.loadStored(prefix = "pref")

        val chunks = handler.allNeurals().shuffled().chunked(10)

        runBlocking {
            chunks.map { neurals ->
                CoroutineScope(Dispatchers.Default).async {
                    val toEvaluate = listOf(
                        Triple("neural", handler.allNeurals(), 1),
                        Triple("simpleLength", listOf(SimpleLengthAI()), 100),
                        Triple("plyLength", listOf(PlyLengthAI()), 100),
                        Triple("monte300", listOf(BalancedMonteCarloAI(300)), 20),
                        Triple("monte500", listOf(BalancedMonteCarloAI(500)), 15)
                    )

                    val battleHandler = BattleHandler(neurals)

                    toEvaluate.forEach { (aiName, ais, repeat) ->
                        repeat(repeat) {
                            battleHandler.battle(ais)
                        }
                        println("$aiName: ${battleHandler.highest().name}")
                        coarseHighest += battleHandler.highest()
                        battleHandler.resetBattles()
                    }
                }
            }.awaitAll()
        }

        println("----")
        println("----")
        println("----")

        val fineHighest = mutableSetOf<AI>()

        runBlocking {
            coarseHighest.chunked(5).map { neurals ->
                CoroutineScope(Dispatchers.Default).async {
                    val toEvaluate = listOf(
                        Triple("neural", coarseHighest.toList(), 1),
                        Triple("simpleLength", listOf(SimpleLengthAI()), 100),
                        Triple("plyLength", listOf(PlyLengthAI()), 100),
                        Triple("monte500", listOf(BalancedMonteCarloAI(500)), 20)
                    )

                    val battleHandler = BattleHandler(neurals)

                    toEvaluate.forEach { (aiName, ais, repeat) ->
                        repeat(repeat) {
                            battleHandler.battle(ais)
                        }
                        println("$aiName: ${battleHandler.highest().name}")
                        fineHighest += battleHandler.highest()
                        battleHandler.resetBattles()
                    }
                }
            }.awaitAll()
        }

        println("----")
        println("----")
        println("----")

        fineHighest.forEach {
            println((it as NeuralAI).info())
        }

        println("----")
        println("----")
        println("----")

        val battleHandler = BattleHandler(fineHighest.toList())

        println("allNeurals")
        battleHandler.battle(handler.allNeurals())
        battleHandler.currentScore()
        battleHandler.resetBattles()

        val toEvaluate = listOf(
            "simpleLength" to listOf(SimpleLengthAI()),
            "balancedLength" to listOf(BalancedLengthAI()),
            "plyLength" to listOf(PlyLengthAI())
        )

        toEvaluate.forEach { (aiName, ais) ->
            println(aiName)
            repeat(500) {
                battleHandler.battle(ais)
            }
            battleHandler.currentScore()
            battleHandler.resetBattles()
        }

        println("500")
        runBlocking {
            (0..9).map {
                CoroutineScope(Dispatchers.Default).async {
                    battleHandler.battle(listOf(BalancedMonteCarloAI(500)))
                    println(".")
                    battleHandler.battle(listOf(BalancedMonteCarloAI(500)))
                    println(".")
                    battleHandler.battle(listOf(BalancedMonteCarloAI(500)))
                    println(".")
                    battleHandler.battle(listOf(BalancedMonteCarloAI(500)))
                }
            }.awaitAll()
        }
        battleHandler.currentScore()
        battleHandler.resetBattles()

        println("1000")
        runBlocking {
            (0..9).map {
                CoroutineScope(Dispatchers.Default).async {
                    battleHandler.battle(listOf(BalancedMonteCarloAI(1000)))
                    println(".")
                    battleHandler.battle(listOf(BalancedMonteCarloAI(1000)))
                }
            }.awaitAll()
        }
        battleHandler.currentScore()
    }

    @Test
    fun neuralChunkedAllStrengthTest() {
        val handler = StoredHandler()
        handler.loadStored()

        val allCombinations = CombinationCreator.fromNeurals(handler.allNeurals())
        println(allCombinations.size)

        val chunks = allCombinations.shuffled().chunked(10)

        val scores: Map<String, MutableMap<AI, Int>> =
            listOf(
                "randomAI",
                "simpleLength",
                "balancedLength",
                "plyLength",
                "monte1000"
            ).associateWith { mutableMapOf() }

        chunks.map { neurals ->
            runBlocking {
                neurals.map { neural ->
                    CoroutineScope(Dispatchers.Default).async {
                        val toEvaluate = listOf(
                            Triple("randomAI", listOf(RandomAI()), 500),
                            Triple("simpleLength", listOf(SimpleLengthAI()), 500),
                            Triple("balancedLength", listOf(BalancedLengthAI()), 500),
                            Triple("plyLength", listOf(PlyLengthAI()), 500),
                            Triple("monte1000", listOf(BalancedMonteCarloAI(1000)), 10)
                        )

                        val battleHandler = BattleHandler(listOf(neural))

                        toEvaluate.forEach { (aiName, ais, repeat) ->
                            repeat(repeat) {
                                battleHandler.battle(ais)
                            }
                            val score = battleHandler.counters.first()
                            scores[aiName]!![neural] = score.gamesWon
                            battleHandler.resetBattles()
                        }
                        println(".")
                    }
                }.awaitAll()
            }
        }

        println("-----")
        println()

        scores.toList().map { it.first to it.second.toList() }.forEach { (aiName, score) ->
            println("------")
            println(aiName)
            score.sortedByDescending { it.second }.forEach { (neural, gamesWon) ->
                println("${neural.name}: $gamesWon")
            }
            println("------")
            println()
        }
    }
}