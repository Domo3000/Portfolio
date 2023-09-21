package connect4.ai.neural

import connect4.ai.AI
import connect4.game.Connect4Game
import kotlinx.coroutines.*
import java.time.Instant
import kotlin.random.Random

// TODO test
fun List<Move>.repeatLastMoves(): List<Move> {
    return if (size >= 2) {
        this + takeLast(size / 2).repeatLastMoves()
    } else {
        this
    }
}

// TODO test
// also TODO: boolean flag to include all winningMoves
fun getTrainingMoves(players: List<() -> AI>): List<Move> {
    val winningMoves = players.map { p1 ->
        players.mapNotNull { p2 ->
            val result = Connect4Game.runGame(p1(), p2())
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

object NeuralTrainer {
    fun train(toTrain: List<RandomNeuralAI>, trainingMoves: List<Move>) {
        val chunks = toTrain.shuffled().chunked(5)

        chunks.map { trainees ->
            runBlocking {
                trainees.map { ai ->
                    CoroutineScope(Dispatchers.Default).async {
                        ai.train(trainingMoves)
                    }
                }.awaitAll()
            }
        }
    }
}

class EvolutionHandler(private val random: Random = Random(Instant.now().toEpochMilli())) {
    private val neurals = mutableListOf<RandomNeuralAI>()

    fun allNeurals(): List<RandomNeuralAI> = neurals.toList()

    fun train(toTrain: List<RandomNeuralAI>, trainingMoves: List<Move>) {
        val chunks = toTrain.shuffled().chunked(5)

        chunks.map { trainees ->
            runBlocking {
                trainees.map { ai ->
                    CoroutineScope(Dispatchers.Default).async {
                        if (!ai.train(trainingMoves)) {
                            neurals -= ai
                        }
                    }
                }.awaitAll()
            }
        }
    }

    fun softEvolve(
        toEvolve: RandomNeuralAI,
    ) {
        println("Creating from softEvolve:")

        /*
        listOf(
            {
                toEvolve.copy(
                    training,
                    inputSingular = !toEvolve.inputSingular,
                    conv = toEvolve.convLayer.map { it.softRandomize() })
            },
            { toEvolve.copy(training, conv = toEvolve.convLayer.map { it.softRandomize() }) },
            {
                toEvolve.copy(
                    training,
                    inputSingular = !toEvolve.inputSingular,
                    dense = toEvolve.denseLayer.map { it.softRandomize() })
            },
            { toEvolve.copy(training, dense = toEvolve.denseLayer.map { it.softRandomize() }) },
            {
                toEvolve.copy(
                    training,
                    conv = toEvolve.convLayer.map { it.softRandomize() },
                    dense = toEvolve.denseLayer.map { it.softRandomize() })
            }
        ).mapNotNull {
            try {
                it()
            } catch (_: Exception) {
                null
            }
        }.forEach {
            neurals += it
            println(it.info())
        }

         */
    }

    fun evolve(
        toEvolve: RandomNeuralAI,
    ) {
        println("Creating from evolving:")

        /*
        listOf(
            { toEvolve.copy(training, inputSingular = !toEvolve.inputSingular) },
            { toEvolve.copy(training, conv = toEvolve.convLayer.map { it.randomize() }) },
            { toEvolve.copy(training, dense = toEvolve.denseLayer.map { it.randomize() }) },
            { toEvolve.copy(training, output = LayerFactory.randomDenseLayer(7)) },
            { toEvolve.copy(training, conv = null) },
            { toEvolve.copy(training, dense = null) },
            {
                toEvolve.copy(
                    conv = toEvolve.convLayer.map { it.randomize() },
                    dense = toEvolve.denseLayer.map { it.randomize() },
                    output = LayerFactory.randomDenseLayer(7)
                )
            }
        ).mapNotNull {
            try {
                it()
            } catch (_: Exception) {
                null
            }
        }.forEach {
            neurals += it
            println(it.info())
        }

         */
    }

    fun setNeurals(toKeep: List<RandomNeuralAI>) {
        neurals.clear()
        neurals += toKeep
    }

    fun initPredefinedNeurals(name: String, toGenerate: (Random) -> List<RandomNeuralAI>) {
        println("Initializing $name Neurals:")

        val new = toGenerate(random)

        neurals.addAll(new)

        println("Initialized $name Neurals:")
        new.forEach {
            println(it.info())
        }
    }

    fun initWithRandom(amount: Int, trainingMoves: List<Move> = emptyList()) {
        var i = 0

        /*
        while (i < amount) {
            try {
                val new = RandomNeuralAI(random.nextBoolean())
                neurals += new
                println("generated randomly: ${new.info()}")
                i++
            } catch (_: Exception) {
            }
        }

         */
    }

    fun initFromStored(storedNeurals: List<StoredNeuralAI>) {
        storedNeurals.map { it.toRandomNeural() }.forEach {
            println("converted: ${it.info()}")
            neurals += it
        }
    }
}