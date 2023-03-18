package util

import connect4.messages.Connect4Message
import connect4.messages.Connect4Messages
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach

suspend fun DefaultClientWebSocketSession.readMessages(handler: (Connect4Message) -> Unit) {
    try {
        incoming.consumeEach { frame ->
            (frame as? Frame.Text)?.let { text ->
                Connect4Messages.decode(text.readText())?.let { message ->
                    handler(message)
                }
            }
        }
    } catch (e: Exception) {
        console.log("Error while receiving: " + e.message)
    }
}

suspend fun DefaultClientWebSocketSession.inputMessage(input: String) = try {
    send(input)
} catch (e: Exception) {
    console.log("Error while sending: " + e.message)
}