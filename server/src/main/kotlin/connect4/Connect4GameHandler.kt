package connect4

import connect4.ai.AIs
import connect4.ai.length.PlyLengthAI
import connect4.ai.monte.BalancedMonteCarloAI
import connect4.ai.neural.StoredHandler
import connect4.game.Connect4Game
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

object Connect4GameHandler {
    private val mutex = Mutex()

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

    suspend fun loadStored(names: List<String>) {
        mutex.withLock {
            storedHandler.loadStored(names)
        }
    }

    suspend fun makeMove(game: Connect4Game): Int = mutex.withLock {
        /*
        storedHandler.allNeurals().forEach {
            val move = it.nextMove(game.field, game.availableColumns, game.currentPlayer)
            println("$move: ${it.info()}")
        }

        val mostCommonFirstChoice = storedHandler.allNeurals()
            .map {
                it.nextMoveRanked(game.field, game.availableColumns, game.currentPlayer)
            }.map { list ->
                list.maxBy { it.second }.first
            }.groupingBy { it }.eachCount().maxBy { it.value }.key

        println(mostCommonFirstChoice)

        val highestOverall = storedHandler.allNeurals()
            .map {
                it.nextMoveRanked(game.field, game.availableColumns, game.currentPlayer)
            }
            .fold(Array(7) { 0.0f }) { acc, list ->
                list.forEach {
                    acc[it.first] += it.second
                }
                acc
            }.mapIndexed { i, r -> i to r }.maxBy { it.second }.first

        println(highestOverall)

         */

        storedHandler.allNeurals().random().nextMoveRanked(game.field, game.availableColumns, game.currentPlayer)
            .maxBy { it.second }.first
    }
}