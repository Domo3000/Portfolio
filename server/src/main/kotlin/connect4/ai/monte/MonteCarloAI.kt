package connect4.ai.monte

import connect4.ai.AI
import connect4.game.Connect4Game
import connect4.game.Player
import connect4.game.sizeX
import kotlin.random.Random

data class Counter(val wins: Int, val losses: Int, val finishedGames: Int = 1)

class Node(private val result: Counter?, availableColumns: List<Int>) {
    val children = mutableMapOf<Int, Node?>()

    fun isFinished(): Boolean = (result != null || children.values.all { it?.isFinished() ?: false })

    fun haveFinished(): Int = if (result != null) {
        1
    } else {
        children.values.sumOf {
            it?.haveFinished() ?: 0
        }
    }

    fun prune(random: Random) {
        val pruned = children.map { (k, v) ->
            if (random.nextBoolean()) {
                k to null
            } else {
                v?.prune(random)
                k to v
            }
        }
        children.clear()
        children.putAll(pruned)
    }

    fun getResult(): Counter = result
        ?: (children.values.mapNotNull { it?.getResult() }
            .reduceOrNull { acc, counter -> Counter(acc.wins + counter.wins, acc.losses + counter.losses) }
            ?: Counter(0, 0))

    fun runRandomRecursively(game: Connect4Game, player: Player) {
        children.filterNot { it.value?.isFinished() ?: false }.toList().random().let { (c, n) ->
            if (game.availableColumns.contains(c)) {
                game.makeMove(c)

                if (n == null) {
                    val result = if (game.hasFinished()) {
                        game.result().second?.let { winner ->
                            if (player == winner) {
                                Counter(1, 0)
                            } else {
                                Counter(0, 1)
                            }
                        } ?: Counter(0, 0)
                    } else {
                        null
                    }
                    val new = Node(result, game.availableColumns)
                    if (result == null) {
                        new.runRandomRecursively(game, player)
                    }
                    children[c] = new
                } else {
                    n.runRandomRecursively(game, player)
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

abstract class MonteCarloAI(private val maxGames: Int) : AI() {
    private var node = Node(null, (0 until sizeX).toList())

    override val name = "MonteCarlo($maxGames)"

    override fun reset() {
        node = Node(null, (0 until sizeX).toList())
    }

    override fun updateMove(move: Int, availableColumns: List<Int>) {
        node.children.getOrDefault(move, null)?.let {
            node = it
        } ?: run {
            val new = Node(null, availableColumns)
            node.children[move] = new
            node = new
        }
    }

    override fun nextMove(field: List<List<Player?>>, availableColumns: List<Int>, player: Player): Int {
        if (node.children.isEmpty() || availableColumns.size == 1) {
            return availableColumns.random()
        }

        while (!node.isFinished() && node.children.values.filterNotNull()
                .all { it.isFinished() || it.haveFinished() < (maxGames / availableColumns.size) }
        ) {
            node.runRandomRecursively(Connect4Game(field, player), player)
        }

        val result = node.children.filter { availableColumns.contains(it.key) }
            .map { (key, value) -> key to (value?.getResult() ?: Counter(0, 0)) }

        result.filter { it.second.losses == 0 }.maxByOrNull { it.second.wins }?.let {
            return it.first
        }

        return decision(result)
    }

    abstract fun decision(results: List<Pair<Int, Counter>>): Int
}

class BalancedMonteCarloAI(max: Int) : MonteCarloAI(max) {
    override fun decision(results: List<Pair<Int, Counter>>): Int =
        results.maxBy { it.second.wins.toDouble() / it.second.losses.toDouble() }.first
}

class MaximizeWinsMonteCarloAI(max: Int) : MonteCarloAI(max) {
    override val name = "MaximizeWinsMonteCarloAI($max)"

    override fun decision(results: List<Pair<Int, Counter>>): Int = results.maxBy { it.second.wins.toDouble() }.first
}

class MinimizeLossesMonteCarloAI(max: Int) : MonteCarloAI(max) {
    override val name = "MinimizeLossesMonteCarloAI($max)"

    override fun decision(results: List<Pair<Int, Counter>>): Int = results.minBy { it.second.losses.toDouble() }.first
}