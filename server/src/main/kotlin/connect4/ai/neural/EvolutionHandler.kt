package connect4.ai.neural

import connect4.ai.AI
import connect4.ai.AIs
import connect4.ai.length.BalancedLengthAI
import connect4.ai.length.PlyLengthAI
import connect4.ai.length.SimpleLengthAI
import connect4.ai.monte.BalancedMonteCarloAI
import connect4.game.Connect4Game
import connect4.game.Player
import org.jetbrains.kotlinx.dl.api.core.Sequential
import org.jetbrains.kotlinx.dl.api.core.activation.Activations
import org.jetbrains.kotlinx.dl.api.core.initializer.Constant
import org.jetbrains.kotlinx.dl.api.core.initializer.GlorotNormal
import org.jetbrains.kotlinx.dl.api.core.initializer.Zeros
import org.jetbrains.kotlinx.dl.api.core.layer.convolutional.ConvPadding
import org.jetbrains.kotlinx.dl.api.core.loss.Losses
import org.jetbrains.kotlinx.dl.api.core.metric.Metrics
import java.io.File
import kotlin.random.Random

class NeuralCounter(
    val ai: RandomNeuralAI,
    var trained: Int = 0,
    var gamesPlayed: Int = 0,
    var gamesWon: Int = 0
)

class EvolutionHandler(maxChildren: Int = 10) {
    private val neurals = mutableListOf<NeuralCounter>()

    private val trainingPlayers = listOf(
        { SimpleLengthAI() },
        { BalancedLengthAI() },
        { PlyLengthAI() },
        { BalancedMonteCarloAI(500) },
        { BalancedMonteCarloAI(700) }
    )

    var strongest: NeuralCounter? = null

    fun allNeurals(): List<NeuralCounter> = neurals.toList()

    private fun leastTrained(): NeuralCounter =
        neurals.minBy { it.trained }

    private fun leastPlayed(): NeuralCounter =
        neurals.minBy { it.gamesPlayed }

    fun highestRanking(amount: Int): List<NeuralCounter> =
        neurals.sortedByDescending { it.gamesWon }.subList(
            0, if (amount < neurals.size) {
                amount
            } else {
                neurals.size - 1
            }
        )

    private fun getTrainingMoves(players: List<AI>): List<Move> {
        val winningMoves = players.map { p1 ->
            players.mapNotNull { p2 ->
                val result = Connect4Game.runGame(p1, p2)
                if (result.first != null) {
                    result.second to result.first
                } else {
                    null
                }
            }
        }.flatten()

        return winningMoves.map { (g, p) ->
            val game = Connect4Game()

            g.map { m ->
                val move = Move(game.field.map { it.toList() }, m)

                val allMoves = game.availableColumns.map { column ->
                    val maybeWinningGame = Connect4Game(game.field, game.currentPlayer)
                    maybeWinningGame.makeMove(column)
                    if (maybeWinningGame.hasFinished()) {
                        Move(game.field.map { it.toList() }, column)
                    } else {
                        null
                    }
                } + if (game.currentPlayer == p) {
                    move
                } else {
                    null
                }

                game.makeMove(m)

                allMoves.filterNotNull()
            }.flatten()
        }.flatten()
    }

