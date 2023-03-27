package connect4.ai.neural

import connect4.ai.AI
import connect4.ai.AIs
import connect4.ai.length.BalancedLengthAI
import connect4.ai.length.PlyLengthAI
import connect4.ai.length.SimpleLengthAI
import connect4.ai.monte.BalancedMonteCarloAI
import connect4.ai.monte.MaximizeWinsMonteCarloAI
import connect4.ai.monte.MinimizeLossesMonteCarloAI
import connect4.game.Connect4Game
import connect4.game.Player
import org.jetbrains.kotlinx.dl.api.core.activation.Activations
import org.jetbrains.kotlinx.dl.api.core.initializer.Constant
import org.jetbrains.kotlinx.dl.api.core.initializer.GlorotNormal
import org.jetbrains.kotlinx.dl.api.core.initializer.RandomNormal
import org.jetbrains.kotlinx.dl.api.core.initializer.Zeros
import org.jetbrains.kotlinx.dl.api.core.layer.convolutional.ConvPadding
import org.jetbrains.kotlinx.dl.api.core.loss.Losses
import org.jetbrains.kotlinx.dl.api.core.metric.Metrics
import java.time.Instant
import kotlin.random.Random

fun List<Move>.repeatLastMoves(): List<Move> {
    return if(size >= 2) {
        this + takeLast(size / 2).repeatLastMoves()
    } else {
        this
    }
}

fun getTrainingMoves(players: List<AI>): List<Move> {
    val winningMoves = players.map { p1 ->
        players.mapNotNull { p2 ->
            p1.reset()
            p2.reset()
            val result = Connect4Game.runGame(p1, p2)
            if (result.first != null) {
                result.second to result.first
            } else {
                null
            }
        }
    }.flatten()

    return winningMoves.map { (moves, player) ->
        val game = Connect4Game()

        moves.map { m ->
            val move = Move(game.field.map { it.toList() }, m)

            /*
            val allWMoves = game.availableColumns.map { column ->
                val maybeWinningGame = Connect4Game(game.field, game.currentPlayer)
                maybeWinningGame.makeMove(column)
                if (maybeWinningGame.hasFinished()) {
                    Move(game.field.map { it.toList() }, column)
                } else {
                    null
                }
            } + if (game.currentPlayer == player) {
                move
            } else {
                null
            }

             */

            val playerMoves = if (game.currentPlayer == player) {
                listOf(move)
            } else {
                emptyList()
            }

            game.makeMove(m)

            playerMoves
        }.flatten().repeatLastMoves()
    }.flatten()
}

class NeuralCounter(
    val ai: RandomNeuralAI,
    var trained: Int = 0,
    var gamesPlayed: Int = 0,
    var gamesWon: Int = 0
)

class EvolutionHandler {
    private val neurals = mutableListOf<NeuralCounter>()

    private val trainingPlayers = listOf(
        { SimpleLengthAI() },
        { BalancedLengthAI() },
        { PlyLengthAI() },
        { PlyLengthAI() },
        { MaximizeWinsMonteCarloAI(500) },
        { MinimizeLossesMonteCarloAI(500) },
        { BalancedMonteCarloAI(500) }
    )

    fun allNeurals(): List<NeuralCounter> = neurals.toList()

    private fun leastTrained(): NeuralCounter =
        neurals.minBy { it.trained }

    private fun leastPlayed(): NeuralCounter =
        neurals.minBy { it.gamesPlayed }

    fun highestRanking(amount: Int): List<NeuralCounter> =
        neurals.sortedByDescending { it.gamesWon }.take(amount)

    fun train(toTrain: List<NeuralCounter> = emptyList(), trainingMoves: List<Move> = emptyList()) {
        val moves = trainingMoves.ifEmpty {
            getTrainingMoves(trainingPlayers.map { it() })
        }

        if (toTrain.isEmpty()) {
            val counter = leastTrained()
            counter.ai.train(moves)
            counter.trained++
        } else {
            toTrain.forEach { counter ->
                if (counter.ai.train(moves)) {
                    counter.trained++
                } else {
                    neurals -= counter
                }
            }
        }
    }

