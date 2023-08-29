package connect4.game

import kotlin.test.Test
import kotlin.test.assertEquals

// TODO update
class Connect4GameTest {
    @Test
    fun print() {
        val game = Connect4Game()
        assertEquals(
            """|| | | |
               || | | |
               || | | |
            """.trimMargin(),
            game.toString()
        )
    }

    @Test
    fun availableColumns() {
        val game = Connect4Game()
        (0..1).forEach {
            assertEquals(
                listOf(0, 1),
                game.availableColumns
            )
            game.makeMove(0)
        }
        (0..1).forEach {
            assertEquals(
                listOf(1),
                game.availableColumns
            )
            game.makeMove(1)
        }
        assertEquals(
            emptyList(),
            game.availableColumns
        )
    }

    @Test
    fun makeMove() {
        val game = Connect4Game()
        game.makeMove(0)
        game.makeMove(0)
        game.makeMove(0)
        game.makeMove(1)
        game.makeMove(1)
        assertEquals(
            """||O| | |
               ||X|O| |
               ||O|X| |
            """.trimMargin(),
            game.toString()
        )
    }

    @Test
    fun hasFinished() {
        val game = Connect4Game()
        game.makeMove(0)
        game.makeMove(1)
        game.makeMove(0)
        game.makeMove(1)
        game.makeMove(0)
        game.makeMove(1)
        game.makeMove(0)
        assertEquals(
            true to Player.SecondPlayer,
            game.result()
        )
    }

    @Test
    fun hasFinishedDraw() {
        val game = Connect4Game()
        assertEquals(
            false to null,
            game.result()
        )
        game.makeMove(0)
        game.makeMove(0)
        game.makeMove(1)
        game.makeMove(1)
        assertEquals(
            true to null,
            game.result()
        )
    }
}