package connect4

import connect4.messages.*
import io.ktor.websocket.*
import sockets.Connection

object Connect4 {
    private val connections = mutableSetOf<Connection>()

    fun add(connection: Connection) =
        synchronized(connections) { connections += connection }

    fun remove(connection: Connection) =
        synchronized(connections) { connections.removeIf { it.session == connection.session } }

    fun cleanUp(now: Long = System.currentTimeMillis()): Unit =
        synchronized(connections) { connections.removeAll { it.timeStamp < now - 1000 * 60 * 10 } }

    suspend fun replyTo(id: Int, message: Connect4Message) =
        connections.find { it.id == id }?.session?.send(message.encode())

    suspend fun broadCast(message: String) = connections.forEach { it.session.send(message) }
}