    private fun handleResult(counter: NeuralCounter, player: Player, winner: Player?) {
        counter.gamesPlayed++
        if (player == winner) {
            counter.gamesWon++
        }
    }

    private fun singleBattle(counter: NeuralCounter, opponent: AI, handleResult: Boolean) {
        val resultP1 = Connect4Game.runGame(counter.ai, opponent)
        if (handleResult) {
            handleResult(counter, Player.FirstPlayer, resultP1.first)
        }
        val resultP2 = Connect4Game.runGame(opponent, counter.ai)
        if (handleResult) {
            handleResult(counter, Player.SecondPlayer, resultP2.first)
        }
    }

    fun battle(
        players: List<() -> AI> = AIs.highAIs,
        ai: NeuralCounter = leastPlayed()
    ) {
        players.forEach { opponent ->
            singleBattle(ai, opponent(), true)
        }
    }

    fun softEvolve(
        toEvolve: RandomNeuralAI,
        training: List<Move> = emptyList()
    ) {
        val before = neurals.toList()

        val new = listOf(
            { toEvolve.copy(training, conv = toEvolve.convLayer.map { it.softRandomize() }) },
            { toEvolve.copy(training, dense = toEvolve.denseLayer.map { it.softRandomize() }) },
            { toEvolve.copy(training, output = toEvolve.outputLayer.softRandomize()) },
            { toEvolve.copy(training, dense = null) },
            {
                toEvolve.copy(
                    training,
                    conv = toEvolve.convLayer.map { it.softRandomize() },
                    dense = toEvolve.denseLayer.map { it.softRandomize() })
            },
            {
                toEvolve.copy(
                    training,
                    conv = toEvolve.convLayer.map { it.softRandomize() },
                    dense = toEvolve.denseLayer.map { it.softRandomize() },
                    output = toEvolve.outputLayer.softRandomize())
            },
        ).mapNotNull {
            try {
                it()
            } catch (_: Exception) {
                null
            }
        }

        new.forEach { evolved ->
            neurals += NeuralCounter(evolved)
        }

        val after = neurals.toList()

        println("Created from softEvolve:")
        after.filterNot { before.contains(it) }.forEach {
            println(it.ai.info())
        }
    }

    private fun evolve(
        first: RandomNeuralAI,
        second: RandomNeuralAI,
        training: List<Move> = emptyList()
    ): List<RandomNeuralAI> = listOf(
        { first.copy(training) },
        { first.copy(training, conv = first.convLayer.map { it.randomize() }) },
        {
            first.copy(
                training,
                conv = listOf(NeuralAIFactory.randomConv2DLayer(), NeuralAIFactory.randomConv2DLayer())
            )
        },
        {
            first.copy(
                training,
                conv = listOf(
                    NeuralAIFactory.randomConv2DLayer(),
                    NeuralAIFactory.randomConv2DLayer(),
                    NeuralAIFactory.randomConv2DLayer()
                )
            )
        },
        {
            first.copy(
                training,
                conv = listOf(
                    NeuralAIFactory.randomConv2DLayer(),
                    NeuralAIFactory.randomConv2DLayer(),
                    NeuralAIFactory.randomConv2DLayer(),
                    NeuralAIFactory.randomConv2DLayer()
                )
            )
        },
        {
            first.copy(
                training,
                conv = listOf(
                    NeuralAIFactory.randomConv2DLayer(),
                    NeuralAIFactory.randomConv2DLayer(),
                    NeuralAIFactory.randomConv2DLayer(),
                    NeuralAIFactory.randomConv2DLayer(),
                    NeuralAIFactory.randomConv2DLayer()
                )
            )
        },
        { first.copy(training, dense = first.denseLayer.map { it.randomize() }) },
        {
            first.copy(
                training,
                conv = first.convLayer.map { it.randomize() },
                dense = first.denseLayer.map { it.randomize() })
        },
        { first.copy(training, losses = NeuralAIFactory.randomLosses()) },
        { first.copy(training, metrics = NeuralAIFactory.randomMetrics()) },
        {
            first.copy(
                training,
                losses = NeuralAIFactory.randomLosses(),
                metrics = NeuralAIFactory.randomMetrics()
            )
        },
        { first.copy(training, conv = null) },
        { first.copy(training, dense = null) },
        { first.copy(training, output = null) },
        { first.copy(training, conv = null, dense = null, output = null) },
        { second.copy(training) },
        { second.copy(training, conv = null) },
        { second.copy(training, dense = null) },
        { second.copy(training, output = null) },
        { first.copy(training, conv = second.convLayer) },
        { first.copy(training, dense = second.denseLayer) }
    ).mapNotNull {
        try {
            it()
        } catch (_: Exception) {
            null
        }
    }

