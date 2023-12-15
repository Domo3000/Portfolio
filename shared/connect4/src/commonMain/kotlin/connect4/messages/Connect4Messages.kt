package connect4.messages

import connect4.game.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

private fun Boolean.toShortString() = when(this) {
    true -> "T"
    false -> "F"
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
data class DenseLayerDescription(val size: LayerSize, val activation: Activation? = null) : Connect4Message() {
    fun toShortString() = size.toShortString() + (activation?.toShortString() ?: "")

    companion object {
        private fun getActivation(short: String) = Activation.fromShortString(short.drop(1).take(1))

        fun fromShortString(short: String): DenseLayerDescription =
            when (val size = LayerSize.fromShortString(short.take(1))) {
                LayerSize.None -> DenseLayerDescription(size)
                else -> DenseLayerDescription(size, getActivation(short))
            }
    }
}

@Serializable
data class ConvLayerDescription(
    val size: LayerSize,
    val activation: Activation? = null,
    val padding: Padding? = null
) : Connect4Message() {
    fun toShortString() = size.toShortString() +
            (activation?.toShortString() ?: "") +
            (padding?.toShortString() ?: "")

    companion object {
        private fun getActivation(short: String) = Activation.fromShortString(short.drop(1).take(1))
        private fun getPadding(short: String) = Padding.fromShortString(short.drop(2).take(1))

        fun fromShortString(short: String): ConvLayerDescription =
            when (val size = LayerSize.fromShortString(short.take(1))) {
                LayerSize.None -> ConvLayerDescription(size)
                else -> ConvLayerDescription(size, getActivation(short), getPadding(short))
            }
    }
}

@Serializable
data class NeuralDescription(
    val inputSingular: Boolean,
    val batchNorm: Boolean,
    val conv: ConvLayerDescription,
    val dense: DenseLayerDescription,
    val outputLayer: OutputActivation
) : Connect4Message() {
    fun toShortString(): String =
        "${inputSingular.toShortString()}${batchNorm.toShortString()}-${conv.toShortString()}-${dense.toShortString()}-${outputLayer.toShortString()}"

    companion object {
        fun fromShortString(short: String): NeuralDescription {
            val parts = short.split("-")

            return NeuralDescription(
                parts[0][0] == 'T',
                parts[0][1] == 'T',
                ConvLayerDescription.fromShortString(parts[1]),
                DenseLayerDescription.fromShortString(parts[2]),
                OutputActivation.fromShortString(parts[3])
            )
        }
    }
}

@Serializable
data class TrainingResultMessage(
    val description: NeuralDescription,
    val trainingTime: Double,
    val results: List<Double>
) : Connect4Message()

@Serializable // TODO better names, and include in Portfolio
data class TrainingResultsMessage(val results: List<TrainingResultMessage>) : Connect4Message()

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