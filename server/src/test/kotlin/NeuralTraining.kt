import connect4.ai.AI
import connect4.ai.length.*
import connect4.ai.monte.BalancedMonteCarloAI
import connect4.ai.monte.MaximizeWinsMonteCarloAI
import connect4.ai.monte.MinimizeLossesMonteCarloAI
import connect4.ai.neural.EvolutionHandler
import connect4.ai.neural.getTrainingMoves
import org.junit.Test
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

// Use these to train neurals in various ways
// use neuralTest to evaluate them later
@OptIn(ExperimentalTime::class)
class NeuralTraining {
    private val trainingPlayers = listOf(
        BalancedLengthAI(),
        PlyLengthAI(),
        SimpleLengthAI(),
        AggressiveLengthAI(),
        DefensiveLengthAI(),
        MinimizeLossesMonteCarloAI(500),
        MaximizeWinsMonteCarloAI(500),
        BalancedMonteCarloAI(800)
    )

    private val battlePlayers = listOf(
        { BalancedLengthAI() },
        { DefensiveLengthAI() },
        { AggressiveLengthAI() },
        { MinimizeLossesMonteCarloAI(500) },
        { MaximizeWinsMonteCarloAI(500) },
        { BalancedMonteCarloAI(500) }
    )

    // TODO lots of repetitive code

    @Test
    fun battleEvolve() {
        val handler = EvolutionHandler()

        val initialMoves = getTrainingMoves(trainingPlayers)
        handler.initWithBasicNeurals(initialMoves)
        handler.initWithComplexNeurals(initialMoves)
        handler.initWithRandom(10)

        var c = 0

        repeat(100) { i ->
            println("outerLoop $i")
            repeat(10) { j ->
                println(j)
                repeat(handler.allNeurals().size) {
                    handler.battle(battlePlayers)
                }
            }
            handler.currentScore(true, true)
            handler.purge()

            val newMoves = getTrainingMoves(trainingPlayers)
            handler.highestRanking(2).forEach { counter ->
                println("highestRanking: b${c} = ${counter.ai.info()}")
                counter.ai.store("b${c++}")
                handler.softEvolve(counter.ai, newMoves)
            }

            handler.resetBattles()
            println("start training")
            println(measureTime {
                handler.train(handler.allNeurals(), newMoves)
            })
        }
    }

    @Test
    fun evaluateEvolve() {
        val handler = EvolutionHandler()

        val evaluationPlayers = listOf(
            PlyLengthAI(),
            SimpleLengthAI(),
            AggressiveLengthAI(),
            DefensiveLengthAI(),
            BalancedMonteCarloAI(500)
        )

        val initialMoves = getTrainingMoves(trainingPlayers)
        handler.initWithBasicNeurals(initialMoves)
        handler.initWithComplexNeurals(initialMoves)
        handler.initWithRandom(10)

        var c = 0

        repeat(100) { i ->
            println("outerLoop $i")

            val evalMoves = getTrainingMoves(evaluationPlayers)

            val evalResults = handler.allNeurals().map { counter ->
                val loss = counter.ai.evaluate(evalMoves)
                println("$loss: ${counter.ai.info()}")
                counter to loss
            }.sortedBy { it.second }

            evalResults.sortedByDescending { it.second }.mapIndexed { n, (counter, _) ->
                counter.gamesWon = n
            }

            handler.purge()

            val trainingMoves = getTrainingMoves(trainingPlayers)

            evalResults.take(3).forEach { (counter, loss) ->
                println("lowestLoss: e${c} = $loss: ${counter.ai.info()}")
                counter.ai.store("e${c++}")
                handler.softEvolve(counter.ai, trainingMoves)
            }

            handler.resetBattles()
            println("start training")
            println(measureTime {
                handler.train(handler.allNeurals(), trainingMoves)
            })
        }
    }

    @Test
    fun playAgainstSelfEvolve() {
        val handler = EvolutionHandler()

        val initialMoves = getTrainingMoves(trainingPlayers + trainingPlayers)
        handler.initWithBasicNeurals(initialMoves)
        handler.initWithComplexNeurals(initialMoves)
        handler.initWithRandom(10)

        var c = 0

        repeat(100) { i ->
            println("outerLoop $i")

            handler.allNeurals().forEach { counter ->
                handler.battle(handler.allNeurals().map { { it.ai } }, counter)
            }
            handler.currentScore(true, true)

            handler.purge(30)

            val list: List<AI> = handler.highestRanking(8).map { it.ai }
            val moves = getTrainingMoves(list)

            handler.highestRanking(2).forEach { counter ->
                println("highestRanking: s${c} = ${counter.ai.info()}")
                counter.ai.store("s${c++}")
                handler.softEvolve(counter.ai, moves)
            }

            handler.resetBattles()
            println("start training")
            println(measureTime {
                handler.train(handler.allNeurals(), moves)
            })
        }
    }

    @Test
    fun challengeEvolve() {
        val handler = EvolutionHandler()

        val initialMoves = getTrainingMoves(trainingPlayers)
        handler.initWithBasicNeurals(initialMoves)
        handler.initWithComplexNeurals(initialMoves)
        handler.initWithRandom(10)

        var c = 0

        repeat(100) { i ->
            println(i)

            handler.allNeurals().forEach { neuralCounter ->
                val ai = neuralCounter.ai
                var aiScore = 0

                neuralCounter.gamesWon = 0

                NeuralAiChallengeBuilder.completeChallenge().forEach { challenge ->
                    if (evaluate(ai, challenge)) {
                        aiScore++
                    }
                }

                neuralCounter.gamesWon = aiScore
            }

            handler.currentScore(false, true)
            handler.purge()

            val moves = getTrainingMoves(trainingPlayers)

            handler.highestRanking(2).forEach { counter ->
                println("highestRanking: c${c} = ${counter.ai.info()}")
                counter.ai.store("c${c++}")
                handler.softEvolve(counter.ai, moves)
            }
            handler.resetBattles()
            handler.train(handler.allNeurals(), moves)
        }
    }
}