    fun purge(keep: Int = 30) {
        val toKeep: List<NeuralCounter> = highestRanking(keep)
        neurals.clear()
        neurals += toKeep
    }

    /*
    fun evolve(take: Int = 3) {
        val highest: List<NeuralCounter> = (highestRanking(5) + strongest).filterNotNull()
        val evolved: List<NeuralCounter> =
            (listOf(strongest) + highestRanking(3)).asSequence().filterNotNull()
                .map { it.ai to it.epochs }.take(take).zipWithNext { a, b ->
                    evolve(
                        a.first,
                        b.first
                    ) to a.second
                }.map { (l, c) -> l.map { NeuralCounter(it, epochs = c) } }.flatten().toList()

        neurals.clear()
        highest.forEach { counter ->
            println("${counter.gamesWon} : ${counter.ai.name}")
            neurals += NeuralCounter(counter.ai, epochs = counter.epochs)
        }
        evolved.forEach { e ->
            println("New evolution: ${e.ai.info()}")
            neurals += e
        }
    }

     */

    fun resetBattles() {
        neurals.forEach {
            it.gamesWon = 0
            it.gamesPlayed = 0
        }
    }

    fun currentScore(printAll: Boolean = true, printHighest: Boolean = true) {
        if (printAll) {
            println("Score")
            neurals.forEach {
                println("${it.gamesWon}/${it.gamesPlayed}: ${it.ai.info()}")
            }
        }
        if (printHighest) {
            println("Highest:")
            neurals.maxBy { it.gamesWon }.let {
                println("${it.gamesWon}/${it.gamesPlayed}: ${it.ai.info()}")
            }
        }
    }

