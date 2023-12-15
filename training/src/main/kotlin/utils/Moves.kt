package utils

import connect4.ai.AI
import connect4.game.Connect4Game
import connect4.game.Player
import kotlinx.coroutines.*
import kotlinx.datetime.Clock
import neural.Move
import kotlin.random.Random

private fun List<Move>.repeatLastMoves(): List<Move> {
    return if (size >= 4) {
        this + takeLast(size / 4).repeatLastMoves()
    } else {
        this
    }
}

private fun allPossibleWinningMoves(game: Connect4Game): List<Move> = game.availableColumns.map { column ->
    val maybeWinningGame = Connect4Game(game.field, game.currentPlayer)
    maybeWinningGame.makeMove(column)
    if (maybeWinningGame.hasFinished() && maybeWinningGame.result().second != null) {
        Move(game.field.map { it.toList() }, column)
    } else {
        null
    }
}.filterNotNull()

private fun createMoves(firstPlayer: AI, secondPlayer: AI, count: Int = 100): List<Pair<Move, Player>> {
    val moves = mutableListOf<Pair<Move, Player>>()

    while (moves.size < count) {
        val game = Connect4Game()
        firstPlayer.reset()
        secondPlayer.reset()

        while (!game.hasFinished()) {
            moves.addAll(allPossibleWinningMoves(game).map { it to Player.FirstPlayer })
            val firstMove = game.makeMove(firstPlayer)
            firstPlayer.updateMove(firstMove, game.availableColumns)
            secondPlayer.updateMove(firstMove, game.availableColumns)
            if (!game.hasFinished()) {
                moves.addAll(allPossibleWinningMoves(game).map { it to Player.SecondPlayer })
                val secondMove = game.makeMove(secondPlayer)
                if (!game.hasFinished()) {
                    firstPlayer.updateMove(secondMove, game.availableColumns)
                    secondPlayer.updateMove(secondMove, game.availableColumns)
                }
            }
        }
    }

    return moves.toList()
}

fun createWinningMoves(firstPlayer: AI, secondPlayer: AI, count: Int = 100) =
    createMoves(firstPlayer, secondPlayer, count / 2) + createMoves(secondPlayer, firstPlayer, count / 2)

fun getTrainingMoves(players: List<() -> AI>): List<Move> {
    val winningMoves = players.map { p1 ->
        players.mapNotNull { p2 ->
            val result = Connect4Game.runGame(p1(), p2())
            if (result.first != null) {
                result.second to result.first
            } else {
                null
            }
        }
    }.flatten()

    return winningMoves.map { (moves, player) ->
        val game = Connect4Game()

        moves.map { m ->
            val move = Move(game.field.map { it.toList() }, m)

            val playerMoves = if (game.currentPlayer == player) {
                listOf(move)
            } else {
                emptyList()
            }

            val allWinningMoves = allPossibleWinningMoves(game)

            game.makeMove(m)

            playerMoves + allWinningMoves
        }.flatten().repeatLastMoves()
    }.flatten()
}

object Moves {
    val random = Random(Clock.System.now().toEpochMilliseconds())

    fun createWinningMoves(count: Int): List<Pair<Move, Player>> {
        val firstPlayers = battlePlayers(random)

        return firstPlayers.flatMap { firstPlayer ->
            battlePlayers(random).flatMap { secondPlayer ->
                createWinningMoves(firstPlayer(), secondPlayer(), count / (firstPlayers.size * firstPlayers.size))
            }
        }
    }

    fun createTrainingMoves(parallelism: Int): List<Move> = runBlocking {
        val moves = (0..parallelism).map {
            CoroutineScope(Dispatchers.Default).async {
                getTrainingMoves(trainingPlayers(random))
            }
        }.awaitAll()

        moves
    }.flatten().shuffled(random)
}
