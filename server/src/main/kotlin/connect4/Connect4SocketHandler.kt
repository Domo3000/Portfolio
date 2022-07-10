package connect4

import connect4.messages.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import sockets.Connection
import utils.logInfo

context(Connect4, ApplicationEnvironment)
fun Routing.createConnect4Websocket() = run {
    webSocket("/connect4") {
        val connection = Connection(this)
        add(connection)
        send(ConnectedMessage(connection.id).encode())
        logInfo("connected $connection")

        try {
            for (frame in incoming) {
                when (frame) {
                    is Frame.Close -> {
                        remove(connection)
                        throw ClosedReceiveChannelException("removed")
                    }
                    is Frame.Text -> {
                        when (val message = Connect4Messages.decode(frame.readText())) {
                            is NextMoveMessage -> logInfo("nextMove $message")
                            else -> Unit
                        }
                        // TODO handle game logic
                        broadCast(NextMoveMessage(0, 0).encode())
                    }
                    else -> {}
                }
            }
        } catch (e: Throwable) {
            logInfo("$this disconnected")
        }
    }
}