    fun initWithBasicNeurals(trainingMoves: List<Move> = emptyList()) {
        val before = neurals.toList()

        val moves = trainingMoves.ifEmpty {
            getTrainingMoves(trainingPlayers.map { it() })
        }
        val random = Random(0)

        neurals.addAll(
            listOf(
                RandomNeuralAI(
                    training = moves,
                    inputType = true,
                    conv = listOf(
                        NeuralAIFactory.conv2D(3, 4, Activations.LiSHT, GlorotNormal(random.nextLong()), Zeros())
                    ),
                    dense = listOf(
                        NeuralAIFactory.dense(
                            28,
                            Activations.HardSigmoid,
                            GlorotNormal(random.nextLong()),
                            Constant(0.5f)
                        ),
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
                    training = moves,
                    inputType = false,
                    conv = listOf(
                        NeuralAIFactory.conv2D(3, 4, Activations.LiSHT, GlorotNormal(random.nextLong()), Zeros())
                    ),
                    dense = listOf(
                        NeuralAIFactory.dense(
                            28,
                            Activations.HardSigmoid,
                            GlorotNormal(random.nextLong()),
                            Constant(0.5f)
                        ),
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
                    training = moves,
                    inputType = true,
                    conv = listOf(
                        NeuralAIFactory.conv2D(3, 4, Activations.LiSHT, GlorotNormal(random.nextLong()), Zeros())
                    ),
                    dense = emptyList(),
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
                    training = moves,
                    inputType = false,
                    conv = listOf(
                        NeuralAIFactory.conv2D(3, 4, Activations.LiSHT, GlorotNormal(random.nextLong()), Zeros())
                    ),
                    dense = emptyList(),
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
                    training = moves,
                    inputType = true,
                    conv = listOf(
                        NeuralAIFactory.conv2D(21, 4, Activations.LiSHT, GlorotNormal(random.nextLong()), RandomNormal(0.0f, 0.2f)),
                        NeuralAIFactory.conv2D(7, 3, Activations.LiSHT, GlorotNormal(random.nextLong()), RandomNormal(0.0f, 0.2f))
                    ),
                    dense = listOf(
                        NeuralAIFactory.dense(
                            21,
                            Activations.HardSigmoid,
                            GlorotNormal(random.nextLong()),
                            Constant(0.5f)
                        ),
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
                    training = moves,
                    inputType = false,
                    conv = listOf(
                        NeuralAIFactory.conv2D(21, 4, Activations.LiSHT, GlorotNormal(random.nextLong()), RandomNormal(0.0f, 0.2f)),
                        NeuralAIFactory.conv2D(7, 3, Activations.LiSHT, GlorotNormal(random.nextLong()), RandomNormal(0.0f, 0.2f))
                    ),
                    dense = listOf(
                        NeuralAIFactory.dense(
                            21,
                            Activations.HardSigmoid,
                            GlorotNormal(random.nextLong()),
                            Constant(0.5f)
                        ),
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
                    training = moves,
                    inputType = true,
                    conv = listOf(
                        NeuralAIFactory.conv2D(3, 4, Activations.LiSHT, GlorotNormal(random.nextLong()), Zeros())
                    ),
                    dense = listOf(
                        NeuralAIFactory.dense(
                            74,
                            Activations.HardSigmoid,
                            GlorotNormal(random.nextLong()),
                            Constant(0.5f)
                        ),
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
                    training = moves,
                    inputType = false,
                    conv = listOf(
                        NeuralAIFactory.conv2D(3, 4, Activations.LiSHT, GlorotNormal(random.nextLong()), Zeros())
                    ),
                    dense = listOf(
                        NeuralAIFactory.dense(
                            74,
                            Activations.HardSigmoid,
                            GlorotNormal(random.nextLong()),
                            Constant(0.5f)
                        ),
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
                    training = moves,
                    inputType = true,
                    conv = listOf(
                        NeuralAIFactory.conv2D(
                            32,
                            4,
                            Activations.Mish,
                            GlorotNormal(random.nextLong()),
                            RandomNormal(0.0f, 0.5f)
                        )
                    ),
                    dense = listOf(
                        NeuralAIFactory.dense(
                            294,
                            Activations.HardSigmoid,
                            GlorotNormal(random.nextLong()),
                            RandomNormal(0.0f, 0.5f)
                        )
                    ),
                    output = NeuralAIFactory.dense(
                        7,
                        Activations.Linear,
                        GlorotNormal(random.nextLong()),
                        RandomNormal(0.0f, 0.5f)
                    ),
                    losses = Losses.SOFT_MAX_CROSS_ENTROPY_WITH_LOGITS,
                    metrics = Metrics.ACCURACY
                ),
                RandomNeuralAI(
                    training = moves,
                    inputType = false,
                    conv = listOf(
                        NeuralAIFactory.conv2D(
                            32,
                            4,
                            Activations.Mish,
                            GlorotNormal(random.nextLong()),
                            RandomNormal(0.0f, 0.5f)
                        )
                    ),
                    dense = listOf(
                        NeuralAIFactory.dense(
                            294,
                            Activations.HardSigmoid,
                            GlorotNormal(random.nextLong()),
                            RandomNormal(0.0f, 0.5f)
                        )
                    ),
                    output = NeuralAIFactory.dense(
                        7,
                        Activations.Linear,
                        GlorotNormal(random.nextLong()),
                        RandomNormal(0.0f, 0.5f)
                    ),
                    losses = Losses.SOFT_MAX_CROSS_ENTROPY_WITH_LOGITS,
                    metrics = Metrics.ACCURACY
                )
            ).map { NeuralCounter(it) }
        )

        val after = neurals.toList()

        println("Initialized basic Neurals:")
        after.filterNot { before.contains(it) }.forEach {
            println(it.ai.info())
        }
    }

    fun initWithComplexNeurals(trainingMoves: List<Move> = emptyList()) {
        val before = neurals.toList()

        val moves = trainingMoves.ifEmpty {
            getTrainingMoves(trainingPlayers.map { it() })
        }
        val random = Random(0)

        neurals.addAll(
            listOf(
                RandomNeuralAI(
                    training = moves,
                    inputType = true,
                    conv = (0..1).map {
                        NeuralAIFactory.conv2D(
                            64,
                            3,
                            Activations.LiSHT,
                            GlorotNormal(random.nextLong()),
                            RandomNormal(0.0f, 0.5f)
                        )
                    } + (0..1).map {
                        NeuralAIFactory.conv2D(
                            32,
                            3,
                            Activations.LiSHT,
                            GlorotNormal(random.nextLong()),
                            RandomNormal(0.0f, 0.5f)
                        )
                    } + (0..1).map {
                        NeuralAIFactory.conv2D(
                            16,
                            3,
                            Activations.LiSHT,
                            GlorotNormal(random.nextLong()),
                            RandomNormal(0.0f, 0.5f)
                        )
                    } + NeuralAIFactory.conv2D(
                        3,
                        1,
                        Activations.LiSHT,
                        GlorotNormal(random.nextLong()),
                        RandomNormal(0.0f, 0.5f)
                    ),
                    dense = listOf(
                        NeuralAIFactory.dense(64, Activations.Linear, GlorotNormal(random.nextLong()), Constant(0.5f))
                    ),
                    output = NeuralAIFactory.dense(
                        7,
                        Activations.Relu,
                        GlorotNormal(random.nextLong()),
                        Constant(0.5f)
                    ),
                    losses = Losses.SOFT_MAX_CROSS_ENTROPY_WITH_LOGITS,
                    metrics = Metrics.ACCURACY
                ),
                RandomNeuralAI(
                    training = moves,
                    inputType = false,
                    conv = (0..1).map {
                        NeuralAIFactory.conv2D(
                            64,
                            3,
                            Activations.LiSHT,
                            GlorotNormal(random.nextLong()),
                            RandomNormal(0.0f, 0.5f)
                        )
                    } + (0..1).map {
                        NeuralAIFactory.conv2D(
                            32,
                            3,
                            Activations.LiSHT,
                            GlorotNormal(random.nextLong()),
                            RandomNormal(0.0f, 0.5f)
                        )
                    } + (0..1).map {
                        NeuralAIFactory.conv2D(
                            16,
                            3,
                            Activations.LiSHT,
                            GlorotNormal(random.nextLong()),
                            RandomNormal(0.0f, 0.5f)
                        )
                    } + NeuralAIFactory.conv2D(
                        3,
                        1,
                        Activations.LiSHT,
                        GlorotNormal(random.nextLong()),
                        RandomNormal(0.0f, 0.5f)
                    ),
                    dense = listOf(
                        NeuralAIFactory.dense(64, Activations.Linear, GlorotNormal(random.nextLong()), Constant(0.5f))
                    ),
                    output = NeuralAIFactory.dense(
                        7,
                        Activations.Relu,
                        GlorotNormal(random.nextLong()),
                        Constant(0.5f)
                    ),
                    losses = Losses.SOFT_MAX_CROSS_ENTROPY_WITH_LOGITS,
                    metrics = Metrics.ACCURACY
                ),
                RandomNeuralAI(
                    training = moves,
                    inputType = true,
                    conv = listOf(
                        NeuralAIFactory.max2D(
                            2,
                            1,
                            ConvPadding.VALID
                        ),
                        NeuralAIFactory.conv2D(
                            64,
                            2,
                            Activations.SoftSign,
                            GlorotNormal(random.nextLong()),
                            RandomNormal(0.0f, 0.5f)
                        ),
                        NeuralAIFactory.avg2D(
                            3,
                            1,
                            ConvPadding.VALID
                        )
                    ),
                    dense = listOf(
                        NeuralAIFactory.dense(
                            256,
                            Activations.Exponential,
                            GlorotNormal(random.nextLong()),
                            RandomNormal(0.0f, 0.5f)
                        ),
                        NeuralAIFactory.dense(
                            64,
                            Activations.TanhShrink,
                            GlorotNormal(random.nextLong()),
                            RandomNormal(0.0f, 0.5f)
                        ),
                        NeuralAIFactory.dense(
                            28,
                            Activations.Tanh,
                            GlorotNormal(random.nextLong()),
                            RandomNormal(0.0f, 0.5f)
                        ),
                    ),
                    output = NeuralAIFactory.dense(
                        7,
                        Activations.Relu,
                        GlorotNormal(random.nextLong()),
                        Constant(0.5f)
                    ),
                    losses = Losses.SOFT_MAX_CROSS_ENTROPY_WITH_LOGITS,
                    metrics = Metrics.ACCURACY
                ),
                RandomNeuralAI(
                    training = moves,
                    inputType = false,
                    conv = listOf(
                        NeuralAIFactory.max2D(
                            2,
                            1,
                            ConvPadding.VALID
                        ),
                        NeuralAIFactory.conv2D(
                            64,
                            2,
                            Activations.SoftSign,
                            GlorotNormal(random.nextLong()),
                            RandomNormal(0.0f, 0.5f)
                        ),
                        NeuralAIFactory.avg2D(
                            3,
                            1,
                            ConvPadding.VALID
                        )
                    ),
                    dense = listOf(
                        NeuralAIFactory.dense(
                            256,
                            Activations.Exponential,
                            GlorotNormal(random.nextLong()),
                            RandomNormal(0.0f, 0.5f)
                        ),
                        NeuralAIFactory.dense(
                            64,
                            Activations.TanhShrink,
                            GlorotNormal(random.nextLong()),
                            RandomNormal(0.0f, 0.5f)
                        ),
                        NeuralAIFactory.dense(
                            28,
                            Activations.Tanh,
                            GlorotNormal(random.nextLong()),
                            RandomNormal(0.0f, 0.5f)
                        ),
                    ),
                    output = NeuralAIFactory.dense(
                        7,
                        Activations.Relu,
                        GlorotNormal(random.nextLong()),
                        Constant(0.5f)
                    ),
                    losses = Losses.SOFT_MAX_CROSS_ENTROPY_WITH_LOGITS,
                    metrics = Metrics.ACCURACY
                ),
                RandomNeuralAI(
                    training = moves,
                    inputType = true,
                    conv = listOf(
                        NeuralAIFactory.max2D(
                            3,
                            1,
                            ConvPadding.VALID
                        ),
                        NeuralAIFactory.conv2D(
                            64,
                            3,
                            Activations.SoftSign,
                            GlorotNormal(random.nextLong()),
                            RandomNormal(0.0f, 0.5f)
                        ),
                        NeuralAIFactory.avg2D(
                            3,
                            1,
                            ConvPadding.VALID
                        )
                    ),
                    dense = listOf(
                        NeuralAIFactory.dense(
                            20,
                            Activations.Exponential,
                            GlorotNormal(random.nextLong()),
                            RandomNormal(0.0f, 0.5f)
                        ),
                    ),
                    output = NeuralAIFactory.dense(
                        7,
                        Activations.Relu,
                        GlorotNormal(random.nextLong()),
                        Constant(0.5f)
                    ),
                    losses = Losses.SOFT_MAX_CROSS_ENTROPY_WITH_LOGITS,
                    metrics = Metrics.ACCURACY
                ),
                RandomNeuralAI(
                    training = moves,
                    inputType = false,
                    conv = listOf(
                        NeuralAIFactory.max2D(
                            3,
                            1,
                            ConvPadding.VALID
                        ),
                        NeuralAIFactory.conv2D(
                            64,
                            3,
                            Activations.SoftSign,
                            GlorotNormal(random.nextLong()),
                            RandomNormal(0.0f, 0.5f)
                        ),
                        NeuralAIFactory.avg2D(
                            3,
                            1,
                            ConvPadding.VALID
                        )
                    ),
                    dense = listOf(
                        NeuralAIFactory.dense(
                            40,
                            Activations.Exponential,
                            GlorotNormal(random.nextLong()),
                            RandomNormal(0.0f, 0.5f)
                        ),
                    ),
                    output = NeuralAIFactory.dense(
                        7,
                        Activations.Relu,
                        GlorotNormal(random.nextLong()),
                        Constant(0.5f)
                    ),
                    losses = Losses.SOFT_MAX_CROSS_ENTROPY_WITH_LOGITS,
                    metrics = Metrics.ACCURACY
                ),
                RandomNeuralAI(
                    training = moves,
                    inputType = true,
                    conv = listOf(
                        NeuralAIFactory.conv2D(
                            64,
                            4,
                            Activations.Tanh,
                            GlorotNormal(random.nextLong()),
                            RandomNormal(0.0f, 0.5f)
                        ),
                        NeuralAIFactory.conv2D(
                            32,
                            3,
                            Activations.Tanh,
                            GlorotNormal(random.nextLong()),
                            RandomNormal(0.0f, 0.5f)
                        )
                    ),
                    dense = listOf(
                        NeuralAIFactory.dense(
                            64,
                            Activations.Exponential,
                            GlorotNormal(random.nextLong()),
                            RandomNormal(0.0f, 0.5f)
                        )
                    ),
                    output = NeuralAIFactory.dense(
                        7,
                        Activations.Relu,
                        GlorotNormal(random.nextLong()),
                        Constant(0.5f)
                    ),
                    losses = Losses.SOFT_MAX_CROSS_ENTROPY_WITH_LOGITS,
                    metrics = Metrics.ACCURACY
                ),
                RandomNeuralAI(
                    training = moves,
                    inputType = false,
                    conv = listOf(
                        NeuralAIFactory.conv2D(
                            64,
                            4,
                            Activations.Tanh,
                            GlorotNormal(random.nextLong()),
                            RandomNormal(0.0f, 0.5f)
                        ),
                        NeuralAIFactory.conv2D(
                            32,
                            3,
                            Activations.Tanh,
                            GlorotNormal(random.nextLong()),
                            RandomNormal(0.0f, 0.5f)
                        )
                    ),
                    dense = listOf(
                        NeuralAIFactory.dense(
                            64,
                            Activations.Exponential,
                            GlorotNormal(random.nextLong()),
                            RandomNormal(0.0f, 0.5f)
                        )
                    ),
                    output = NeuralAIFactory.dense(
                        7,
                        Activations.Relu,
                        GlorotNormal(random.nextLong()),
                        Constant(0.5f)
                    ),
                    losses = Losses.SOFT_MAX_CROSS_ENTROPY_WITH_LOGITS,
                    metrics = Metrics.ACCURACY
                ),
                RandomNeuralAI(
                    training = moves,
                    inputType = false,
                    conv = listOf(
                        NeuralAIFactory.conv2D(
                            64,
                            3,
                            Activations.Tanh,
                            GlorotNormal(random.nextLong()),
                            RandomNormal(0.0f, 0.5f)
                        ),
                        NeuralAIFactory.conv2D(
                            32,
                            3,
                            Activations.Tanh,
                            GlorotNormal(random.nextLong()),
                            RandomNormal(0.0f, 0.5f)
                        ),
                        NeuralAIFactory.conv2D(
                            16,
                            2,
                            Activations.Tanh,
                            GlorotNormal(random.nextLong()),
                            RandomNormal(0.0f, 0.5f)
                        )
                    ),
                    dense = listOf(
                        NeuralAIFactory.dense(
                            32,
                            Activations.Exponential,
                            GlorotNormal(random.nextLong()),
                            RandomNormal(0.0f, 0.5f)
                        )
                    ),
                    output = NeuralAIFactory.dense(
                        7,
                        Activations.Relu,
                        GlorotNormal(random.nextLong()),
                        Constant(0.5f)
                    ),
                    losses = Losses.SOFT_MAX_CROSS_ENTROPY_WITH_LOGITS,
                    metrics = Metrics.ACCURACY
                ),
                RandomNeuralAI(
                    training = moves,
                    inputType = true,
                    conv = listOf(
                        NeuralAIFactory.conv2D(
                            64,
                            3,
                            Activations.Tanh,
                            GlorotNormal(random.nextLong()),
                            RandomNormal(0.0f, 0.5f)
                        ),
                        NeuralAIFactory.conv2D(
                            32,
                            3,
                            Activations.Tanh,
                            GlorotNormal(random.nextLong()),
                            RandomNormal(0.0f, 0.5f)
                        ),
                        NeuralAIFactory.conv2D(
                            16,
                            2,
                            Activations.Tanh,
                            GlorotNormal(random.nextLong()),
                            RandomNormal(0.0f, 0.5f)
                        )
                    ),
                    dense = listOf(
                        NeuralAIFactory.dense(
                            32,
                            Activations.Exponential,
                            GlorotNormal(random.nextLong()),
                            RandomNormal(0.0f, 0.5f)
                        )
                    ),
                    output = NeuralAIFactory.dense(
                        7,
                        Activations.Relu,
                        GlorotNormal(random.nextLong()),
                        Constant(0.5f)
                    ),
                    losses = Losses.SOFT_MAX_CROSS_ENTROPY_WITH_LOGITS,
                    metrics = Metrics.ACCURACY
                ),
                RandomNeuralAI(
                    training = moves,
                    inputType = true,
                    conv = listOf(
                        NeuralAIFactory.avg2D(
                            4,
                            1,
                            ConvPadding.SAME
                        ),
                        NeuralAIFactory.conv2D(
                            32,
                            3,
                            Activations.Tanh,
                            GlorotNormal(random.nextLong()),
                            RandomNormal(0.0f, 0.5f)
                        ),
                        NeuralAIFactory.conv2D(
                            16,
                            2,
                            Activations.Tanh,
                            GlorotNormal(random.nextLong()),
                            RandomNormal(0.0f, 0.5f)
                        )
                    ),
                    dense = listOf(
                        NeuralAIFactory.dense(
                            32,
                            Activations.Exponential,
                            GlorotNormal(random.nextLong()),
                            RandomNormal(0.0f, 0.5f)
                        )
                    ),
                    output = NeuralAIFactory.dense(
                        7,
                        Activations.Relu,
                        GlorotNormal(random.nextLong()),
                        Constant(0.5f)
                    ),
                    losses = Losses.SOFT_MAX_CROSS_ENTROPY_WITH_LOGITS,
                    metrics = Metrics.ACCURACY
                ),
                RandomNeuralAI(
                    training = moves,
                    inputType = true,
                    conv = listOf(
                        NeuralAIFactory.avg2D(
                            4,
                            1,
                            ConvPadding.SAME
                        ),
                        NeuralAIFactory.conv2D(
                            32,
                            3,
                            Activations.Tanh,
                            GlorotNormal(random.nextLong()),
                            RandomNormal(0.0f, 0.5f)
                        ),
                        NeuralAIFactory.conv2D(
                            16,
                            2,
                            Activations.Tanh,
                            GlorotNormal(random.nextLong()),
                            RandomNormal(0.0f, 0.5f)
                        )
                    ),
                    dense = listOf(
                        NeuralAIFactory.dense(
                            32,
                            Activations.Exponential,
                            GlorotNormal(random.nextLong()),
                            RandomNormal(0.0f, 0.5f)
                        )
                    ),
                    output = NeuralAIFactory.dense(
                        7,
                        Activations.Relu,
                        GlorotNormal(random.nextLong()),
                        Constant(0.5f)
                    ),
                    losses = Losses.SOFT_MAX_CROSS_ENTROPY_WITH_LOGITS,
                    metrics = Metrics.ACCURACY
                )
            ).map { NeuralCounter(it) }
        )

        val after = neurals.toList()

        println("Initialized complex Neurals:")
        after.filterNot { before.contains(it) }.forEach {
            println(it.ai.info())
        }
    }

    fun initWithRandom(amount: Int, trainingMoves: List<Move> = emptyList()) {
        val random = Random(Instant.now().toEpochMilli())

        val moves = trainingMoves.ifEmpty {
            getTrainingMoves(trainingPlayers.map { it() })
        }

        var i = 0

        while (i < amount) {
            try {
                val new = NeuralCounter(RandomNeuralAI(moves, random.nextBoolean()))
                neurals += new
                println("generated randomly: ${new.ai.info()}")
                i++
            } catch (_: Exception) {
            }
        }
    }
}