package neural

import kotlinx.serialization.json.Json
import org.jetbrains.kotlinx.dl.api.core.Sequential
import org.jetbrains.kotlinx.dl.api.core.loss.Losses
import org.jetbrains.kotlinx.dl.api.core.metric.Metrics
import org.jetbrains.kotlinx.dl.api.core.optimizer.Adam
import java.io.File

private val json = Json {
    ignoreUnknownKeys = true
    classDiscriminator = "class"
}

class StoredNeuralAI(
    name: String,
    override val brain: Sequential
) : NeuralAI() {
    override val name: String = "StoredNeural($name)"

    companion object {
        fun fromStorage(path: String, name: String): StoredNeuralAI {
            val neuralDirectory = File("${System.getProperty("user.dir")}/$path/$name")
            val file = File(neuralDirectory.path + "/modelConfig.json")
            val stored = Sequential.loadModelConfiguration(file)
            stored.compile(Adam(), Losses.SOFT_MAX_CROSS_ENTROPY_WITH_LOGITS, Metrics.ACCURACY)
            stored.loadWeights(neuralDirectory, true)
            val additionalFile = File(neuralDirectory.path + "/additionalInfo.json")
            val additionalInfo = json.decodeFromString(AdditionalInfo.serializer(), additionalFile.readText())
            return StoredNeuralAI(name, stored)
        }
    }
}
