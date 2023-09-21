package ai.neural

import connect4.ai.AI
import connect4.ai.BattleHandler
import connect4.ai.length.AggressiveLengthAI
import connect4.ai.length.BalancedLengthAI
import connect4.ai.length.DefensiveLengthAI
import connect4.ai.length.SimpleLengthAI
import connect4.ai.monte.BalancedMonteCarloAI
import connect4.ai.monte.MaximizeWinsMonteCarloAI
import connect4.ai.monte.MinimizeLossesMonteCarloAI
import connect4.ai.neural.*
import connect4.ai.simple.BiasedRandomAI
import connect4.ai.simple.RandomAI
import connect4.game.Connect4Game
import connect4.game.Player
import kotlinx.coroutines.*
import org.junit.Test
import java.time.Instant
import kotlin.math.ceil
import kotlin.random.Random

private fun combinations(neurals: List<NeuralAI>): List<List<NeuralAI>> {
    val result = mutableListOf<List<NeuralAI>>()

    // TODO recursive
    neurals.forEach { initial ->
        val remaining = neurals.filterNot { it == initial }
        remaining.forEach { remainder ->
            val two = listOf(initial, remainder)
            //result += two
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

class AIStrengthTest {
    private val random = Random(Instant.now().toEpochMilli())

    private fun toEvaluate(accuracy: Int) = listOf(
        { SimpleLengthAI(false, random.nextLong()) } to 100 * accuracy,
        { BalancedLengthAI(false, random.nextLong()) } to 100 * accuracy,
        { SimpleLengthAI(true, random.nextLong()) } to 100 * accuracy,
        { BalancedLengthAI(true, random.nextLong()) } to 100 * accuracy,
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

        if(printInfo) {
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
        val handler = StoredHandler()
        handler.loadStoredNeurals()

        rateOverallChunked(handler.allNeurals(), 25, true, true)
    }

    /*
     *   after training you've got hundreds of Neurals so use this to evaluate them
     */
    @Test
    fun neuralChunkedStrengthTest() {
        val handler = StoredHandler()
        handler.loadStoredNeurals()

        val grouped = handler.allNeurals().map {
            it.toRandomNeural()
        }.groupBy { it.shortInfo() }.mapValues { (_, value) ->
            value.toMutableList()
        }

        val coarse = listOf(
            Triple(5, 0.333, false),
            Triple(10, 0.5, true),
            Triple(25, 1.0, true)
        )

        coarse.forEach { (accuracy, toKeep, print) ->
            grouped.forEach {(groupName, ais) -> run {
                println(groupName)
                println(ais.size)
                val scored = rateOverallChunked(ais.toList(), accuracy, print, true)
                val keep = ceil((ais.size * toKeep)).toInt()
                ais.clear()
                ais.addAll(scored.take(keep).map { it as RandomNeuralAI })
            }}
        }
    }

    /*
     *   only use this for very low amount of Neurals, like between 3 and 9
     */
    @Test
    fun neuralAllCombinationsStrengthTest() {
        val handler = StoredHandler()
        handler.loadStoredNeurals()

        val allCombinations = CombinationCreator.fromNeurals(handler.allNeurals())

        val coarse = listOf(
            Triple(1, 0.333, false),
            Triple(3, 0.333, true),
            Triple(10, 1.0, true)
        )

        val ais: MutableList<AI> = allCombinations.toMutableList()

        coarse.forEach { (accuracy, toKeep, print) ->
            println(ais.size)
            val scored = rateOverallChunked(ais.toList(), accuracy, print, false)
            val keep = ceil((ais.size * toKeep)).toInt()
            ais.clear()
            ais.addAll(scored.take(keep).map { it as RandomNeuralAI })
        }
    }

    private fun evaluateRelativeStrength(ai: AI): Int {
        var winning = true
        var strength = 50

        while (winning) {
            val p1Result = Connect4Game.runGame(ai, BalancedMonteCarloAI(strength, random.nextLong()))
            val p2Result = Connect4Game.runGame(BalancedMonteCarloAI(strength, random.nextLong()), ai)

            if (p1Result.first != Player.FirstPlayer && p2Result.first != Player.SecondPlayer) {
                winning = false
            }
            strength += 50
        }

        return strength
    }

    private fun evaluateRelativeStrengthParallel(
        ai: () -> AI,
        repeat: Int,
        chunkSize: Int
    ): Triple<Double, Int, Int> {
        val aiScore = mutableListOf<Int>()

        runBlocking {
            (0 until repeat).chunked(chunkSize).map { chunk ->
                chunk.map {
                    CoroutineScope(Dispatchers.Default).async {
                        aiScore += evaluateRelativeStrength(ai())
                    }
                }.awaitAll()
            }
        }

        val result = Triple(aiScore.average(), aiScore.min(), aiScore.max());
        return result
    }

    private fun relativeAIStrength(ais: List<() -> AI>, repeat: Int = 20, chunkSize: Int = 10) {
        ais.shuffled().map { ai ->
            val result = evaluateRelativeStrengthParallel(ai, repeat, chunkSize)
            println("${ai().name}: ${result.first}")
            ai() to result
        }.sortedBy { it.second.first }.forEach { (ai, score) ->
            println("${ai.name}: ${score.first} (${score.second}-${score.third})")
        }
    }

    @Test
    fun relativeFastAiStrength() {
        val ais = listOf(
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

    @Test
    fun relativeNeuralAiStrength() {
        val handler = StoredHandler()
        handler.loadStoredNeurals()

        relativeAIStrength(handler.allNeurals().map { { it } })
    }

    @Test
    fun neuralPrint() {
        val handler = StoredHandler()
        handler.loadStoredNeurals()

        val toEvaluate = listOf(
            SimpleLengthAI(false, 0L),
            BalancedLengthAI(false, 0L),
            SimpleLengthAI(true, 0L),
            BalancedLengthAI(true, 0L),
            BalancedMonteCarloAI(500, 0L)
        )

        handler.allNeurals().forEach { neural ->
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
