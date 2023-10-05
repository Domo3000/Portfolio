package ai.monte

import ai.AI
import connect4.game.Connect4Game
import connect4.game.Player
import connect4.game.sizeX
import kotlin.random.Random

enum class Result {
    Win,
    Loss,
    Draw
}

private class Node(private val result: Result?, availableColumns: List<Int>, val parent: Node?) {
    var haveFinished: Int = 0
    val children = mutableMapOf<Int, Node?>()

    fun isFinished(): Boolean = (result != null || children.values.all { it?.isFinished() ?: false })

    fun haveFinished(): Int = haveFinished

    fun getResult(): List<Result> = result?.let { listOf(it) }
        ?: (children.values.mapNotNull { it?.getResult() }).flatten()

    fun updateFinished() {
        haveFinished++
        parent?.updateFinished()
    }

    fun runRandomRecursively(game: Connect4Game, player: Player, random: Random) {
        children.filterNot { it.value?.isFinished() ?: false }.toList().random(random).let { (c, n) ->
            if (game.availableColumns.contains(c)) {
                game.makeMove(c)

                if (n == null) {
                    children[c] = if (game.hasFinished()) {
                        updateFinished()
                        val result = game.result().second?.let { winner ->
                            if (player == winner) {
                                Result.Win
                            } else {
                                Result.Loss
                            }
                        } ?: Result.Draw
                        Node(result, emptyList(), this)
                    } else {
                        val new = Node(null, game.availableColumns, this)
                        new.runRandomRecursively(game, player, random)
                        new
                    }
                } else {
                    n.runRandomRecursively(game, player, random)
                }
            } else {
                children.remove(c)
            }
        }
    }

    init {
        availableColumns.forEach {
            children[it] = null
        }
    }
}

abstract class MonteCarloAI(private val maxGames: Int, seed: Long?) : AI(seed) {
    private var node = Node(null, (0 until sizeX).toList(), null)

    override val name = "MonteCarlo($maxGames)"

    override fun reset() {
        node = Node(null, (0 until sizeX).toList(), null)
    }

    override fun updateMove(move: Int, availableColumns: List<Int>) {
        node.children.getOrDefault(move, null)?.let {
            node = it
        } ?: run {
            node = Node(null, availableColumns, null)
        }
    }

    override fun nextMove(field: List<List<Player?>>, availableColumns: List<Int>, player: Player): Int {
        if (node.children.isEmpty() || availableColumns.size == 1) {
            return availableColumns.random(random)
        }

        while (!node.isFinished() && node.children.values.filterNotNull()
                .all { it.isFinished() || it.haveFinished() < (maxGames / availableColumns.size) }
        ) {
            node.runRandomRecursively(Connect4Game(field, player), player, random)
        }

        val result = node.children.filter { availableColumns.contains(it.key) }
            .map { (key, value) -> key to (value?.getResult() ?: emptyList()) }

        result.filter { r -> r.second.count { it == Result.Loss } == 0 }
            .maxByOrNull { r -> r.second.count { it == Result.Win } }?.let {
            return it.first
        }

        return decision(result)
    }

    abstract fun decision(results: List<Pair<Int, List<Result>>>): Int
}

class BalancedMonteCarloAI(max: Int, seed: Long?) : MonteCarloAI(max, seed) {
    override fun decision(results: List<Pair<Int, List<Result>>>): Int =
        results.maxBy { r -> r.second.count { it == Result.Win }.toDouble() / r.second.count { it == Result.Loss }.toDouble() }.first
}

class MaximizeWinsMonteCarloAI(max: Int, seed: Long?) : MonteCarloAI(max, seed) {
    override val name = "MaximizeWinsMonteCarloAI($max)"

    override fun decision(results: List<Pair<Int, List<Result>>>): Int = results.maxBy { r -> r.second.count { it == Result.Win } }.first
}

class MinimizeLossesMonteCarloAI(max: Int, seed: Long?) : MonteCarloAI(max, seed) {
    override val name = "MinimizeLossesMonteCarloAI($max)"

    override fun decision(results: List<Pair<Int, List<Result>>>): Int = results.minBy { r -> r.second.count { it == Result.Loss } }.first
}