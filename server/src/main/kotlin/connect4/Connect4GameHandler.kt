package connect4

import connect4.ai.AIs
import connect4.ai.length.PlyLengthAI
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
    val monteCarloAI = { BalancedMonteCarloAI(1001) }
    val lengthAI = { PlyLengthAI() }

    suspend fun loadStored() {
        mutex.withLock {
            storedHandler.loadStored()
        }
    }

    val allNeurals by lazy { storedHandler.allNeurals() }

    val overallHighestAis by lazy {
        listOf(Regex("[012]"), Regex("[134]"), Regex("[245]")).map { r ->
            OverallHighestAI(storedHandler.allNeurals().filter { it.name.contains(r) } )
        }
    }

    val mostCommonAis by lazy {
        listOf(Regex("[167]"), Regex("[236]"), Regex("[678]")).map { r ->
            MostCommonAI(storedHandler.allNeurals().filter { it.name.contains(r) } )
        }
    }

    suspend fun makeMove(game: Connect4Game): Int = mutex.withLock {
        return when (random.nextInt(0, 3)) {
            0 -> allNeurals.random()
                .nextMoveRanked(game.field, game.availableColumns, game.currentPlayer)
                .maxBy { it.second }.first

            1 -> overallHighestAis.random().nextMove(game.field, game.availableColumns, game.currentPlayer)

            else -> mostCommonAis.random().nextMove(game.field, game.availableColumns, game.currentPlayer)
        }
    }
}