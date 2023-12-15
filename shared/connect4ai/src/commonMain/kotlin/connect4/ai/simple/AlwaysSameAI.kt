package connect4.ai.simple

import connect4.ai.AI
import connect4.game.Player

class AlwaysSameAI(private val always: Int, seed: Long? = null) : AI(seed) {
    override val name: String = "AlwaysSameAI($always)"

    override fun nextMove(field: List<List<Player?>>, availableColumns: List<Int>, player: Player): Int =
        if (availableColumns.contains(always)) {
            always
        } else {
            availableColumns.random(random)
        }
}
