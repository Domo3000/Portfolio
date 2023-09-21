package ai.neural

import connect4.ai.AI
import connect4.ai.BattleHandler
import connect4.ai.length.*
import connect4.ai.monte.BalancedMonteCarloAI
import connect4.ai.monte.MaximizeWinsMonteCarloAI
import connect4.ai.neural.*
import connect4.ai.simple.BiasedRandomAI
import connect4.game.Connect4Game
import kotlinx.coroutines.*
import org.junit.Test
import java.time.Instant
import kotlin.random.Random

private fun getTrainingMovesParallel(trainingPlayers: List<() -> AI>, parallelism: Int = 5) = runBlocking {
    println("gettingTrainingMoves")
    val moves = (0..parallelism).map {
        CoroutineScope(Dispatchers.Default).async {
            getTrainingMoves(trainingPlayers)
        }
    }.awaitAll()

    moves
}.flatten()

sealed interface Mode
data class Evolve(val keep: Int) : Mode
data class PurgeWeakest(val min: Int) : Mode

/*
    Use these tests to train new RandomNeurals in various ways
    use neuralFilteredStrengthTest or neuralChallenge to evaluate them later

    after some time neurals might get overfitted
    so after each iteration we store the highest and delete the weakest
    softEvolve creates copies of the strongest with new random activation functions
 */
class NeuralTraining {
    private val random = Random(Instant.now().toEpochMilli())

    private val trainingPlayers = listOf({ BalancedLengthAI(true, random.nextLong()) },
        { SimpleLengthAI(false, random.nextLong()) },
        { BalancedLengthAI(false, random.nextLong()) },
        { BalancedMonteCarloAI(800, random.nextLong()) })

    private fun evaluateAndTrain(
        prefix: String,
        newTrainingPlayers: List<() -> AI>,
        mode: Mode,
        createRandom: Int? = null,
        includeHighestInTraining: Int? = null,
        evaluate: (BattleHandler) -> Unit
    ) {
        println("start")
        val evolutionHandler = EvolutionHandler()

        //evolutionHandler.initPredefinedNeurals("basic") { random -> PredefinedNeurals.basic(random) }
        evolutionHandler.initPredefinedNeurals("complex") { random -> PredefinedNeurals.complex(random) }
        //evolutionHandler.initPredefinedNeurals("weird") { random -> PredefinedNeurals.weird(random) }

        val initialMoves = getTrainingMovesParallel(trainingPlayers)
        println(initialMoves.size)

        println("start initial training")
        evolutionHandler.train(evolutionHandler.allNeurals(), initialMoves)
        println("initial training done")

        var c = 0
        repeat(100) { i ->
            println("outerLoop $i")

            val battleHandler = BattleHandler(evolutionHandler.allNeurals())
            evaluate(battleHandler)
            battleHandler.currentScore(true, true)

            when(mode) {
                is Evolve -> {
                    println("test1")
                    evolutionHandler.setNeurals(battleHandler.highestRanking(mode.keep).map { it as RandomNeuralAI })
                }
                is PurgeWeakest -> {
                    println("test2")
                    if(evolutionHandler.allNeurals().size > mode.min) {
                        battleHandler.purgeWeakest()
                        evolutionHandler.setNeurals(battleHandler.counters.map { it.ai as RandomNeuralAI })
                    }
                }
            }

            battleHandler.highestRanking(3).forEach { counter ->
                val ai = counter as RandomNeuralAI
                println("highestRanking: $prefix${c} = ${ai.info()}")
                ai.store("$prefix${c++}")
            }

            val newMoves = getTrainingMovesParallel(newTrainingPlayers + (includeHighestInTraining?.let { highest ->
                battleHandler.highestRanking(highest).map { counter -> { counter } }
            } ?: emptyList()))

            println("got new trainingMoves: ${newMoves.size}")

            if(mode is Evolve) {
                println("softEvolve")
                battleHandler.highestRanking(1).forEach { ai ->
                    evolutionHandler.softEvolve(ai as RandomNeuralAI)
                }
            }

            createRandom?.let { amount ->
                println("create random")
                evolutionHandler.initWithRandom(amount, newMoves + newMoves)
            }

            println("start training")
            evolutionHandler.train(evolutionHandler.allNeurals(), newMoves)

            battleHandler.resetBattles()
        }
    }

    /*
    slowest, but probably most accurate

    actually plays against Length/MonteCarlo AIs
     */
    @Test
    fun battleEvolve() {
        var strongest: Pair<StoredNeuralAI?, Int> = null to 0

        val battlePlayers = listOf(
            Triple({ BiasedRandomAI() }, 1, 10),
            Triple({ BalancedLengthAI(false, random.nextLong()) }, 3, 100),
            Triple({ SimpleLengthAI(false, random.nextLong()) }, 3, 100),
            Triple({ BalancedLengthAI(true, random.nextLong()) }, 6, 100),
            Triple({ BalancedMonteCarloAI(500, random.nextLong()) }, 50, 10)
        )

        evaluateAndTrain(
            "battle",
            (trainingPlayers + strongest.first?.let { { it } }).filterNotNull(),
            Evolve(15)
        ) { battleHandler ->
            runBlocking {
                battleHandler.counters.map {
                    CoroutineScope(Dispatchers.Default).async {
                        battleHandler.battleScored(battlePlayers, it)
                    }
                }.awaitAll()
            }
            val highestCounter = battleHandler.highest()
            if (highestCounter.score > strongest.second && highestCounter.score > 800) {
                (highestCounter.ai as NeuralAI).store("strngst")

                strongest = StoredHandler.loadStored(listOf("strngst")).first() to highestCounter.score
            }
        }
    }


