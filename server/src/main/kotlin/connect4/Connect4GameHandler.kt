package connect4

import ai.length.AggressiveLengthAI
import ai.length.BalancedLengthAI
import ai.length.DefensiveLengthAI
import ai.length.SimpleLengthAI
import ai.monte.BalancedMonteCarloAI
import ai.monte.MaximizeWinsMonteCarloAI
import ai.monte.MinimizeLossesMonteCarloAI
import ai.simple.BiasedRandomAI
import connect4.game.Connect4Game
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import neural.MostCommonAI
import neural.OverallHighestAI
import neural.StoredHandler
import java.time.Instant
import kotlin.random.Random

object Connect4GameHandler {
    private val mutex = Mutex()
    private val random = Random(Instant.now().toEpochMilli())

    private val storedHandler = StoredHandler()
    val simpleAIs = listOf(
        { BiasedRandomAI(random.nextLong()) },
        { DefensiveLengthAI(false, random.nextLong()) },
        { MinimizeLossesMonteCarloAI(300, random.nextLong()) }
    )
    val mediumAIs = listOf(
        { AggressiveLengthAI(false, random.nextLong()) },
        { SimpleLengthAI(false, random.nextLong()) },
        { MaximizeWinsMonteCarloAI(500, random.nextLong()) }
    )
    val hardAIs = listOf(
        { BalancedLengthAI(true, random.nextLong()) },
        { SimpleLengthAI(true, random.nextLong()) },
        { BalancedMonteCarloAI(800, random.nextLong()) }
    )
    val monteCarloAI = { BalancedMonteCarloAI(1001, random.nextLong()) }
    val lengthAI = { BalancedLengthAI(true, random.nextLong()) }

    suspend fun loadStored() {
        mutex.withLock {
            storedHandler.loadStoredNeurals((1..7).map { "$it" }.toList())
        }
    }

    private val allNeurals by lazy { storedHandler.allNeurals() }

    suspend fun makeMove(game: Connect4Game): Int = mutex.withLock {
        val ai = when (random.nextInt(0, 5)) {
            0 -> allNeurals.random(random)

            in 1..2 -> OverallHighestAI(allNeurals.shuffled(random).take(3))

            else -> MostCommonAI(allNeurals.shuffled(random).take(3))
        }

        return ai.nextMove(game)
    }
}