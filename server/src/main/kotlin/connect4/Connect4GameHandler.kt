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

    suspend fun makeMove(game: Connect4Game): Int = mutex.withLock {
        return when (random.nextInt(0, 3)) {
            0 -> storedHandler.allNeurals().random()
                .nextMoveRanked(game.field, game.availableColumns, game.currentPlayer)
                .maxBy { it.second }.first

            1 -> OverallHighestAI(storedHandler.allNeurals().filter {
                it.name.contains(
                    Regex(
                        when (random.nextInt(0, 3)) {
                            0 -> "[012]"
                            1 -> "[134]"
                            else -> "[245]"
                        }
                    )
                )
            }).nextMove(game.field, game.availableColumns, game.currentPlayer)

            else -> MostCommonAI(storedHandler.allNeurals().filter {
                it.name.contains(
                    Regex(
                        when (random.nextInt(0, 3)) {
                            0 -> "[236]"
                            1 -> "[167]"
                            else -> "[678]"
                        }
                    )
                )
            }).nextMove(game.field, game.availableColumns, game.currentPlayer)
        }
    }
}