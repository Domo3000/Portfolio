package connect4

import connect4.ai.AIs
import connect4.ai.length.BalancedLengthAI
import connect4.ai.monte.BalancedMonteCarloAI
import connect4.ai.neural.MostCommonAI
import connect4.ai.neural.OverallHighestAI
import connect4.ai.neural.StoredHandler
import connect4.game.Connect4Game
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Instant
import kotlin.random.Random

object Connect4GameHandler {
    private val mutex = Mutex()
    private val random = Random(Instant.now().toEpochMilli())

    private val storedHandler = StoredHandler()
    val simpleAIs = AIs.simpleAIs
    val mediumAIs = AIs.mediumAIs
    val hardAIs = AIs.highAIs
    val monteCarloAI = { BalancedMonteCarloAI(1001, random.nextLong()) }
    val lengthAI = { BalancedLengthAI(true, random.nextLong()) }

    suspend fun loadStored() {
        mutex.withLock {
            storedHandler.loadStoredNeurals()
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