    /*
    probably not as accurate, as we just look if they made the same moves as Length/MontheCarlo AIs
    and in many instances multiple different moves are just as valid
     */
    @Test
    fun evaluateLossEvolve() {
        val evaluationPlayers =
            listOf({ BiasedRandomAI() }, { BalancedLengthAI(true, random.nextLong()) }, { SimpleLengthAI(false, random.nextLong()) }, { BalancedMonteCarloAI(1000, random.nextLong()) })

        evaluateAndTrain("e", trainingPlayers, Evolve(15)) { battleHandler ->
            val evalMoves = getTrainingMoves(evaluationPlayers)

            battleHandler.counters.map { counter ->
                val ai = (counter.ai as NeuralAI)
                val loss = ai.evaluate(evalMoves)
                println("$loss: ${ai.info()}")
                counter to loss
            }.sortedByDescending { it.second }.mapIndexed { n, (counter, _) ->
                counter.score = n
            }
        }
    }

    /*
        runs some games and picks all possible winning moves
        it then compares if the AI would pick the same move
     */
    @Test
    fun winningMovesChallengeNoEvolve() {
        evaluateAndTrain("winningMoves", trainingPlayers, PurgeWeakest(5), includeHighestInTraining = 1) { battleHandler ->
            val winningMoves = createWinningMoves(BalancedLengthAI(true, random.nextLong()), BiasedRandomAI()) + createWinningMoves(
                BalancedLengthAI(true, random.nextLong()), SimpleLengthAI(false, random.nextLong())
            ) + createWinningMoves(SimpleLengthAI(true, random.nextLong()), BalancedMonteCarloAI(200, random.nextLong()))
            battleHandler.counters.forEach { counter ->
                val ai = counter.ai
                var aiScore = 0

                winningMoves.forEach { (move, player) ->
                    val game = Connect4Game(move.field, player)

                    if (ai.nextMove(game) == move.move) {
                        aiScore++
                    }
                }

                counter.maxScore = winningMoves.size
                counter.score = aiScore
            }
        }
    }


    /*
    similar to winningMovesEvolve, but with handcrafted challenges with multiple correct answers
    problem could be that answers aren't as correct as I expect them to be. E.g:
      B
      B
     AA
    here I'm expecting 0 or 3, but 4 could also be correct and probably smarter in some cases
     */
    @Test
    fun challengeEvolve() {
        evaluateAndTrain("c", trainingPlayers, Evolve(15)) { battleHandler ->
            battleHandler.counters.forEach { counter ->
                val ai = counter.ai
                var aiScore = 0

                NeuralAiChallengeBuilder.completeChallenge().forEach { challenge ->
                    if (evaluate(ai, challenge)) {
                        aiScore++
                    }
                }

                counter.score = aiScore
            }
        }
    }

    /*
    faster than regular battleEvolve, but could be garbage in garbage out
    also, all training comes from battling against each other
     */
    @Test
    fun playAgainstSelfEvolve() {
        evaluateAndTrain("self", emptyList(), Evolve(20), includeHighestInTraining = 7) { battleHandler ->
            runBlocking {
                battleHandler.counters.map { counter ->
                    CoroutineScope(Dispatchers.Default).async {
                        //battleHandler.battle(battleHandler.counters.map { it.ai })
                    }
                }.awaitAll()
            }
        }
    }

    /*
    similar to regular battleEvolve, and less garbage in than pure playAgainstSelf
     */
    @Test
    fun playAgainstSelfMixedEvolve() {
        val battlePlayers = listOf(
            Triple({ SimpleLengthAI(false, random.nextLong()) }, 1, 50),
            Triple({ BalancedLengthAI(true, random.nextLong()) }, 2, 50),
            Triple({ BalancedMonteCarloAI(800, random.nextLong()) }, 100, 5)
        )

        evaluateAndTrain(
            "mixed",
            listOf({ SimpleLengthAI(false, random.nextLong()) }, { BalancedMonteCarloAI(800, random.nextLong()) }, { MaximizeWinsMonteCarloAI(500, random.nextLong()) }, { BalancedLengthAI(true, random.nextLong()) }),
            Evolve(15),
            includeHighestInTraining = 3
        ) { battleHandler ->

            runBlocking {
                battleHandler.counters.map { counter ->
                    CoroutineScope(Dispatchers.Default).async {
                        //battleHandler.battle(battleHandler.counters.map { it.ai })
                        battleHandler.battleScored(battlePlayers, counter)
                    }
                }.awaitAll()
            }
        }
    }

    @Test
    fun playAgainstStoredNoEvolve() {
        val storedHandler = StoredHandler()
        storedHandler.loadStoredNeurals()

        fun battlePlayers() = listOf(
            Triple({ SimpleLengthAI(false, random.nextLong()) }, 1, 30),
            Triple({ BalancedLengthAI(false, random.nextLong()) }, 1, 30),
            Triple({ BalancedLengthAI(true, random.nextLong()) }, 1, 30),
            Triple({ BalancedMonteCarloAI(500, random.nextLong()) }, 6, 5),
            Triple({ BalancedMonteCarloAI(800, random.nextLong()) }, 10, 3)
        ) + storedHandler.allNeurals().shuffled().take(2).map { Triple({it}, 30, 1) }

        evaluateAndTrain(
            "mixedStored",
            battlePlayers().map { it.first },
            PurgeWeakest(8),
            includeHighestInTraining = 2
        ) { battleHandler ->

            runBlocking {
                battleHandler.counters.map { counter ->
                    CoroutineScope(Dispatchers.Default).async {
                        battleHandler.battleScored(battlePlayers(), counter)
                    }
                }.awaitAll()
            }
        }
    }
}
