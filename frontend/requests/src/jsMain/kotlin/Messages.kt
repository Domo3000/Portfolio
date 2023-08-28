import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

@Serializable
sealed class Message

@Serializable
data class Counter(val piles: Int, val count: Long) : Message()

@Serializable
data class Row(val size: Int, val counters: List<Counter>) : Message()

@Serializable
data class Result(val rows: MutableList<Row>) : Message()

object Messages {
    private val json: Json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        classDiscriminator = "class"
    }

    fun decode(s: String): Message? = try {
        json.decodeFromString(Message.serializer(), s)
    } catch (e: SerializationException) {
        null
    }
}