package connect4.messages

import connect4.game.Player
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

enum class AIChoice {
    Simple,
    Medium,
    Hard,
    MonteCarlo,
    Length,
    Neural
}

@Serializable
sealed class Connect4Message {
    fun encode(): String = Connect4Messages.encode(this)
}

@Serializable
data class ConnectedMessage(val sessionId: Int) : Connect4Message()

@Serializable
data class PickedAIMessage(val ai: AIChoice) : Connect4Message()

@Serializable
data class PickedPlayerMessage(val player: Player) : Connect4Message()

@Serializable
data class GameStartedMessage(val player: Player) : Connect4Message()

@Serializable
data class GameFinishedMessage(val player: Player?) : Connect4Message()

@Serializable
data class NextMoveMessage(val column: Int) : Connect4Message()

@Serializable
object WaitMessage : Connect4Message()

@Serializable
object ContinueMessage : Connect4Message()

object Connect4Messages {
    private val json: Json = Json {
        ignoreUnknownKeys = true
        classDiscriminator = "class"
    }

    fun decode(s: String): Connect4Message? = try {
        json.decodeFromString(Connect4Message.serializer(), s)
    } catch (e: SerializationException) {
        null
    }

    fun encode(m: Connect4Message): String = json.encodeToString(Connect4Message.serializer(), m)
}