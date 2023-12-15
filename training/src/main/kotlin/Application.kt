import connect4.game.*
import connect4.messages.*
import db.*
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock
import neural.NeuralAI
import neural.NeuralBuilder
import neural.StoredNeuralAI
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import utils.Moves
import utils.battlePlayers
import utils.evaluateBattles
import utils.evaluateWinningMoves
import java.io.File
import kotlin.random.Random
import kotlin.time.measureTime

private fun loadNeural(name: String) = StoredNeuralAI.fromStorage("training/data/neurals", name)
private fun NeuralAI.store(name: String) = store("training/data/neurals", name)

private val allCombinations = listOf(true, false).flatMap { input ->
    listOf(true, false).flatMap { batchNorm ->
        Padding.entries.flatMap { padding ->
            LayerSize.entries.flatMap { layerSize ->
                Activation.entries.map { activation ->
                    ConvLayerDescription(layerSize, activation, padding)
                }
            }.flatMap { convLayer ->
                LayerSize.entries.flatMap { layerSize ->
                    Activation.entries.map { activation ->
                        DenseLayerDescription(layerSize, activation)
                    }
                }.flatMap { denseLayer ->
                    OutputActivation.entries.map { output ->
                        NeuralDescription(
                            input,
                            batchNorm,
                            convLayer,
                            denseLayer,
                            output
                        ).toShortString()
                    }
                }
            }
        }
    }
}.map { NeuralDescription.fromShortString(it) }.toSet().toList()

private val initialTraining by lazy {
    (Moves.createTrainingMoves(4).shuffled().take(4000) +
            Moves.createWinningMoves(1000).take(1000).map { it.first })
        .shuffled()
        .chunked(1000)
}

private val times by lazy { TimeDao.getAll() }

private val random = Random(Clock.System.now().epochSeconds)

private fun createMissingCombinations(group: TrainingGroup) {
    val current = ResultDao.getAll().map { it.key }

    val matching = allCombinations.filter { description ->
        group.contains(description)
    }

    val missing = matching.filter { description ->
        !current.contains(description.toShortString())
    }

    println(matching.size)
    println(missing.size)

    missing.forEach {
        val name = it.toShortString()
        println(name)

        val neural = NeuralBuilder.build(it, random)
        println(neural.info())
        println(neural.paramsCount())

        initialTraining.forEach { training ->
            val time = measureTime { neural.train(training) }
            TimeDao.insert(name, time)
        }

        println(TimeDao.getByPlayer(name))
        ResultDao.insert(name, 0.0)
        neural.store(name)
    }
}

private fun initialSetup() {
    println("!!!WARNING!!!")
    println("!!!WARNING!!!")
    println("!!!WARNING!!!")
    println("!!!WARNING!!!")
    println("!!!WARNING!!!")
    runBlocking {
        delay(10000)
    }

    transaction {
        SchemaUtils.drop(Result, Time)
        SchemaUtils.create(Result, Time)
    }

    TrainingGroups.all.forEach {
        createMissingCombinations(it)
    }
}

private fun resultsToJson(fileName: String, group: TrainingGroup) {
    val mappedResults = ResultDao.getAll()
        .map { (name, scores) ->
            Triple(NeuralDescription.fromShortString(name), name, scores)
        }
        .filter {
            group.contains(it.first) && group.max <= it.third.size
        }.map { (description, name, scores) ->
            TrainingResultMessage(
                description,
                times[name]!!,
                scores.sortedBy { it.id.value }.map { it.points }.take(group.max).drop(1)
            )
        }

    val new = File("${System.getProperty("user.dir")}/shared/src/commonMain/resources/assets/$fileName-experiment.json")
    val json = TrainingResultsMessage(mappedResults).encode()
    new.createNewFile()
    new.writeText(json)
}

private fun allResultsToJson() {
    listOf(
        "inputOutput" to TrainingGroups.inputOutputExperiment,
        "batchNorm" to TrainingGroups.batchNormExperiment,
        //"samePadding" to TrainingGroups.samePaddingExperiment,
        //"validPadding" to TrainingGroups.validPaddingExperiment,
        //"longTraining" to TrainingGroups.longExperiment
    ).forEach { (name, group) ->
        println(name)
        ResultDao.getAll()
            .map { (name, scores) ->
                name to scores.size
            }
            .filter {
                group.contains(NeuralDescription.fromShortString(it.first))
            }.let { list ->
                println(list.size)
                println("done: " + list.filter { group.max <= it.second }.size)
                println("notDone: " + list.filter { group.max > it.second }.size)
                println(list.filter { group.max > it.second }.map { it.second }.average())
            }

        resultsToJson(name, group)
    }
}

private fun analyzeResults() {
    println("Highest scores:")
    ResultDao.getAll()
        .map { (name, scores) ->
            Triple(name, scores.size, scores.maxBy { it.points }.points)
        }
        .sortedByDescending { it.third }
        .take(10)
        .forEach { println(it) }

    listOf<Pair<String, (String) -> Double>>(
        "First" to { 1.0 },
        "Shortest training time" to { times[it]!! }
    ).map { (title, multiplier) ->
        println("$title to x:")
        listOf(
            0.2, 0.3, 0.4, 0.5, 0.6,
            0.7, 0.725, 0.75, 0.775,
            0.8, 0.81, 0.82, 0.83, 0.84, 0.85, 0.86, 0.87, 0.88, 0.89,
            0.9, 0.91, 0.92, 0.93, 0.94
        ).forEach { score ->
            ResultDao.getAll()
                .map { (name, scores) ->
                    name to scores.mapIndexed { index, resultDao ->
                        index to resultDao.points
                    }
                }.map { (name, scores) ->
                    name to scores.firstOrNull { it.second > score }
                }.minByOrNull { it.second?.first?.let { a -> a * multiplier(it.first) } ?: Double.MAX_VALUE }
                ?.let { println(it) }
        }
    }
}

