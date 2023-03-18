package sockets

import io.ktor.websocket.*
import java.util.concurrent.atomic.AtomicInteger

class Connection(val session: DefaultWebSocketSession, var timeStamp: Long = System.currentTimeMillis()) {
    companion object {
        var lastId = AtomicInteger(0)
    }

    val id = lastId.getAndIncrement()

    override fun toString(): String = "Connection(id=$id, timeStamp=$timeStamp)"
}