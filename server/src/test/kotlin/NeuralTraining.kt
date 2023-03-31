import connect4.ai.AI
import connect4.ai.length.*
import connect4.ai.monte.BalancedMonteCarloAI
import connect4.ai.monte.MaximizeWinsMonteCarloAI
import connect4.ai.monte.MinimizeLossesMonteCarloAI
import connect4.ai.neural.EvolutionHandler
import connect4.ai.neural.NeuralCounter
import connect4.ai.neural.PredefinedNeurals
import connect4.ai.neural.getTrainingMoves
import org.junit.Test

/*
    Use these to train neurals in various ways
    use neuralFilteredStrengthTest or neuralChallenge to evaluate them later

    after some time neurals might get overfitted
    so after each iteration we store the highest and delete the weakest
    softEvolve creates copies of the strongest with new random activation functions
 */
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

    private fun evaluateAndTrain(
        prefix: String,
        handler: EvolutionHandler,
        newTrainingPlayers: () -> List<AI>,
        evaluate: (List<NeuralCounter>) -> Unit
    ) {
        val initialMoves = getTrainingMoves(trainingPlayers)
        handler.initPredefinedNeurals("basic") { random -> PredefinedNeurals.basic(initialMoves, random) }
        handler.initPredefinedNeurals("complex") { random -> PredefinedNeurals.complex(initialMoves, random) }
        handler.initWithRandom(10, initialMoves)

        var c = 0

        repeat(100) { i ->
            println("outerLoop $i")
            evaluate(handler.allNeurals())
            handler.currentScore(false, true)
            handler.purge()

            val newMoves = getTrainingMoves(newTrainingPlayers())

            handler.highestRanking(1).forEach { counter ->
                handler.evolve(counter.ai, newMoves)
            }

            handler.highestRanking(2).forEach { counter ->
                println("highestRanking: $prefix${c} = ${counter.ai.info()}")
                counter.ai.store("$prefix${c++}")
            }

            handler.initWithRandom(2, newMoves)

            handler.resetBattles()
            println("start training")
            handler.train(handler.allNeurals(), newMoves)
        }
    }

    // slowest, but most accurate
    @Test
    fun battleEvolve() {
        val handler = EvolutionHandler()

        // TODO start out with weaker players and get stronger as Neurals do?
        val battlePlayers = listOf(
            { BalancedLengthAI() },
            { SimpleLengthAI() },
            { DefensiveLengthAI() },
            { AggressiveLengthAI() },
            { PlyLengthAI() },
            { BalancedMonteCarloAI(500) }
        )

        evaluateAndTrain("b", handler, { trainingPlayers }) { allNeurals ->
            repeat(10) { j ->
                println(j)
                repeat(allNeurals.size) {
                    handler.battle(battlePlayers)
                }
            }
        }
    }

    // probably not as accurate, as we just look if they made the same moves
    // but in many instances multiple different moves are just as valid
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

        evaluateAndTrain("e", handler, { trainingPlayers }) { allNeurals ->
            val evalMoves = getTrainingMoves(evaluationPlayers)

            allNeurals.map { counter ->
                val loss = counter.ai.evaluate(evalMoves)
                println("$loss: ${counter.ai.info()}")
                counter to loss
            }.sortedByDescending { it.second }.mapIndexed { n, (counter, _) ->
                counter.gamesWon = n
            }
        }
    }

    // similar to evaluateEvolve, but with handcrafted challenges with multiple correct answers
    // problem could be that answers aren't as correct as I expect them to be. E.g:
    //   B
    //   B
    //  AA
    // here I'm expecting 0 or 3, but 4 could also be correct and probably smarter
    @Test
    fun challengeEvolve() {
        val handler = EvolutionHandler()

        evaluateAndTrain("c", handler, { trainingPlayers }) { allNeurals ->
            allNeurals.forEach { neuralCounter ->
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
        }
    }

    // faster than regular battleEvolve, but could be garbage in garbage out
    @Test
    fun playAgainstSelfEvolve() {
        val handler = EvolutionHandler()

        evaluateAndTrain("s", handler, {
            handler.highestRanking(7).map { it.ai }
        }) { allNeurals -> // fighting 10 times not needed, as they'll always do the same
            allNeurals.forEach { counter ->
                handler.battle(allNeurals.map { { it.ai } }, counter)
            }
        }
    }

    // faster than regular battleEvolve, and less garbage in than pure playAgainstSelf
    @Test
    fun playAgainstSelfMixedEvolve() {
        val handler = EvolutionHandler()

        val trainingPlayers: List<AI> = listOf(
            BalancedLengthAI(),
            PlyLengthAI()
        )

        evaluateAndTrain("m", handler, {
            trainingPlayers + handler.highestRanking(5).map { it.ai }
        }) { allNeurals ->
            allNeurals.forEach { counter ->
                handler.battle(allNeurals.map { { it.ai } }, counter)
            }
        }
    }
}