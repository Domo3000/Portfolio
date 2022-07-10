package sockets

import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach

suspend fun DefaultClientWebSocketSession.readMessages(handler: (Frame.Text) -> Unit) {
    try { // TODO check if this stops when messages are empty or if I spawn hundreds of threads with endless loops
        incoming.consumeEach { message ->
            (message as? Frame.Text)?.let(handler)
        }
    } catch (e: Exception) {
        console.log("Error while receiving: " + e.message)
    }
}

suspend fun DefaultClientWebSocketSession.inputMessages(input: String) = try {
    send(input)
} catch (e: Exception) {
    console.log("Error while sending: " + e.message)
}

suspend fun HttpClient.webSocketSession(path: String) =
    webSocketSession(method = HttpMethod.Get, host = "127.0.0.1", port = 8080, path = path)