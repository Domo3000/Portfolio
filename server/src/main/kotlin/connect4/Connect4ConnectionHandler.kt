package connect4

import connect4.messages.*
import io.ktor.websocket.*
import kotlinx.coroutines.cancel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import sockets.Connection

object Connect4ConnectionHandler {
    private val mutex = Mutex()
    private val connections = mutableSetOf<Connection>()

    suspend fun add(connection: Connection) = mutex.withLock { connections += connection }

    suspend fun remove(connection: Connection) =
        mutex.withLock { connections.removeIf { it.session == connection.session } }

    suspend fun cleanUp(now: Long): Unit = mutex.withLock {
        connections.removeAll { it.timeStamp < now - 1000 * 60 * 10 }
    }

    suspend fun replyTo(id: Int, message: Connect4Message) = mutex.withLock {
        connections.find { it.id == id }?.session?.send(message.encode())
    }

    suspend fun flush(id: Int) = mutex.withLock {
        connections.find { it.id == id }?.session?.flush()
    }
}