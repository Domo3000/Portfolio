package neurals

import connect4.ai.AI
import connect4.ai.BattleHandler
import connect4.ai.length.AggressiveLengthAI
import connect4.ai.length.BalancedLengthAI
import connect4.ai.length.DefensiveLengthAI
import connect4.ai.length.SimpleLengthAI
import connect4.ai.monte.BalancedMonteCarloAI
import connect4.ai.monte.MaximizeWinsMonteCarloAI
import connect4.ai.monte.MinimizeLossesMonteCarloAI
import connect4.ai.simple.AlwaysSameAI
import connect4.ai.simple.BiasedRandomAI
import connect4.ai.simple.RandomAI
import connect4.game.Connect4Game
import connect4.game.Player
import kotlinx.coroutines.*
import neural.NeuralAI
import neural.StoredHandler
import neural.StoredNeuralAI
import org.junit.Test
import java.time.Instant
import kotlin.math.ceil
import kotlin.random.Random

class AIStrengthTest {
    private val random = Random(Instant.now().toEpochMilli())
    private val path = "neurals"

    private fun toEvaluate(accuracy: Int) = listOf(
        { BiasedRandomAI(random.nextLong()) } to 100 * accuracy,
        { DefensiveLengthAI(false, random.nextLong()) } to 100 * accuracy,
        { DefensiveLengthAI(true, random.nextLong()) } to 100 * accuracy,
        { AggressiveLengthAI(false, random.nextLong()) } to 100 * accuracy,
        { AggressiveLengthAI(true, random.nextLong()) } to 100 * accuracy,
        { BalancedLengthAI(false, random.nextLong()) } to 100 * accuracy,
        { BalancedLengthAI(true, random.nextLong()) } to 100 * accuracy,
        { SimpleLengthAI(false, random.nextLong()) } to 100 * accuracy,
        { SimpleLengthAI(true, random.nextLong()) } to 100 * accuracy,
        { BalancedMonteCarloAI(300, random.nextLong()) } to 5 * accuracy,
        { BalancedMonteCarloAI(500, random.nextLong()) } to 3 * accuracy,
        { BalancedMonteCarloAI(800, random.nextLong()) } to 2 * accuracy,
        { BalancedMonteCarloAI(1000, random.nextLong()) } to 1 * accuracy
    )

    private fun rateOverallChunked(
        ais: List<AI>,
        accuracy: Int,
        printAllResults: Boolean,
        printInfo: Boolean
    ): List<AI> {
        val scores = mutableMapOf<AI, List<Double>>()

        toEvaluate(accuracy).forEach { (opponent, repeat) ->
            println("----")
            println(opponent().name)
            val battleHandler = BattleHandler(ais)
            battleHandler.chunkedSingleOpponentBattle(opponent, repeat, 5) {
                println(".")
            }
            battleHandler.currentScore(printAllResults, true)
            battleHandler.counters.map { c ->
                val newScore = (scores[c.ai] ?: emptyList()) + listOf(c.score.toDouble() / c.maxScore.toDouble())
                scores.put(c.ai, newScore)
            }
        }

        val sortedScores = scores.toList().sortedByDescending { it.second.average() }

        if (printInfo) {
            println("----")
            sortedScores.forEach { (ai, score) ->
                println("${score.average()}: ${(ai as NeuralAI).info()}")
            }
            println("----")
        }

        return sortedScores.map { it.first }
    }

    /*
        if you only have a few Neurals to check use this one
     */
    @Test
    fun neuralAllStrengthTest() {
        rateOverallChunked(StoredHandler.loadStored(path), 10, true, true)
    }

    /*
     *   after training you've got hundreds of Neurals so use this to evaluate them
     */
    @Test
    fun neuralChunkedStrengthTest() {
        val neurals = StoredHandler.loadStored(path).toMutableList()

        val coarse = listOf(
            Triple(5, 0.5, true),
            Triple(5, 0.333, true),
            Triple(10, 0.5, true),
            Triple(25, 1.0, true)
        )

        coarse.forEach { (accuracy, toKeep, print) ->
            val scored = rateOverallChunked(neurals.toList(), accuracy, print, true)
            val keep = ceil((neurals.size * toKeep)).toInt()
            neurals.clear()
            neurals.addAll(scored.take(keep).map { it as StoredNeuralAI })
        }
    }

    private fun evaluateRelativeStrength(ai: AI, strength: Int): Boolean {
        val p1Result = Connect4Game.runGame(ai, BalancedMonteCarloAI(strength, random.nextLong()))
        val p2Result = Connect4Game.runGame(BalancedMonteCarloAI(strength, random.nextLong()), ai)

        return p1Result.first == Player.FirstPlayer || p2Result.first == Player.SecondPlayer
    }

