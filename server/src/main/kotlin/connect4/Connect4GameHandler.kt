package connect4

import connect4.ai.AIs
import connect4.ai.length.PlyLengthAI
import connect4.ai.monte.BalancedMonteCarloAI
import connect4.ai.neural.EvolutionHandler
import connect4.game.Connect4Game
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

object Connect4GameHandler {
    private val mutex = Mutex()

    private val evolutionHandler = EvolutionHandler()
    val simpleAIs = AIs.simpleAIs
    val mediumAIs = AIs.mediumAIs
    val hardAIs = AIs.highAIs
    val monteCarloAI = { BalancedMonteCarloAI(1001) }
    val lengthAI = { PlyLengthAI() }

    suspend fun currentScore() {
        mutex.withLock {
            evolutionHandler.currentScore()
        }
    }

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

    // TODO method to battle all Neurals, not just leastPlayed
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
            evolutionHandler.currentScore()
            evolutionHandler.storeHighest(3)
            evolutionHandler.resetBattles()
        }
    }

    suspend fun evolve() {
        mutex.withLock {
            evolutionHandler.evolve()
        }
    }

    suspend fun makeMove(game: Connect4Game): Int = mutex.withLock {
        evolutionHandler.any().ai.nextMoveRanked(game.field, game.availableColumns, game.currentPlayer)
            .maxBy { it.second }.first
    }
    /*
    suspend fun makeMove(game: Connect4Game): Int = mutex.withLock {
            evolutionHandler.highestRanking(3)
                .map {
                    it.ai.nextMoveRanked(game.field, game.availableColumns, game.currentPlayer)
                }
                .fold(Array(7) { 0.0f }) { acc, list ->
                    list.forEach {
                        acc[it.first] += it.second
                    }
                    acc
                }.mapIndexed { i, r -> i to r }.maxBy { it.second }.first
        }
    }
    */
}