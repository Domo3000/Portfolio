package neural

import org.jetbrains.kotlinx.dl.api.core.Sequential
import org.jetbrains.kotlinx.dl.api.core.layer.Layer
import org.jetbrains.kotlinx.dl.api.core.layer.convolutional.Conv2D
import org.jetbrains.kotlinx.dl.api.core.layer.core.Dense
import org.jetbrains.kotlinx.dl.api.core.layer.core.Input
import org.jetbrains.kotlinx.dl.api.core.layer.normalization.BatchNorm
import org.jetbrains.kotlinx.dl.api.core.layer.pooling.AvgPool2D
import org.jetbrains.kotlinx.dl.api.core.layer.pooling.MaxPool2D
import org.jetbrains.kotlinx.dl.api.core.layer.reshaping.Flatten
import org.jetbrains.kotlinx.dl.api.core.loss.Losses
import org.jetbrains.kotlinx.dl.api.core.metric.Metrics
import org.jetbrains.kotlinx.dl.api.core.optimizer.Adam
import java.time.Instant

class RandomNeuralAI(
    override val brain: Sequential,
    private val age: Int = 0,
    name: String? = null
) : NeuralAI() {
    companion object {
        fun createSequential(
            input: Input,
            conv: List<Layer>? = null,
            dense: List<Layer>? = null,
            output: Layer? = null,
            batchNorm: Boolean? = null
        ): Sequential {
            val denseLayer = dense?.map { it.copy() } ?: LayerFactory.getRandomDenseLayers()
            val outputLayer = output?.copy() ?: LayerFactory.getOutputLayer()

            val convLayer = conv?.map {
                val c = it.copy()
                if (batchNorm == true) {
                    listOf(c, BatchNorm())
                } else {
                    listOf(c)
                }
            }?.flatten() ?: LayerFactory.getRandomConvLayers(batchNorm ?: false)

            val sequential = Sequential.of(listOf(input) + convLayer + Flatten() + denseLayer + outputLayer)

            sequential.compile(
                optimizer = Adam(),
                loss = Losses.SOFT_MAX_CROSS_ENTROPY_WITH_LOGITS,
                metric = Metrics.ACCURACY
            )

            sequential.init()

            return sequential
        }
    }

    constructor(
        input: Input,
        conv: List<Layer>? = null,
        dense: List<Layer>? = null,
        output: Layer? = null,
        age: Int = 0,
        batchNorm: Boolean? = null
    ) : this(createSequential(input, conv, dense, output, batchNorm), age = age)

    private val timeStamp = Instant.now().toEpochMilli()

    override val name: String = "RandomNeural(${name ?: "$timeStamp:$age"})"

    fun evolve(
        inputSingular: Boolean? = null,
        batchNorm: Boolean? = null,
        softRandomize: Boolean = false,
        newConv: Boolean = false,
        newDense: Boolean = false
    ): RandomNeuralAI {
        val convLayers = mutableListOf<Layer>()
        val denseLayers = mutableListOf<Layer>()
        var outputLayer: Layer? = null
        var batch = false

        brain.layers.forEach {
            when (it) {
                is Conv2D -> convLayers.add(it.copy(random))
                is BatchNorm -> batch = true
                is AvgPool2D -> convLayers.add(it.copy(random))
                is MaxPool2D -> convLayers.add(it.copy(random))
                is Dense -> if (it.outputSize != 7) {
                    denseLayers.add(it.copy(random))
                } else {
                    outputLayer = it.copy(random)
                }

                else -> {}
            }
        }

        if(softRandomize) {
            setOf(convLayers, denseLayers).forEach { layer ->
                val randomized = layer.map { it.softRandomize(random) }
                layer.clear()
                layer.addAll(randomized)
            }
        }

        return RandomNeuralAI(
            InputType.toInput(inputSingular ?: (brain.inputLayer.packedDims[2] == 1L)),
            if(newConv) null else convLayers,
            if(newDense) null else denseLayers,
            outputLayer,
            age + 1,
            batchNorm ?: batch
        )
    }
}
