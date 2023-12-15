package connect4.ai.simple

import connect4.ai.AI
import connect4.game.Player

class RandomAI(seed: Long? = null) : AI(seed) {
    override val name: String = javaClass.simpleName
    override fun nextMove(field: List<List<Player?>>, availableColumns: List<Int>, player: Player): Int =
        availableColumns.random(random)
}

class BiasedRandomAI(seed: Long? = null) : AI(seed) {
    override val name: String = javaClass.simpleName

    override fun nextMove(field: List<List<Player?>>, availableColumns: List<Int>, player: Player): Int {
        if (availableColumns.size < 3) {
            return availableColumns.random(random)
        }

        val mid = availableColumns.size / 2
        val offsetDir = if (random.nextBoolean()) 1 else -1
        val offset = when(random.nextBoolean()) {
            true -> 0
            false -> when(random.nextBoolean()) {
                true -> 1
                false -> (random.nextInt() % (mid + 1))
            }
        }  * offsetDir

        return availableColumns.sorted().getOrNull(mid + offset) ?: nextMove(field, availableColumns, player)
    }
}