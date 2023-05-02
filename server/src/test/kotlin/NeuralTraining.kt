import connect4.ai.AI
import connect4.ai.BattleHandler
import connect4.ai.length.*
import connect4.ai.monte.BalancedMonteCarloAI
import connect4.ai.monte.MaximizeWinsMonteCarloAI
import connect4.ai.monte.MinimizeLossesMonteCarloAI
import connect4.ai.neural.*
import connect4.ai.simple.BiasedRandomAI
import kotlinx.coroutines.*
import org.jetbrains.kotlinx.dl.api.core.activation.Activations
import org.jetbrains.kotlinx.dl.api.core.initializer.Constant
import org.jetbrains.kotlinx.dl.api.core.initializer.GlorotNormal
import org.jetbrains.kotlinx.dl.api.core.initializer.RandomNormal
import org.jetbrains.kotlinx.dl.api.core.loss.Losses
import org.jetbrains.kotlinx.dl.api.core.metric.Metrics
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
        { PlyLengthAI() },
        { PlyLengthAI() },
        { SimpleLengthAI() },
        { BalancedLengthAI() },
        { MinimizeLossesMonteCarloAI(500) },
        { MaximizeWinsMonteCarloAI(500) },
        { BalancedMonteCarloAI(800) }
    )

    private fun evaluateAndTrain(
        prefix: String,
        handler: EvolutionHandler,
        newTrainingPlayers: List<() -> AI>,
        evaluate: (List<NeuralCounter>) -> Unit
    ) {
        println("start")

        //handler.initPredefinedNeurals("basic") { random -> PredefinedNeurals.basic(initialMoves, random) }
        //handler.initPredefinedNeurals("complex") { random -> PredefinedNeurals.complex(initialMoves, random) }
        //handler.initWithRandom(30, initialMoves + initialMoves)

        handler.initPredefinedNeurals("predefined") { random ->
            listOf(
                RandomNeuralAI(
                    training = emptyList(),
                    inputType = false,
                    batchNorm = true,
                    conv = (0..1).map {
                        NeuralAIFactory.conv2D(
                            64,
                            4,
                            Activations.Mish,
                            GlorotNormal(random.nextLong()),
                            RandomNormal(0.0f, 0.2f)
                        )
                    },
                    dense = listOf(
                        NeuralAIFactory.dense(
                            300,
                            Activations.LiSHT,
                            GlorotNormal(random.nextLong()),
                            RandomNormal(0.0f, 0.2f)
                        )
                    ),
                    output = NeuralAIFactory.dense(
                        7,
                        Activations.Linear,
                        GlorotNormal(random.nextLong()),
                        Constant(0.5f)
                    ),
                    losses = Losses.SOFT_MAX_CROSS_ENTROPY_WITH_LOGITS,
                    metrics = Metrics.ACCURACY
                ),
                RandomNeuralAI(
                    training = emptyList(),
                    inputType = false,
                    batchNorm = true,
                    conv = (0..1).map {
                        NeuralAIFactory.conv2D(
                            64,
                            4,
                            Activations.Mish,
                            GlorotNormal(random.nextLong()),
                            RandomNormal(0.0f, 0.2f)
                        )
                    },
                    dense = listOf(
                        NeuralAIFactory.dense(
                            300,
                            Activations.Mish,
                            GlorotNormal(random.nextLong()),
                            RandomNormal(0.0f, 0.2f)
                        ),
                        NeuralAIFactory.dense(
                            120,
                            Activations.Mish,
                            GlorotNormal(random.nextLong()),
                            RandomNormal(0.0f, 0.2f)
                        )
                    ),
                    output = NeuralAIFactory.dense(
                        7,
                        Activations.Linear,
                        GlorotNormal(random.nextLong()),
                        Constant(0.5f)
                    ),
                    losses = Losses.SOFT_MAX_CROSS_ENTROPY_WITH_LOGITS,
                    metrics = Metrics.ACCURACY
                ),
                RandomNeuralAI(
                    training = emptyList(),
                    inputType = false,
                    batchNorm = false,
                    conv = listOf(
                        NeuralAIFactory.conv2D(
                            64,
                            4,
                            Activations.Mish,
                            GlorotNormal(random.nextLong()),
                            RandomNormal(0.0f, 0.2f)
                        ),
                        NeuralAIFactory.conv2D(
                            64,
                            3,
                            Activations.Mish,
                            GlorotNormal(random.nextLong()),
                            RandomNormal(0.0f, 0.2f)
                        )
                    ),
                    dense = listOf(
                        NeuralAIFactory.dense(
                            300,
                            Activations.LiSHT,
                            GlorotNormal(random.nextLong()),
                            RandomNormal(0.0f, 0.2f)
                        )
                    ),
                    output = NeuralAIFactory.dense(
                        7,
                        Activations.Linear,
                        GlorotNormal(random.nextLong()),
                        Constant(0.5f)
                    ),
                    losses = Losses.SOFT_MAX_CROSS_ENTROPY_WITH_LOGITS,
                    metrics = Metrics.ACCURACY
                ),
                RandomNeuralAI(
                    training = emptyList(),
                    inputType = false,
                    batchNorm = false,
                    conv = listOf(
                        NeuralAIFactory.conv2D(
                            64,
                            4,
                            Activations.Mish,
                            GlorotNormal(random.nextLong()),
                            RandomNormal(0.0f, 0.2f)
                        ),
                        NeuralAIFactory.conv2D(
                            64,
                            3,
                            Activations.Mish,
                            GlorotNormal(random.nextLong()),
                            RandomNormal(0.0f, 0.2f)
                        )
                    ),
                    dense = listOf(
                        NeuralAIFactory.dense(
                            300,
                            Activations.Mish,
                            GlorotNormal(random.nextLong()),
                            RandomNormal(0.0f, 0.2f)
                        ),
                        NeuralAIFactory.dense(
                            120,
                            Activations.Mish,
                            GlorotNormal(random.nextLong()),
                            RandomNormal(0.0f, 0.2f)
                        )
                    ),
                    output = NeuralAIFactory.dense(
                        7,
                        Activations.Linear,
                        GlorotNormal(random.nextLong()),
                        Constant(0.5f)
                    ),
                    losses = Losses.SOFT_MAX_CROSS_ENTROPY_WITH_LOGITS,
                    metrics = Metrics.ACCURACY
                ),
            )
        }

        println("gettingTrainingMoves")

        val initialMoves = runBlocking {
            (0..10).map {
                CoroutineScope(Dispatchers.Default).async {
                    getTrainingMoves(trainingPlayers)
                }
            }.awaitAll()
        }.flatten()

        println("gotTrainingMoves: ${initialMoves.size}")
        println("start initial training")
        handler.train(handler.allNeurals(), initialMoves)
        println("initial training done")

        var c = 0

        repeat(100) { i ->
            println("outerLoop $i")
            evaluate(handler.allNeurals())
            handler.currentScore(true, true)
            handler.purge(15)

            handler.highestRanking(3).forEach { counter ->
                println("highestRanking: $prefix${c} = ${counter.ai.info()}")
                counter.ai.store("$prefix${c++}")
            }

            val newMoves = runBlocking {
                (0..10).map {
                    CoroutineScope(Dispatchers.Default).async {
                        getTrainingMoves(newTrainingPlayers)
                    }
                }.awaitAll()
            }.flatten()

            println("softEvolve")
            handler.highestRanking(1).forEach { counter ->
                handler.softEvolve(counter.ai, newMoves)
            }

            println("start training")
            handler.train(handler.allNeurals(), newMoves)

            handler.resetBattles()
        }
    }

    // slowest, but most accurate
    @Test
    fun battleEvolve() {
        val handler = EvolutionHandler()
        var strongest: Pair<StoredNeuralAI?, Int> = null to 0

        val battlePlayers = listOf(
            Triple({ BiasedRandomAI() }, 1, 10),
            Triple({ DumbLengthAI() }, 1, 20),
            Triple({ BalancedLengthAI() }, 3, 100),
            Triple({ SimpleLengthAI() }, 3, 100),
            Triple({ PlyLengthAI() }, 6, 100),
            Triple({ BalancedMonteCarloAI(500) }, 50, 10)
        )

        println("start")

        evaluateAndTrain(
            "b", handler,
            (trainingPlayers + strongest.first?.let { { it } }).filterNotNull()
        ) { allNeurals ->
            runBlocking {
                allNeurals.map {
                    CoroutineScope(Dispatchers.Default).async {
                        handler.battleScored(battlePlayers, it)
                    }
                }.awaitAll()
            }
            handler.highestRanking(1).forEach { counter ->
                if (counter.gamesWon > strongest.second && counter.gamesWon > 800) {
                    (counter.ai as NeuralAI).store("strngst")
                    val storedHandler = StoredHandler()
                    storedHandler.loadStored(listOf("strngst"))
                    strongest = storedHandler.allNeurals().first() to counter.gamesWon
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
            { PlyLengthAI() },
            { SimpleLengthAI() },
            { AggressiveLengthAI() },
            { DefensiveLengthAI() },
            { BalancedMonteCarloAI(500) }
        )

        evaluateAndTrain("e", handler, trainingPlayers)
        { allNeurals ->
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

        evaluateAndTrain("c", handler, trainingPlayers) { allNeurals ->
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

        evaluateAndTrain("s", handler, handler.highestRanking(7).map { { it.ai } }) { allNeurals ->
            runBlocking {
                allNeurals.map { counter ->
                    CoroutineScope(Dispatchers.Default).async {
                        handler.battle(allNeurals.map { { it.ai } }, counter)
                    }
                }.awaitAll()
            }
        }
    }

    // faster than regular battleEvolve, and less garbage in than pure playAgainstSelf
    @Test
    fun playAgainstSelfMixedEvolve() {
        val handler = EvolutionHandler()
        var strongest: StoredNeuralAI? = null

        evaluateAndTrain("sm", handler, (handler.highestRanking(4)
            .map { it.ai } + PlyLengthAI() + BalancedMonteCarloAI(800) + strongest).filterNotNull()
            .map { { it } }) { allNeurals ->

            runBlocking {
                allNeurals.map { counter ->
                    CoroutineScope(Dispatchers.Default).async {
                        handler.battle(allNeurals.map { { it.ai } }, counter)
                    }
                }.awaitAll()
            }
            handler.highestRanking(1).forEach { counter ->
                val maybeNewStrongest = strongest?.let { strongest ->
                    val battleHandler = BattleHandler(listOf(strongest, counter.ai))
                    battleHandler.battle(
                        listOf(
                            SimpleLengthAI(),
                            DefensiveLengthAI(),
                            AggressiveLengthAI(),
                            BalancedMonteCarloAI(300),
                            BalancedMonteCarloAI(400),
                            BalancedMonteCarloAI(500)
                        )
                    )
                    battleHandler.currentScore(true, false)
                    battleHandler.highestRanking(1).first()
                } ?: run {
                    counter.ai
                }

                if (maybeNewStrongest != strongest) {
                    (maybeNewStrongest as NeuralAI).store("strongest")
                    val storedHandler = StoredHandler()
                    storedHandler.loadStored(listOf("strongest"))
                    strongest = storedHandler.allNeurals().first()
                }
            }
        }
    }
}