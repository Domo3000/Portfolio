package connect4.messages

import connect4.game.Player
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

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
data class LayerDescription(val size: LayerSize, val activation: Activation) : Connect4Message() {
    fun toShortString() = if (size == LayerSize.None) {
        size.toShortString()
    } else {
        size.toShortString() + activation.toShortString()
    }
    companion object {
        fun fromShortString(short: String): LayerDescription {
            val size = LayerSize.fromShortString(short.take(1))

            return if(size == LayerSize.None) {
                LayerDescription(size, Activation.Swish)
            } else {
                val activation = Activation.fromShortString(short.drop(1))
                LayerDescription(size, activation)
            }
        }
    }
}

@Serializable
data class NeuralDescription(val conv: LayerDescription, val dense: LayerDescription) : Connect4Message() {
    fun toShortString() = "${conv.toShortString()}-${dense.toShortString()}"

    companion object {
        fun fromShortString(short: String): NeuralDescription {
            val descriptions = short.split("-").map(LayerDescription::fromShortString)

            return NeuralDescription(descriptions.first(), descriptions.last())
        }
    }
}

@Serializable
data class TrainingResultsMessage(val results: List<Pair<NeuralDescription, List<Double>>>) : Connect4Message()

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