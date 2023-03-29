package connect4.ai

import connect4.game.Connect4Game
import connect4.game.Player

class BattleCounter(
    val ai: AI,
    var gamesPlayed: Int = 0,
    var gamesWon: Int = 0
)

class BattleHandler(players: List<AI>) {
    private val counters = players.map { BattleCounter(it) }

    private fun leastPlayed(): BattleCounter =
        counters.minBy { it.gamesPlayed }

    private fun handleResult(counter: BattleCounter, player: Player, winner: Player?) {
        counter.gamesPlayed++
        if (player == winner) {
            counter.gamesWon++
        }
    }

    private fun fight(counter: BattleCounter, opponent: AI) {
        val resultP1 = Connect4Game.runGame(counter.ai, opponent)
        handleResult(counter, Player.FirstPlayer, resultP1.first)
        val resultP2 = Connect4Game.runGame(opponent, counter.ai)
        handleResult(counter, Player.SecondPlayer, resultP2.first)
    }

    fun battleLeastPlayed(
        opponents: List<AI> = AIs.highAIs.map { it() }
    ) {
        opponents.forEach { opponent ->
            fight(leastPlayed(), opponent)
        }
    }

    fun battle(
        opponents: List<AI> = AIs.highAIs.map { it() }
    ) {
        opponents.forEach { opponent ->
            counters.forEach { counter ->
                fight(counter, opponent)
            }
        }
    }

    fun currentScore(printAll: Boolean = true, printHighest: Boolean = true) {
        if (printAll) {
            println("Score")
            counters.forEach {
                println("${it.gamesWon}/${it.gamesPlayed}: ${it.ai.name}")
            }
        }
        if (printHighest) {
            println("Highest:")
            counters.maxBy { it.gamesWon }.let {
                println("${it.gamesWon}/${it.gamesPlayed}: ${it.ai.name}")
            }
        }
    }

    fun resetBattles() {
        counters.forEach {
            it.gamesWon = 0
            it.gamesPlayed = 0
        }
    }

    fun highest(): AI = counters.maxBy { it.gamesWon }.ai
}