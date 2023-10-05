package ai

import connect4.game.Connect4Game
import connect4.game.Player
import kotlinx.coroutines.*

class BattleCounter(
    val ai: AI,
    var maxScore: Int = 0,
    var score: Int = 0
)

class BattleHandler(players: List<AI>) {
    companion object {
        private fun handleResult(counter: BattleCounter, player: Player, winner: Player?, score: Int) {
            counter.maxScore += score
            if (player == winner) {
                counter.score += score
            }
        }

        fun fight(counter: BattleCounter, opponent: () -> AI, score: Int = 1) {
            val resultP1 = Connect4Game.runGame(counter.ai, opponent())
            handleResult(counter, Player.FirstPlayer, resultP1.first, score)
            val resultP2 = Connect4Game.runGame(opponent(), counter.ai)
            handleResult(counter, Player.SecondPlayer, resultP2.first, score)
        }
    }

    val counters = players.map { BattleCounter(it) }.toMutableList()

    fun battleScored(
        players: List<Triple<() -> AI, Int, Int>>,
        ai: BattleCounter
    ) {
        players.forEach { (opponent, score, repeat) ->
            repeat(repeat) {
                fight(ai, opponent, score)
            }
        }
    }

    fun singleBattle(
        opponent: () -> AI,
        ai: BattleCounter
    ) {
        battleScored(listOf(Triple(opponent, 1, 1)), ai)
    }

    fun singleOpponentBattle(
        opponent: () -> AI
    ) {
        counters.forEach { counter ->
            fight(counter, opponent, 1)
        }
    }

    fun chunkedSingleOpponentBattle(
        opponent: () -> AI,
        repeat: Int,
        chunkSize: Int,
        callback: (AI) -> Unit = {}
    ) {
        runBlocking {
            counters.shuffled().chunked(chunkSize).map { aiChunk ->
                aiChunk.map { counter ->
                    CoroutineScope(Dispatchers.Default).async {
                        repeat(repeat) {
                            fight(counter, opponent, 1)
                        }
                    }
                }.awaitAll()
                callback(aiChunk.maxBy { it.score }.ai)
            }
        }
    }

    fun currentScore(printAll: Boolean = true, printHighest: Boolean = true) {
        if (printAll) {
            println("Score")
            counters.sortedByDescending { it.score }.forEach {
                println("${it.score}/${it.maxScore}: ${it.ai.name}")
            }
        }
        if (printHighest) {
            println("Highest:")
            counters.maxBy { it.score }.let {
                println("${it.score}/${it.maxScore}: ${it.ai.name}")
            }
        }
    }

    fun resetBattles() {
        counters.forEach {
            it.score = 0
            it.maxScore = 0
        }
    }

    fun highest(): BattleCounter = counters.maxBy { it.score }

    fun highestRanking(amount: Int): List<AI> = counters.sortedByDescending { it.score }.map { it.ai }.take(amount)

    fun purgeWeakest() {
        counters.sortByDescending { it.score }
        counters.removeLastOrNull()
    }

    fun sortedScores() {
        val scores = mutableMapOf<AI, Double>()

        counters.map { c ->
            c.ai to c.score.toDouble() / c.maxScore.toDouble()
        }.sortedByDescending { it.second }
    }
}