    // TODO store lots of trainingMoves so I don't have to run them again
    fun train(neurals: List<NeuralCounter> = emptyList()) {
        val players = AIs.highAIs.map { it() } + highestRanking(1).map { it.ai } + strongest?.ai

        val moves = getTrainingMoves(players.filterNotNull())

        if (neurals.isEmpty()) {
            val counter = leastTrained()
            counter.ai.train(moves)
            counter.trained++
        } else {
            neurals.forEach { counter ->
                counter.ai.train(moves)
                counter.trained++
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

    fun evaluate(): Boolean {
        var new = false
        neurals.forEach { counter ->
            println("${counter.gamesWon}/${counter.gamesPlayed}: ${counter.ai.info()}")

            var wins = 0

            listOf(
                SimpleLengthAI(),
                PlyLengthAI(),
                BalancedMonteCarloAI(350),
                BalancedMonteCarloAI(700)
            ).map { opponent ->
                val currentWins = counter.gamesWon
                val currentGames = counter.gamesPlayed
                repeat(10) { singleBattle(counter, opponent, true) }
                val newWins = counter.gamesWon
                wins += newWins - currentWins

                counter.gamesWon = currentWins
                counter.gamesPlayed = currentGames
            }

            if (strongest == null || wins >= strongest!!.gamesWon) {
                storeStrongest(counter.ai)
                strongest = loadStrongest(wins)
                println("New Strongest: $wins: ${strongest!!.ai.info()}")
                new = true
            }
        }
        return new
    }

    fun battle(
        players: List<() -> AI> = AIs.highAIs,
        ai: NeuralCounter = leastPlayed()
    ) {
        players.forEach { opponent ->
            singleBattle(ai, opponent(), true)
        }
    }

    private fun evolve(
        first: RandomNeuralAI,
        second: RandomNeuralAI,
        training: List<Move> = emptyList()
    ): List<RandomNeuralAI> = listOf(
        { first.copy(training) },
        { first.copy(training, conv = first.convLayer.map { it.randomize() }) },
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

    fun evolve() {
        val highest: List<NeuralCounter> = (highestRanking(5) + strongest).filterNotNull()
        val evolved: List<RandomNeuralAI> =
            (listOf(strongest) + highestRanking(2)).asSequence().filterNotNull()
                .map { it.ai }.take(2).zipWithNext { a, b ->
                    evolve(
                        a,
                        b
                    )
                }.flatten().toList()

        neurals.clear()
        highest.forEach { counter ->
            println("${counter.gamesWon} : ${counter.ai.name}")
            neurals += NeuralCounter(counter.ai)
        }
        evolved.forEach { e ->
            println("New evolution: ${e.info()}")
            neurals += NeuralCounter(e)
        }
    }

    // TODO method to purge weakest from list of neurals

    // TODO remove print or use currentScore
    fun resetBattles(printAll: Boolean = false, printHighest: Boolean = true) {
        println("reset")
        if (printAll) {
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
        neurals.forEach {
            it.gamesWon = 0
            it.gamesPlayed = 0
        }
    }

    fun currentScore() {
        println("Score")
        neurals.forEach {
            println("${it.gamesWon}/${it.gamesPlayed}: ${it.ai.info()}")
        }
        println("Highest:")
        neurals.maxBy { it.gamesWon }.let {
            println("${it.gamesWon}/${it.gamesPlayed}: ${it.ai.info()}")
        }
    }

    fun storeHighest(highest: Int = 3) {
        val baseDirectory = File("${System.getProperty("user.dir")}/neurals")
        if (!baseDirectory.isDirectory) {
            baseDirectory.mkdir()
        }
        highestRanking(highest).forEachIndexed { index, counter ->
            val directory = File("$baseDirectory/$index")
            println("highest directory: $index = ${counter.ai.info()}")
            if (!directory.isDirectory) {
                directory.mkdir()
            }
            counter.ai.store(directory)
        }
    }

    var c = 1

    fun storeStrongest(ai: RandomNeuralAI) {
        val baseDirectory = File("${System.getProperty("user.dir")}/neurals")
        if (!baseDirectory.isDirectory) {
            baseDirectory.mkdir()
        }
        c++
        println("directory: $c")
        val directory = File("$baseDirectory/$c")
        if (!directory.isDirectory) {
            directory.mkdir()
        }
        ai.store(directory)
    }

    fun loadStrongest(wins: Int): NeuralCounter {
        val baseDirectory = File("${System.getProperty("user.dir")}/neurals")
        if (!baseDirectory.isDirectory) {
            baseDirectory.mkdir()
        }
        val directory = File("$baseDirectory/$c")
        val file = File(directory.path + "/modelConfig.json")

        val stored = Sequential.loadModelConfiguration(file)
        val loaded = RandomNeuralAI.fromStorage(stored)
        return NeuralCounter(loaded, gamesWon = wins)
    }

    private fun loadStored() {
        val directory = File("${System.getProperty("user.dir")}/neurals")

        if (directory.isDirectory) {
            directory.walk().forEach { neuralDirectory ->
                if (neuralDirectory != directory && neuralDirectory.isDirectory) {
                    val file = File(neuralDirectory.path + "/modelConfig.json")
                    try {
                        val stored = Sequential.loadModelConfiguration(file)
                        val loaded = RandomNeuralAI.fromStorage(stored)
                        println("${neuralDirectory.path}: ${loaded.name}")
                        neurals += NeuralCounter(loaded)
                    } catch (e: Exception) {
                        println(e)
                    }
                }
            }
        }
    }

    init {
        val moves = getTrainingMoves(trainingPlayers.map { it() })
        val random = Random(0)

        // TODO disable on PROD
        val test = RandomNeuralAI(
            training = moves,
            conv = emptyList(),
            dense = emptyList(),
            output = NeuralAIFactory.dense(8, Activations.Linear, GlorotNormal(random.nextLong()), Constant(0.5f)),
            losses = Losses.SOFT_MAX_CROSS_ENTROPY_WITH_LOGITS,
            metrics = Metrics.ACCURACY
        )

        val test2 = RandomNeuralAI(
            training = moves,
            conv = listOf(
                NeuralAIFactory.conv2D(3, 4, Activations.LiSHT, GlorotNormal(random.nextLong()), Zeros())
            ),
            dense = emptyList(),
            output = NeuralAIFactory.dense(8, Activations.Linear, GlorotNormal(random.nextLong()), Constant(0.5f)),
            losses = Losses.SOFT_MAX_CROSS_ENTROPY_WITH_LOGITS,
            metrics = Metrics.ACCURACY
        )

        val test3 = RandomNeuralAI(
            training = moves,
            conv = listOf(
                NeuralAIFactory.max2D(4, 1, ConvPadding.VALID)
            ),
            dense = listOf(
                NeuralAIFactory.dense(80, Activations.HardSigmoid, GlorotNormal(random.nextLong()), Constant(0.5f)),
            ),
            output = NeuralAIFactory.dense(8, Activations.Linear, GlorotNormal(random.nextLong()), Constant(0.5f)),
            losses = Losses.SOFT_MAX_CROSS_ENTROPY_WITH_LOGITS,
            metrics = Metrics.ACCURACY
        )

        val test4 = RandomNeuralAI(
            training = moves,
            conv = listOf(
                NeuralAIFactory.conv2D(3, 4, Activations.LiSHT, GlorotNormal(random.nextLong()), Zeros())
            ),
            dense = listOf(
                NeuralAIFactory.dense(20, Activations.HardSigmoid, GlorotNormal(random.nextLong()), Constant(0.5f)),
            ),
            output = NeuralAIFactory.dense(8, Activations.Linear, GlorotNormal(random.nextLong()), Constant(0.5f)),
            losses = Losses.SOFT_MAX_CROSS_ENTROPY_WITH_LOGITS,
            metrics = Metrics.ACCURACY
        )

        val test5 = RandomNeuralAI(
            training = moves,
            conv = listOf(
                NeuralAIFactory.conv2D(3, 4, Activations.LiSHT, GlorotNormal(random.nextLong()), Zeros())
            ),
            dense = listOf(
                NeuralAIFactory.dense(74, Activations.HardSigmoid, GlorotNormal(random.nextLong()), Constant(0.5f)),
            ),
            output = NeuralAIFactory.dense(8, Activations.Linear, GlorotNormal(random.nextLong()), Constant(0.5f)),
            losses = Losses.SOFT_MAX_CROSS_ENTROPY_WITH_LOGITS,
            metrics = Metrics.ACCURACY
        )

        val test6 = RandomNeuralAI(
            training = moves,
            conv = listOf(
                NeuralAIFactory.conv2D(3, 5, Activations.LiSHT, GlorotNormal(random.nextLong()), Zeros())
            ),
            dense = listOf(
                NeuralAIFactory.dense(120, Activations.HardSigmoid, GlorotNormal(random.nextLong()), Constant(0.5f)),
            ),
            output = NeuralAIFactory.dense(8, Activations.Linear, GlorotNormal(random.nextLong()), Constant(0.5f)),
            losses = Losses.SOFT_MAX_CROSS_ENTROPY_WITH_LOGITS,
            metrics = Metrics.ACCURACY
        )

        neurals.addAll(
            listOf(
                test,
                test2,
                test3,
                test4,
                test5,
                test6
            ).map { NeuralCounter(it) }
        )

        loadStored()

        while (neurals.size < maxChildren) {
            try {
                neurals += NeuralCounter(RandomNeuralAI(moves))
            } catch (_: Exception) {
            }
        }

        neurals.forEach {
            println("${it.trained}: ${it.ai.info()}")
        }
    }
}