private fun run(
    groups: List<TrainingGroup> = TrainingGroups.all,
    repeatsPerNeural: Int = 50,
    parallelism: Int = 20
) = runBlocking {
    val mutex = Mutex()
    val ais = mutableMapOf<String, Triple<StoredNeuralAI, Int, List<Double>>>()

    val allAisGrouped = mutableListOf<List<Pair<NeuralDescription, Int>>>()

    fun nextAIGroup(): List<Pair<NeuralDescription, Int>>? {
        if (allAisGrouped.isEmpty()) {
            val next: List<List<Pair<NeuralDescription, Int>>> = ResultDao.getAll()
                .map { (name, scores) ->
                    name to scores.size
                }
                .filter { groups.any { group -> group.contains(NeuralDescription.fromShortString(it.first)) && group.max > it.second } }
                .map { NeuralDescription.fromShortString(it.first) to it.second }
                .groupBy { it.first.conv.padding }
                .flatMap { (_, descriptions) ->
                    descriptions
                        .groupBy { it.first.conv.size }
                        .flatMap { it.value.chunked(parallelism) }
                }

            allAisGrouped.addAll(next)
        }

        return allAisGrouped.getOrNull(0)?.let { ais ->
            allAisGrouped.removeAt(0)

            println("nextGroup: ${ais.size} elements similar to ${ais.first().first.toShortString()}")
            ais
        }
    }

    fun saveProgress(name: String) {
        print("saving progress for $name")
        ais[name]?.let { (ai, rounds, scores) ->
            ResultDao.insert(name, scores)
            ai.store(name)
            ais[name] = Triple(ai, rounds, emptyList())
        }
        println("- done!")
    }

    fun saveProgress() {
        println("saving all progress")
        ais.forEach { (name, _) ->
            saveProgress(name)
        }
        println("progress saved")
    }

    fun removeIfDone() {
        val done = ais.filterNot { (name, pair) ->
            groups.any { group -> group.contains(NeuralDescription.fromShortString(name)) && group.max > pair.second }
        }
        done.forEach { (name, _) ->
            saveProgress(name)
            println("done: $name")
            ais.remove(name)
        }

        if (ais.isEmpty()) {
            nextAIGroup()?.let { group ->
                group.map { (description, rounds) ->
                    val name = description.toShortString()
                    println("adding: $name")
                    ais.put(name, Triple(loadNeural(name), rounds, emptyList()))
                }
            }
        }
    }

    suspend fun increment(name: String, score: Double): Unit = mutex.withLock {
        ais[name]?.let { current ->
            ais[name] = Triple(current.first, current.second + 1, current.third + score)
        }
    }

    while (true) {
        println(Clock.System.now())
        if (ais.isEmpty()) {
            val aiNames = nextAIGroup() ?: emptyList()

            if (aiNames.isEmpty()) {
                return@runBlocking
            }

            ais.putAll(
                aiNames.associate { (description, rounds) ->
                    val name = description.toShortString()
                    println("adding: $name")
                    name to Triple(loadNeural(name), rounds, emptyList())
                }
            )
        }

        (0 until repeatsPerNeural).map { round ->
            println(round)
            println(Clock.System.now())
            val winningMoves = Moves.createWinningMoves(1000).take(1000)
            val trainingMoves = Moves.createTrainingMoves(4).take(4000)

            val scores = ais.map { (name, pair) ->
                CoroutineScope(Dispatchers.Default).async {
                    val ai = pair.first

                    val battleScore = evaluateBattles(ai, battlePlayers(random))
                    val winningMoveScore = evaluateWinningMoves(ai, winningMoves)
                    val score = ((battleScore * 2.0) + winningMoveScore) / 3.0

                    if (score > 0.9) {
                        ai.store("best-" + name + "-${(score * 10000).toInt()}")
                    }

                    val training = trainingMoves + winningMoves.map { it.first }
                    ai.train(training)

                    increment(name, score)

                    name to score
                }
            }.awaitAll()

            println(scores.maxBy { it.second })
            println(scores.minBy { it.second })
            removeIfDone()
        }

        saveProgress()
    }
}

fun main() {
    Connection("jdbc:h2:file:./training/data/db", "user", "password")

    /*
    if (ResultDao.getAll().isEmpty()) {
        initialSetup()
    }

    analyzeResults()
    allResultsToJson()
    run(TrainingGroups.all)

     */

    ResultDao.insert("test", listOf(0.1, 0.2, 0.3, 0.4, 0.5))
    ResultDao.insert("test", listOf(0.6, 0.7, 0.8, 0.9, 1.0))
    println(ResultDao.getAll().filter { it.key == "test" }.size)
    ResultDao.getAll().filter { it.key == "test" }.forEach { println(it.value.size) }
    ResultDao.getAll().filter { it.key == "test" }.forEach { it.value.forEach { r -> println(r.points) }  }
}
