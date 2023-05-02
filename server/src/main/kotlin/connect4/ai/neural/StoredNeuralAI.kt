package connect4.ai.neural

import kotlinx.serialization.json.Json
import org.jetbrains.kotlinx.dl.api.core.Sequential
import org.jetbrains.kotlinx.dl.api.core.layer.Layer
import org.jetbrains.kotlinx.dl.api.core.layer.convolutional.Conv2D
import org.jetbrains.kotlinx.dl.api.core.layer.core.Dense
import org.jetbrains.kotlinx.dl.api.core.layer.core.Input
import org.jetbrains.kotlinx.dl.api.core.layer.normalization.BatchNorm
import org.jetbrains.kotlinx.dl.api.core.layer.pooling.AvgPool2D
import org.jetbrains.kotlinx.dl.api.core.layer.pooling.MaxPool2D
import org.jetbrains.kotlinx.dl.api.core.loss.Losses
import org.jetbrains.kotlinx.dl.api.core.metric.Metrics
import org.jetbrains.kotlinx.dl.api.core.optimizer.Adam
import java.io.File

private val json = Json {
    ignoreUnknownKeys = true
    classDiscriminator = "class"
}

class StoredNeuralAI(
    inputType: Boolean,
    path: String,
    override val brain: Sequential
) : NeuralAI(inputType) {
    override val name: String = "StoredNeural($path)"

    fun toRandomNeural(): RandomNeuralAI {
        val convLayers = mutableListOf<Layer>()
        val denseLayers = mutableListOf<Layer>()
        var outputLayer: Layer? = null
        var inputType: Boolean? = null

        brain.layers.forEach {
            when (it) {
                is Input -> inputType = it.packedDims[2] == 1L
                is Conv2D -> convLayers.add(it)
                is BatchNorm -> convLayers.add(it)
                is AvgPool2D -> convLayers.add(it)
                is MaxPool2D -> convLayers.add(it)
                is Dense -> if (it.outputSize != 7) {
                    denseLayers.add(it)
                } else {
                    outputLayer = it
                }

                else -> {}
            }
        }

        return RandomNeuralAI(
            emptyList(),
            inputType!!,
            convLayers,
            denseLayers,
            outputLayer
        )
    }

    companion object {
        fun fromStorage(path: String): StoredNeuralAI {
            val neuralDirectory = File("${System.getProperty("user.dir")}/neurals/$path")
            val file = File(neuralDirectory.path + "/modelConfig.json")
            val additionalFile = File(neuralDirectory.path + "/additionalInfo.json")
            val stored = Sequential.loadModelConfiguration(file)
            stored.compile(Adam(), Losses.SOFT_MAX_CROSS_ENTROPY_WITH_LOGITS, Metrics.ACCURACY)
            stored.loadWeights(neuralDirectory, true)
            val additionalInfo = json.decodeFromString(AdditionalInfo.serializer(), additionalFile.readText())
            return StoredNeuralAI(additionalInfo.input, path, stored)
        }
    }
}