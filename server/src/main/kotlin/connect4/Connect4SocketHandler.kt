package connect4

import connect4.ai.AI
import connect4.game.Connect4Game
import connect4.game.Player
import connect4.messages.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import sockets.Connection
import utils.logError
import utils.logInfo

private fun List<() -> AI>.invokeHighestVoted(game: Connect4Game) =
    map { Connect4Game(game.field, game.currentPlayer).makeMove(it()) }
        .groupBy { it }.map { (choice, list) -> choice to list.size }
        .maxBy { it.second }.first

context(Connect4ConnectionHandler, Connect4GameHandler)
        suspend fun makeMove(connection: Connection, game: Connect4Game, choice: AIChoice?) {
    val next = when (choice) {
        AIChoice.Simple -> simpleAIs.invokeHighestVoted(game)
        AIChoice.Medium -> mediumAIs.invokeHighestVoted(game)
        AIChoice.Hard -> hardAIs.invokeHighestVoted(game)
        AIChoice.MonteCarlo -> listOf(monteCarloAI).invokeHighestVoted(game)
        AIChoice.Length -> listOf(lengthAI).invokeHighestVoted(game)
        AIChoice.Neural -> makeMove(game)
        null -> makeMove(game)
    }

    game.makeMove(next)

    replyTo(connection.id, NextMoveMessage(next))
    replyTo(connection.id, ContinueMessage)
    checkFinished(connection, game)
}


context(Connect4ConnectionHandler)
        suspend fun checkFinished(connection: Connection, game: Connect4Game): Boolean {
    val result = game.result()
    return if (result.first) {
        replyTo(connection.id, GameFinishedMessage(result.second))
        true
    } else {
        false
    }
}

// TODO why does PROD make illegal moves? check if game per connection
context(Connect4ConnectionHandler, Connect4GameHandler, ApplicationEnvironment)
fun Routing.createConnect4Websocket() = webSocket("/connect4") {
    val connection = Connection(this)
    var game: Connect4Game? = null
    var ai: AIChoice? = null
    var player: Player? = null
    add(connection)
    send(ConnectedMessage(connection.id).encode())
    logInfo("connected $connection")

    try {
        for (frame in incoming) {
            connection.timeStamp = System.currentTimeMillis()
            when (frame) {
                is Frame.Close -> { // TODO test again why this doesn't get received
                    frame.readReason()?.let { logInfo(it.toString()) }
                    remove(connection)
                    throw ClosedReceiveChannelException("removed")
                }

                is Frame.Text -> {
                    when (val message = Connect4Messages.decode(frame.readText())) {
                        is PickedAIMessage -> {
                            ai = message.ai
                            replyTo(connection.id, PickedAIMessage(message.ai))
                        }

                        is PickedPlayerMessage -> {
                            player = message.player.switch()
                            replyTo(connection.id, PickedPlayerMessage(message.player))
                        }

                        is NextMoveMessage -> {
                            game?.let { g ->
                                if (g.availableColumns.contains(message.column)) {
                                    g.makeMove(message.column)
                                    replyTo(connection.id, NextMoveMessage(message.column))
                                    replyTo(connection.id, WaitMessage)
                                    flush(connection.id)

                                    if (!checkFinished(connection, g)) {
                                        makeMove(connection, g, ai)
                                    }

                                    if (checkFinished(connection, g)) {
                                        game = null
                                    }
                                    Unit
                                } else {
                                    replyTo(connection.id, ContinueMessage)
                                }
                            }
                        }

                        is GameStartedMessage -> {
                            game = Connect4Game()
                            replyTo(connection.id, GameStartedMessage(player!!.switch()))
                            if (player == Player.FirstPlayer) {
                                replyTo(connection.id, WaitMessage)
                                makeMove(connection, game!!, ai)
                            } else {
                                replyTo(connection.id, ContinueMessage)
                            }
                        }

                        else -> Unit
                    }
                }

                else -> {
                    //logInfo(frame.toString())
                }
            }
        }
    } catch (e: Throwable) {
        remove(connection)
        logError(e) // TODO maybe no cleanup needed. test this in more detail
        logInfo("$this disconnected")
    }
}
