package connect4.game

abstract class Connect4Player {
    abstract val name: String
    abstract fun nextMove(field: List<List<Player?>>, availableColumns: List<Int>, player: Player): Int //TODO nextMove(game, player)?

    open fun updateMove(move: Int, availableColumns: List<Int>) = Unit
    open fun reset() = Unit
}