    private fun evaluateRelativeStrengthParallel(
        ai: () -> AI,
        repeat: Int,
        chunkSize: Int
    ): Triple<Double, Int, Int> {
        val aiScore = mutableListOf<Int>()

        (0 until 3).forEach {
            var winning = true
            var strength = 100

            while (winning) {
                val results = (0 until repeat).chunked(chunkSize).flatMap { chunk ->
                    runBlocking {
                        chunk.map {
                            CoroutineScope(Dispatchers.Default).async {
                                val p1Result =
                                    Connect4Game.runGame(ai(), BalancedMonteCarloAI(strength, random.nextLong()))
                                val p2Result =
                                    Connect4Game.runGame(BalancedMonteCarloAI(strength, random.nextLong()), ai())

                                listOf(p1Result.first == Player.FirstPlayer, p2Result.first == Player.SecondPlayer)
                            }
                        }.awaitAll().flatten()
                    }
                }

                val lost = results.filterNot { it }.size
                val won = results.filter { it }.size

                println("$won/${results.size}")
                if (lost > won) {
                    winning = false
                } else {
                    strength += 100
                }
            }
            println(strength)
            aiScore += strength
        }

        return Triple(aiScore.average(), aiScore.min(), aiScore.max())
    }

    private fun relativeAIStrength(ais: List<() -> AI>, repeat: Int = 20, chunkSize: Int = 5) {
        ais.shuffled().map { ai ->
            val result = evaluateRelativeStrengthParallel(ai, repeat, chunkSize)
            println("${ai().name}: ${result.first}")
            ai() to result
        }.sortedBy { it.second.first }.forEach { (ai, score) ->
            println("${ai.name}: ${score.first} (${score.second}-${score.third})")
        }
    }

    /*
    AlwaysSameAI(3): 100.0 (100-100)
    BiasedRandomAI: 100.0 (100-100)
    RandomAI: 100.0 (100-100)
    AggressiveLengthAI: 266.6666666666667 (200-300)
    DefensiveLengthAI: 266.6666666666667 (200-300)
    PlyDefensiveLengthAI: 300.0 (200-400)
    PlyAggressiveLengthAI: 333.3333333333333 (300-400)
    SimpleLengthAI: 466.6666666666667 (300-600)
    BalancedLengthAI: 500.0 (500-500)
    PlySimpleLengthAI: 633.3333333333334 (600-700)
    PlyBalancedLengthAI: 866.6666666666666 (500-1100)
     */
    @Test
    fun relativeFastAiStrength() {
        val ais = listOf(
            { AlwaysSameAI(3) },
            { RandomAI() },
            { BiasedRandomAI() },
            { DefensiveLengthAI(false, random.nextLong()) },
            { DefensiveLengthAI(true, random.nextLong()) },
            { AggressiveLengthAI(false, random.nextLong()) },
            { AggressiveLengthAI(true, random.nextLong()) },
            { BalancedLengthAI(false, random.nextLong()) },
            { SimpleLengthAI(false, random.nextLong()) },
            { BalancedLengthAI(true, random.nextLong()) },
            { SimpleLengthAI(true, random.nextLong()) }
        )

        relativeAIStrength(ais)
    }

    @Test
    fun relativeMonteCarloAiStrength() {
        val ais = listOf(
            { MaximizeWinsMonteCarloAI(300, random.nextLong()) },
            { MaximizeWinsMonteCarloAI(500, random.nextLong()) },
            { MinimizeLossesMonteCarloAI(300, random.nextLong()) },
            { MinimizeLossesMonteCarloAI(500, random.nextLong()) },
            { BalancedMonteCarloAI(300, random.nextLong()) },
            { BalancedMonteCarloAI(500, random.nextLong()) },
            { BalancedMonteCarloAI(800, random.nextLong()) },
            { BalancedMonteCarloAI(1000, random.nextLong()) }
        )

        relativeAIStrength(ais, chunkSize = 5)
    }

    /*
    StoredNeural(5): 400.0 (300-500)
    StoredNeural(7): 466.6666666666667 (300-600)
    StoredNeural(1): 500.0 (400-600)
    StoredNeural(0): 500.0 (400-600)
    StoredNeural(3): 600.0 (500-700)
    StoredNeural(4): 600.0 (500-700)
    StoredNeural(6): 666.6666666666666 (500-900)
    StoredNeural(2): 733.3333333333334 (500-1000)
     */
    @Test
    fun relativeNeuralAiStrength() {
        relativeAIStrength(StoredHandler.loadStored(path).map { { it } })
    }

    @Test
    fun neuralPrint() {
        val toEvaluate = listOf(
            SimpleLengthAI(false, 0L),
            BalancedLengthAI(false, 0L),
            SimpleLengthAI(true, 0L),
            BalancedLengthAI(true, 0L),
            BalancedMonteCarloAI(500, 0L)
        )

        StoredHandler.loadStored(path).forEach { neural ->
            toEvaluate.forEach { ai ->
                println(neural.info())
                println("${neural.name} vs ${ai.name}")
                Connect4Game.runGame(neural, ai, printResult = true)
                println("${ai.name} vs ${neural.name}")
                Connect4Game.runGame(ai, neural, printResult = true)
                println("${ai.name} vs ${ai.name}")
                Connect4Game.runGame(ai, ai, printResult = true)
            }
        }
    }
}
