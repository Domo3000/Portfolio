
import connect4.messages.*
import db.Connection
import db.Result
import db.ResultDao
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock
import neural.Move
import neural.NeuralBuilder
import neural.RandomNeuralAI
import neural.StoredNeuralAI
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import utils.*
import java.io.File
import kotlin.random.Random
import kotlin.time.measureTimedValue

private fun buildAllCombinations(random: Random): Map<String, RandomNeuralAI> {
    val map = mutableMapOf<String, RandomNeuralAI>()

    LayerSize.entries.flatMap { layerSize ->
        Activation.entries.map { activation ->
            LayerDescription(layerSize, activation)
        }
    }.forEach { convLayer ->
        LayerSize.entries.flatMap { layerSize ->
            Activation.entries.map { activation ->
                LayerDescription(layerSize, activation)
            }
        }.forEach { denseLayer ->
            val neural = NeuralBuilder.build(convLayer, denseLayer, random)
            map[NeuralDescription(convLayer, denseLayer).toShortString()] = neural
        }
    }

    println(map.size)

    return map.toMap()
}

private fun initialSetup(random: Random) {
    println("!!!WARNING!!!")
    println("!!!WARNING!!!")
    println("!!!WARNING!!!")
    println("!!!WARNING!!!")
    println("!!!WARNING!!!")
    runBlocking {
        delay(10000)
    }

    transaction {
        SchemaUtils.drop(Result)
        SchemaUtils.create(Result)
    }

    val all = buildAllCombinations(random)
    val initialTraining = utils.getTrainingMoves(trainingPlayers(random)).shuffled().take(100)

    all.forEach { (name, ai) ->
        println("$name: ${ai.info()}")
        ai.train(initialTraining)
        ai.store(name)
        ResultDao.insert(name, 0.0)
    }
}

private fun resetNeural(description: NeuralDescription, initialTraining: List<Move>, random: Random) {
    val name = description.toShortString()
    println("!!!WARNING!!!")
    println("WARNING resetting: $name")
    println("!!!WARNING!!!")
    runBlocking {
        delay(2000)
    }

    ResultDao.deleteByPlayer(description.toShortString())

    val neural = NeuralBuilder.build(description.conv, description.dense, random)

    println("${description.toShortString()}: ${neural.info()}")
    neural.train(initialTraining)
    neural.store(name)
    ResultDao.insert(name, 0.0)
}

private fun resultsToJson(results: List<ResultDao>) {
    val mappedResults = results.groupBy { it.player }
        .map { (name, result) ->
            NeuralDescription.fromShortString(name) to result.sortedBy { it.id.value }.map { it.points }.drop(1)
        }

    val new = File("${System.getProperty("user.dir")}/training.json")
    val json = TrainingResultsMessage(mappedResults).encode()
    new.createNewFile()
    new.writeText(json)
}

private fun runBreathFirst(repeatsPerNeural: Int = 10, parallelism: Int = 10) = runBlocking {
    val mutex = Mutex()
    val random = Random(Clock.System.now().epochSeconds)

    val ais = mutableListOf<Pair<String, Int>>()

    suspend fun nextAI(): Pair<String, Int>? = mutex.withLock {
        val next = ais.firstOrNull()
        next?.let { ais.removeAt(0) }
        next
    }

    while (true) {
        val left = ResultDao.getAll()
            .groupBy { it.player }
            .map { (name, scores) ->
                name to scores.size
            }
            .filter { it.second < 204 }
            .sortedBy { it.second }
            .take(50)

        if (ais.isEmpty()) {
            if (left.isEmpty()) {
                throw Exception("FINISHED")
            }
            ais.addAll(left)
        }

        println(left.size)
        println(left.map { it.second }.average())
        left.groupBy { it.first.first().toString() }
            .forEach { (start, list) ->
                println("$start ${list.map { it.second }.average()}")
            }
        println(left.minBy { it.second } to left.maxBy { it.second })

        println(Clock.System.now())
        val winningMoves = runBlocking {
            (0 until 10).map {
                CoroutineScope(Dispatchers.Default).async {
                    Moves.createWinningMoves(2000)
                }
            }.awaitAll().flatten()
        }

        val trainingMoves = Moves.createTrainingMoves(10).toMutableList()

        while (trainingMoves.size < ((repeatsPerNeural + 1) * 4500)) {
            println(trainingMoves.size)
            trainingMoves += Moves.createTrainingMoves(10)
        }

        val chunkedTrainingMoves = trainingMoves.chunked(4500).dropLast(1)

        (0 until parallelism).map {
            CoroutineScope(Dispatchers.Default).async {
                var nextAI = nextAI()

                while (nextAI != null) {
                    val timedValue = measureTimedValue {
                        nextAI?.let { (name, count) ->
                            println("$name: $count ${Clock.System.now()}")
                            val ai = StoredNeuralAI.fromStorage(name)

                            val scores = chunkedTrainingMoves.map { moves ->
                                val battleScore = evaluateBattles(ai, battlePlayers(random))
                                val winningMoveScore = evaluateWinningMoves(ai, winningMoves.shuffled().take(1000))
                                val score = ((battleScore * 2.0) + winningMoveScore) / 3.0

                                if (score > 0.85) {
                                    ai.store("best-" + name + "-${(score * 10000).toInt()}")
                                }

                                val training =
                                    (moves + winningMoves.map { it.first }.shuffled().take(1000)).shuffled()
                                ai.train(training)

                                score
                            }

                            scores.forEach { ResultDao.insert(name, it) }

                            ai.store(name)
                            Triple(name, count + scores.size, scores.max())
                        }
                    }

                    println("${timedValue.value} in ${timedValue.duration}")

                    nextAI = nextAI()
                }
            }
        }.awaitAll()

        resultsToJson(ResultDao.getAll())
    }
}

fun main() {
    Connection("jdbc:h2:file:./build/db", "user", "password")

    runBreathFirst()
}
