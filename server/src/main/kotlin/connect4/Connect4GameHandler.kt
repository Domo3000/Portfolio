package connect4

import connect4.ai.AIs
import connect4.ai.length.PlyLengthAI
import connect4.ai.monte.BalancedMonteCarloAI
import connect4.ai.monte.MonteCarloAI
import connect4.ai.neural.EvolutionHandler
import connect4.game.Connect4Game
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Instant

object Connect4GameHandler {
    private val mutex = Mutex()

    private val evolutionHandler = EvolutionHandler(1)
    val simpleAIs = AIs.simpleAIs
    val mediumAIs = AIs.mediumAIs
    val hardAIs = AIs.highAIs
    val monteCarloAI = { BalancedMonteCarloAI(1001) }
    val lengthAI = { PlyLengthAI() }

    suspend fun battleMedium() {
        mutex.withLock {
            evolutionHandler.battle(mediumAIs)
        }
    }

    suspend fun battleHard() {
        mutex.withLock {
            evolutionHandler.battle(hardAIs)
        }
    }

    suspend fun battle() {
        mutex.withLock {
            evolutionHandler.battle(mediumAIs + hardAIs)
        }
    }

    suspend fun train() {
        mutex.withLock {
            evolutionHandler.train()
        }
    }

    suspend fun trainAll() {
        mutex.withLock {
            evolutionHandler.train(evolutionHandler.allNeurals())
        }
    }

    suspend fun evaluate() {
        mutex.withLock {
            evolutionHandler.evaluate()
        }
    }

    suspend fun storeHighest() {
        mutex.withLock {
            evolutionHandler.storeHighest(3)
            evolutionHandler.resetBattles(printAll = true, printHighest = true)
        }
    }

    suspend fun evolve() {
        mutex.withLock {
            evolutionHandler.evolve()
        }
    }

    suspend fun makeMove(game: Connect4Game): Int = mutex.withLock {
        evolutionHandler.highestRanking(3)
            .map { it.ai.nextMoveRanked(game.field, game.availableColumns, game.currentPlayer) }
            .fold(Array(7) { 0.0f }) { acc, list ->
                list.forEach {
                    acc[it.first] += it.second
                }
                acc
            }.mapIndexed { i, r -> i to r}.maxBy { it.second }.first
    }

    init {
        /*
        println("Starting first battles at ${Instant.now()}")
        repeat(20) {
            evolutionHandler.battle(AIs.highAIs + AIs.mediumAIs)
        }
        repeat(10) {
            println("Starting $it training at ${Instant.now()}")
            evolutionHandler.train(evolutionHandler.allNeurals())
        }
         */
        /*
        println("Starting first battles at ${Instant.now()}")
        repeat(evolutionHandler.allNeurals().size * 5) {
            evolutionHandler.battle(AIs.highAIs + AIs.mediumAIs)
        }

         */
        println("GameHandler started at ${Instant.now()}")
    }
}