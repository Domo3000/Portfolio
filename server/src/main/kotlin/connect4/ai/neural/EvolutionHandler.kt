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
import org.jetbrains.kotlinx.dl.api.core.initializer.Zeros
import org.jetbrains.kotlinx.dl.api.core.layer.convolutional.ConvPadding
import org.jetbrains.kotlinx.dl.api.core.loss.Losses
import org.jetbrains.kotlinx.dl.api.core.metric.Metrics
import java.io.File
import kotlin.random.Random

// TODO find a smoother/dynamic way to do this
fun List<Move>.repeatLastMoves(): List<Move> {
    return if (size > 8) {
        val last8 = takeLast(8)
        val last4 = takeLast(4)
        val last2 = takeLast(2)
        val last = last()
        this + last8 + last4 + last2 + last
    } else {
        this
    }
}

fun getTrainingMoves(players: List<AI>): List<Move> {
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

    return winningMoves.map { (moves, player) ->
        val game = Connect4Game()

        moves.map { m ->
            val move = Move(game.field.map { it.toList() }, m)

            val allMoves = game.availableColumns.map { column ->
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

            game.makeMove(m)

            allMoves.filterNotNull()
        }.flatten().repeatLastMoves()
    }.flatten()
}

class NeuralCounter(
    val ai: RandomNeuralAI,
    val epochs: Int = 500,
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

    fun train(neurals: List<NeuralCounter> = emptyList(), trainingMoves: List<Move> = emptyList()) {
        val moves = trainingMoves.ifEmpty {
            getTrainingMoves(trainingPlayers.map { it() })
        }

        if (neurals.isEmpty()) {
            val counter = leastTrained()
            counter.ai.train(moves, counter.epochs)
            counter.trained++
        } else {
            neurals.forEach { counter ->
                counter.ai.train(moves, counter.epochs)
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
                BalancedMonteCarloAI(350)
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

    fun purge(keep: Int = 10) {
        val toKeep: List<NeuralCounter> = highestRanking(keep)
        neurals.clear()
        neurals += toKeep
    }

    fun evolve() {
        val highest: List<NeuralCounter> = (highestRanking(5) + strongest).filterNotNull()
        val evolved: List<NeuralCounter> =
            (listOf(strongest) + highestRanking(3)).asSequence().filterNotNull()
                .map { it.ai to it.epochs }.take(3).zipWithNext { a, b ->
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
                println("${it.gamesWon}/${it.gamesPlayed}: ${it.epochs}/${it.ai.info()}")
            }
        }
        if (printHighest) {
            println("Highest:")
            neurals.maxBy { it.gamesWon }.let {
                println("${it.gamesWon}/${it.gamesPlayed}: ${it.epochs}/${it.ai.info()}")
            }
        }
    }

    fun storeHighest(highest: Int = 3) {
        val baseDirectory = File("${System.getProperty("user.dir")}/neurals")
        if (!baseDirectory.isDirectory) {
            baseDirectory.mkdir()
        }
        highestRanking(highest).forEachIndexed { index, counter ->
            println("highest directory: $index = ${counter.ai.info()}")
            counter.ai.store("$baseDirectory/$index")
        }
    }

    // TODO automatically detect highest folder number
    var c = 84
    fun storeStrongest(ai: RandomNeuralAI) {
        val baseDirectory = File("${System.getProperty("user.dir")}/neurals")
        if (!baseDirectory.isDirectory) {
            baseDirectory.mkdir()
        }
        c++
        println("directory: $c = ${ai.info()}")
        ai.store("$baseDirectory/$c")
    }

    private fun loadStrongest(wins: Int): NeuralCounter {
        val baseDirectory = File("${System.getProperty("user.dir")}/neurals")
        if (!baseDirectory.isDirectory) {
            baseDirectory.mkdir()
        }
        val directory = File("$baseDirectory/$c")
        val loaded = RandomNeuralAI.fromStorage(directory)
        return NeuralCounter(loaded, gamesWon = wins)
    }

    private fun loadStored() {
        val directory = File("${System.getProperty("user.dir")}/neurals")

        if (directory.isDirectory) {
            directory.walk().forEach { neuralDirectory ->
                if (neuralDirectory != directory && neuralDirectory.isDirectory) {
                    try {
                        val loaded = RandomNeuralAI.fromStorage(neuralDirectory)
                        println("${neuralDirectory.path}: ${loaded.name}")
                        neurals += NeuralCounter(loaded)
                    } catch (e: Exception) {
                        println(e)
                    }
                }
            }
        }
    }

    fun initWithBasicNeurals(trainingMoves: List<Move> = emptyList()) {
        val moves = trainingMoves.ifEmpty {
            getTrainingMoves(trainingPlayers.map { it() })
        }
        val random = Random(0)

        val test = RandomNeuralAI(
            training = moves,
            conv = emptyList(),
            dense = emptyList(),
            output = NeuralAIFactory.dense(7, Activations.Linear, GlorotNormal(random.nextLong()), Constant(0.5f)),
            losses = Losses.SOFT_MAX_CROSS_ENTROPY_WITH_LOGITS,
            metrics = Metrics.ACCURACY
        )

        val test1 = RandomNeuralAI(
            training = moves,
            conv = listOf(
                NeuralAIFactory.conv2D(3, 4, Activations.LiSHT, GlorotNormal(random.nextLong()), Zeros())
            ),
            dense = listOf(
                NeuralAIFactory.dense(28, Activations.HardSigmoid, GlorotNormal(random.nextLong()), Constant(0.5f)),
            ),
            output = NeuralAIFactory.dense(7, Activations.Linear, GlorotNormal(random.nextLong()), Constant(0.5f)),
            losses = Losses.SOFT_MAX_CROSS_ENTROPY_WITH_LOGITS,
            metrics = Metrics.ACCURACY
        )

        val test2 = RandomNeuralAI(
            training = moves,
            conv = listOf(
                NeuralAIFactory.conv2D(3, 4, Activations.LiSHT, GlorotNormal(random.nextLong()), Zeros())
            ),
            dense = emptyList(),
            output = NeuralAIFactory.dense(7, Activations.Linear, GlorotNormal(random.nextLong()), Constant(0.5f)),
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
            output = NeuralAIFactory.dense(7, Activations.Linear, GlorotNormal(random.nextLong()), Constant(0.5f)),
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
            output = NeuralAIFactory.dense(7, Activations.Linear, GlorotNormal(random.nextLong()), Constant(0.5f)),
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
            output = NeuralAIFactory.dense(7, Activations.Linear, GlorotNormal(random.nextLong()), Constant(0.5f)),
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
            output = NeuralAIFactory.dense(7, Activations.Linear, GlorotNormal(random.nextLong()), Constant(0.5f)),
            losses = Losses.SOFT_MAX_CROSS_ENTROPY_WITH_LOGITS,
            metrics = Metrics.ACCURACY
        )

        listOf(10, 50, 100, 500).forEach { epochs ->
            neurals.addAll(
                listOf(
                    test,
                    test1,
                    test2,
                    test3,
                    test4,
                    test5,
                    test6
                ).map { NeuralCounter(it, epochs = epochs) }
            )
        }
    }

    fun initWithRandom(amount: Int, trainingMoves: List<Move> = emptyList()) {
        val moves = trainingMoves.ifEmpty {
            getTrainingMoves(trainingPlayers.map { it() })
        }

        var i = 0

        while (i < amount) {
            try {
                val random = NeuralCounter(RandomNeuralAI(moves))
                neurals += random
                println("generated randomly: ${random.ai.info()}")
                i++
            } catch (_: Exception) { }
        }
    }

    init {
        loadStored()

        println("Loaded from Storage:")
        neurals.forEach {
            println(it.ai.info())
        }
    }
}