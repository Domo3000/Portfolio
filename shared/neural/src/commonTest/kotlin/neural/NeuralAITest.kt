package neural

import ai.AI
import ai.length.AggressiveLengthAI
import ai.length.BalancedLengthAI
import ai.length.DefensiveLengthAI
import ai.length.SimpleLengthAI
import connect4.messages.Activation
import connect4.messages.LayerDescription
import connect4.messages.LayerSize
import connect4.messages.NeuralDescription
import kotlinx.coroutines.*
import org.junit.Test
import java.time.Instant
import kotlin.random.Random
import kotlin.time.measureTime

private fun trainingPlayers(random: Random) = listOf(
    { AggressiveLengthAI(false, random.nextLong()) },
    { DefensiveLengthAI(false, random.nextLong()) },
    { BalancedLengthAI(false, random.nextLong()) },
    { SimpleLengthAI(false, random.nextLong()) },
    { AggressiveLengthAI(true, random.nextLong()) },
    { DefensiveLengthAI(true, random.nextLong()) },
    { BalancedLengthAI(true, random.nextLong()) },
    { SimpleLengthAI(true, random.nextLong()) }
)

private fun getTrainingMovesParallel(trainingPlayers: List<() -> AI>, parallelism: Int = 10) = runBlocking {
    val moves = (0..parallelism).map {
        CoroutineScope(Dispatchers.Default).async {
            getTrainingMoves(trainingPlayers)
        }
    }.awaitAll()

    moves
}.flatten().shuffled()

class NeuralAITest {
    private val random = Random(Instant.now().toEpochMilli())

    @Test
    fun trainingTime() {
        val trainingsMoves = getTrainingMovesParallel(trainingPlayers(random)).take(5000)
        println(trainingsMoves.size)

        LayerSize.entries.forEach { convSize ->
            LayerSize.entries.forEach { denseSize ->
                val description = NeuralDescription(
                    LayerDescription(convSize, Activation.Relu),
                    LayerDescription(denseSize, Activation.Relu),
                )
                val ai = NeuralBuilder.build(
                    description.conv,
                    description.dense,
                    random
                )

                println("${description.toShortString()}: ${ai.info()}")

                val times = (0..4).map {
                    measureTime {
                        ai.train(trainingsMoves)
                    }
                }.map { it.inWholeMilliseconds }

                println(times.min() / 1000.0)
                println(times.average() / 1000.0)
                println(times.max() / 1000.0)
            }
        }
    }


}
