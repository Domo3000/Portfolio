package connect4.game

private typealias Position = Pair<Int, Int>

const val sizeX = 7
const val sizeY = 6

enum class Player {
    FirstPlayer,
    SecondPlayer;

    fun switch() = when (this) {
        FirstPlayer -> SecondPlayer
        SecondPlayer -> FirstPlayer
    }
}

class Connect4Game(initialField: List<List<Player?>>? = null, initialPlayer: Player? = null) {
    var currentPlayer: Player = initialPlayer ?: Player.FirstPlayer
    val moves = mutableListOf<Int>()

    val field = initialField?.map { it.toMutableList() } ?: Array(sizeY) {
        Array<Player?>(sizeX) {
            null
        }.toMutableList()
    }.toList()

    val availableColumns
        get() = field[0].mapIndexedNotNull { index, value -> if (value == null) index else null }

    fun makeMove(column: Int) {
        if (!availableColumns.contains(column)) {
            println(currentPlayer)
            println(toString())
            throw Exception("illegal move $column")
        }

        (0 until sizeY).findLast { field[it][column] == null }
            ?.let {
                field[it][column] = currentPlayer
                moves += column
                currentPlayer = currentPlayer.switch()
            }
    }

    fun makeMove(player: Connect4Player): Int {
        val move = player.nextMove(field, availableColumns, currentPlayer)
        makeMove(move)
        return move
    }

    private fun checkRecursively(element: Position, list: List<Position>, offset: Position): Int =
        list.find { it.first == element.first + offset.first && it.second == element.second + offset.second }
            ?.let { next -> 1 + checkRecursively(next, list.filterNot { it == next }, offset) }
            ?: 0

    fun result(): Pair<Boolean, Player?> {
        if (availableColumns.isEmpty()) {
            return true to null
        }

        return field.asSequence().mapIndexed { y, row ->
            row.mapIndexedNotNull { x, element ->
                element?.let {
                    Position(x, y) to it
                }
            }
        }.flatten().groupBy { it.second }.mapNotNull { (player, elements) ->
            elements.mapNotNull { (position, _) ->
                (0..3).maxOfOrNull { direction ->
                    val offsetX = if (direction == 0) 0 else 1
                    val offsetY = when (direction) {
                        2 -> 0
                        3 -> 1
                        else -> -1
                    }

                    val newList = elements.map { it.first }

                    1 + checkRecursively(
                        position,
                        newList,
                        Position(offsetX, offsetY)
                    ) + checkRecursively(
                        position,
                        newList,
                        Position(-offsetX, -offsetY)
                    )
                }?.let { it to player }
            }.find { it.first >= 4 }?.let {
                true to it.second
            }
        }.firstOrNull() ?: (false to null)
    }

    fun hasFinished(): Boolean = result().first

    override fun toString(): String = field.joinToString("\n") { row ->
        row.joinToString("|", "|", "|") {
            when (it) {
                Player.FirstPlayer -> "X"
                Player.SecondPlayer -> "O"
                else -> " "
            }
        }
    }

    companion object {
        fun runGame(
            firstPlayer: Connect4Player,
            secondPlayer: Connect4Player,
            printResult: Boolean = false,
            printField: Boolean = false
        ): Triple<Player?, List<Int>, List<List<Player?>>> {
            val game = Connect4Game()
            firstPlayer.reset()
            secondPlayer.reset()

            while (!game.hasFinished()) {
                val firstMove = game.makeMove(firstPlayer)
                if (printField) {
                    println(game.toString())
                }
                firstPlayer.updateMove(firstMove, game.availableColumns)
                secondPlayer.updateMove(firstMove, game.availableColumns)
                if (!game.hasFinished()) {
                    val secondMove = game.makeMove(secondPlayer)
                    if (printField) {
                        println(game.toString())
                    }
                    if (!game.hasFinished()) {
                        firstPlayer.updateMove(secondMove, game.availableColumns)
                        secondPlayer.updateMove(secondMove, game.availableColumns)
                    }
                }
            }

            val result = game.result()

            if (printResult) {
                println("Result: $result")
                println(game.toString())
            }

            return Triple(game.result().second, game.moves.toList(), game.field.map { it.toList() })
        }
    }
}