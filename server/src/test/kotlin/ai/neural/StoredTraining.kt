package ai.neural

import connect4.ai.AI
import connect4.ai.BattleHandler
import connect4.ai.length.BalancedLengthAI
import connect4.ai.length.SimpleLengthAI
import connect4.ai.monte.BalancedMonteCarloAI
import connect4.ai.neural.*
import connect4.ai.simple.BiasedRandomAI
import connect4.game.Connect4Game
import connect4.game.Player
import kotlinx.coroutines.*
import org.junit.Test
import java.time.Instant
import kotlin.random.Random

private fun getTrainingMovesParallel(trainingPlayers: List<() -> AI>, parallelism: Int = 5) = runBlocking {
    println("gettingTrainingMoves")
    val moves = (0..parallelism).map {
        CoroutineScope(Dispatchers.Default).async {
            getTrainingMoves(trainingPlayers.shuffled())
        }
    }.awaitAll()

    moves
}.flatten()

private infix fun MutableMap<NeuralAI, MutableList<Double>>.put(scores: List<Pair<NeuralAI, Double>>) {
    scores.forEach { (ai, score) ->
        this[ai]?.add(score) ?: run {
            this[ai] = mutableListOf(score)
        }
    }
}

/*
    Use this to load StoredNeurals and train them some more
 */
class StoredTraining {
    private val random = Random(Instant.now().toEpochMilli())

    private val trainingPlayers = listOf(
        { SimpleLengthAI(false, random.nextLong()) },
        { BalancedLengthAI(false, random.nextLong()) },
        { SimpleLengthAI(true, random.nextLong()) },
        { BalancedLengthAI(true, random.nextLong()) },
        { BalancedMonteCarloAI(1000, random.nextLong()) })

    private fun evaluateGroup(groupName: String,
                              neurals: List<RandomNeuralAI>,
                              winningMoves: List<Pair<Move, Player>>,
                              iteration: Int,
                              keep: Int,
                              remove: (AI) -> Unit
    ): List<NeuralAI> {
        val scores = mutableMapOf<NeuralAI, MutableList<Double>>()
        println(groupName)

        scores put runEvalution("WinningMoves", neurals) { evaluateWinningMoves(winningMoves, it) }
        scores put runEvalution("BalancedPly", neurals) {
            evaluateBattle(Triple({ BalancedLengthAI(true, random.nextLong()) }, 1, 100))(it)
        }
        scores put runEvalution("SimplePly", neurals) {
            evaluateBattle(Triple({ SimpleLengthAI(true, random.nextLong()) }, 1, 100))(it)
        }
        scores put runEvalution("Monte500", neurals) {
            evaluateBattle(Triple({ BalancedMonteCarloAI(500, random.nextLong()) }, 1, 5))(it)
        }

        val averageScores = scores.toList().sortedByDescending { it.second.average() }

        if(neurals.size > keep) {
            averageScores.lastOrNull()?.let { (ai, _) -> remove(ai) }
        }

        var c = 1
        return averageScores.take(1).map { (ai, score) ->
            val toStore = ai as RandomNeuralAI // TODO move to evaluateAndTrain
            println("highestRanking: ${score.average()} $iteration-${c} = ${toStore.info()}")
            toStore.store("$groupName-$iteration-${c++}")
            ai
        }
    }

    @Test
    fun evaluateAndTrain() {
        val keep = 5
        val storedHandler = StoredHandler()
        storedHandler.loadStoredNeurals()

        val grouped = storedHandler.allNeurals().map {
            it.toRandomNeural()
        }.groupBy { it.shortInfo() }.mapValues { (_, value) ->
            value.toMutableList()
        }

        val initalTrainingMoves = getTrainingMovesParallel(
            trainingPlayers + trainingPlayers
        )

        NeuralTrainer.train(grouped.values.flatten(), initalTrainingMoves.shuffled())

        repeat(100) { i ->
            println("createWinningMoves")
            val winningMoves: List<Pair<Move, Player>> =
                createWinningMoves(BalancedLengthAI(true, random.nextLong()), BiasedRandomAI()) +
                        createWinningMoves(BalancedLengthAI(true, random.nextLong()), SimpleLengthAI(false, random.nextLong())) +
                        createWinningMoves(BalancedLengthAI(true, random.nextLong()), BalancedMonteCarloAI(500, random.nextLong()))

            val highest = grouped.flatMap { (key, value) ->
                evaluateGroup(key, value.toList(), winningMoves, i, keep) { ai ->
                    value.remove(ai)
                }
            }

            println("start training")

            val trainingMoves = getTrainingMovesParallel(
                trainingPlayers + highest.shuffled().take(2).map { { it } }
            )

            println(trainingMoves.size)
            NeuralTrainer.train(grouped.values.flatten(), trainingMoves.shuffled())
        }
    }

    private fun runEvalution(
        name: String,
        neurals: List<RandomNeuralAI>,
        evaluation: (BattleHandler) -> Unit
    ): List<Pair<NeuralAI, Double>> {
        val scores = mutableSetOf<Pair<NeuralAI, Double>>()
        val battleHandler = BattleHandler(neurals)

        println(name)
        evaluation(battleHandler)
        battleHandler.currentScore(true, false)
        battleHandler.counters.map { c ->
            val newScore = c.score.toDouble() / c.maxScore.toDouble()
            scores += c.ai as NeuralAI to newScore
        }

        return scores.toList()
    }

    // TODO type for this Triple
    private fun evaluateBattle(opponent: Triple<() -> AI, Int, Int>): (BattleHandler) -> Unit = { battleHandler ->
        val chunks = battleHandler.counters.shuffled().chunked(5)

        runBlocking {
            chunks.map { chunk ->
                CoroutineScope(Dispatchers.Default).async {
                    chunk.forEach {
                        battleHandler.battleScored(listOf(opponent), it)
                    }
                }
            }.awaitAll()
        }
    }

    private fun evaluateWinningMoves(winningMoves: List<Pair<Move, Player>>, battleHandler: BattleHandler) {
        val chunks = battleHandler.counters.shuffled().chunked(5)

        runBlocking {
            chunks.map { chunk ->
                CoroutineScope(Dispatchers.Default).async {
                    chunk.forEach { counter ->
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
            }.awaitAll()
        }
    }
}
