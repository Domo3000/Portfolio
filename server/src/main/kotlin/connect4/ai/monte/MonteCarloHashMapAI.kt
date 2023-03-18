package connect4.ai.monte

import connect4.ai.AI
import connect4.game.Connect4Game
import connect4.game.Player
import connect4.game.sizeX

/*
data class HashmapCounter(val wins: Int, val losses: Int, val finishedGames: Int = 1)

class HasmapNode(private val result: Counter?, availableColumns: List<Int>) {
    val children = mutableMapOf<Int, Node?>()
    var accessed = 0

    fun isFinished(): Boolean = (result != null || children.values.all { it?.isFinished() ?: false })

    fun haveFinished(): Int = if (result != null) {
        1
    } else {
        children.values.sumOf {
            it?.haveFinished() ?: 0
        }
    }

    fun prune() {
        children.toList().maxByOrNull { accessed }?.let { (key, _) -> children[key] = null }
        //children.toList().minByOrNull { accessed }?.let { (key, _) -> children[key] = null }
        children.forEach{ it.value?.prune() }
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
                    if(result == null) {
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

class HashmapMonteCarloAI(private val maxGames: Int, private val resetAll: Boolean = false) : AI() {
    private val map = HashMap<String, HashmapCounter?>()
    private var currentMoves = ""

    override val name = "HashmapMonteCarlo($maxGames)"

    override fun reset() {
        if(resetAll) {
            map.clear()
        } else {
            //parentNode.children[parentNode.children.keys.random()] = null
        }
        currentMoves = ""
    }

    override fun updateMove(move: Int, availableColumns: List<Int>) {
        if(availableColumns.isEmpty()) {
            throw Exception("illegal") // should never happen
        }
        currentMoves += "$move"
    }

    override fun nextMove(field: List<List<Player?>>, availableColumns: List<Int>, player: Player): Int {
        if(availableColumns.size == 1) {
            return availableColumns.random()
        }

        map.g

        while (!node.isFinished() && node.children.values.filterNotNull().all { it.isFinished() || it.haveFinished() < (maxGames / availableColumns.size) }) {
            node.runRandomRecursively(Connect4Game(field, player), player)
        }

        val result = node.children.filter { availableColumns.contains(it.key) }.map { (key, value) -> key to (value?.getResult() ?: Counter(0, 0))}

        result.filter { it.second.losses == 0 }.maxByOrNull { it.second.wins }?.let {
            return it.first
        }

        return result.maxBy { it.second.wins.toDouble() / it.second.losses.toDouble() }.first
    }
}

 */