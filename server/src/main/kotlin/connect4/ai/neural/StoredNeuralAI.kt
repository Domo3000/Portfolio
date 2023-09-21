package connect4.ai.neural

import org.jetbrains.kotlinx.dl.api.core.Sequential
import org.jetbrains.kotlinx.dl.api.core.loss.Losses
import org.jetbrains.kotlinx.dl.api.core.metric.Metrics
import org.jetbrains.kotlinx.dl.api.core.optimizer.Adam
import java.io.File

class StoredNeuralAI(
    private val path: String,
    override val brain: Sequential
) : NeuralAI() {
    override val name: String = "StoredNeural($path)"

    fun toRandomNeural() = RandomNeuralAI(
        brain,
        name = path
    )

    companion object {
        fun fromStorage(path: String): StoredNeuralAI {
            val neuralDirectory = File("${System.getProperty("user.dir")}/neurals/$path")
            val file = File(neuralDirectory.path + "/modelConfig.json")
            val stored = Sequential.loadModelConfiguration(file)
            stored.compile(Adam(), Losses.SOFT_MAX_CROSS_ENTROPY_WITH_LOGITS, Metrics.ACCURACY)
            stored.loadWeights(neuralDirectory, true)
            return StoredNeuralAI(path, stored)
        }
    }
}
