package neural

import ai.AI
import connect4.game.Connect4Game
import kotlinx.coroutines.*
import java.time.Instant
import kotlin.random.Random

fun List<Move>.repeatLastMoves(): List<Move> {
    return if (size >= 2) {
        this + takeLast(size / 2).repeatLastMoves()
    } else {
        this
    }
}

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

    fun evolve(
        toEvolve: RandomNeuralAI,
    ) {
        println("Creating from evolve:")

        listOf(
            { toEvolve.evolve(newConv = true) },
            { toEvolve.evolve(newDense = true) },
            { toEvolve.evolve(softRandomize = true) },
            { toEvolve.evolve(softRandomize = true, newConv = true) },
            { toEvolve.evolve(softRandomize = true, newDense = true) }
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

    fun initWithRandom(amount: Int) {
        var i = 0

        while (i < amount) {
            try {
                val new = RandomNeuralAI(InputType.toInput(i % 2 == 0))
                neurals += new
                println("generated randomly: ${new.info()}")
                i++
            } catch (_: Exception) {
            }
        }
    }

    fun initFromStored(storedNeurals: List<StoredNeuralAI>) {
        storedNeurals.map { it.toRandomNeural() }.forEach {
            println("converted: ${it.info()}")
            neurals += it
        }
    }
}