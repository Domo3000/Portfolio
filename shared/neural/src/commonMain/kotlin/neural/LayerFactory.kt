package neural

import org.jetbrains.kotlinx.dl.api.core.activation.Activations
import org.jetbrains.kotlinx.dl.api.core.initializer.*
import org.jetbrains.kotlinx.dl.api.core.layer.Layer
import org.jetbrains.kotlinx.dl.api.core.layer.convolutional.Conv2D
import org.jetbrains.kotlinx.dl.api.core.layer.convolutional.ConvPadding
import org.jetbrains.kotlinx.dl.api.core.layer.core.Dense
import org.jetbrains.kotlinx.dl.api.core.layer.normalization.BatchNorm
import org.jetbrains.kotlinx.dl.api.core.layer.pooling.AvgPool2D
import org.jetbrains.kotlinx.dl.api.core.layer.pooling.MaxPool2D
import org.jetbrains.kotlinx.dl.api.core.loss.Losses
import org.jetbrains.kotlinx.dl.api.core.metric.Metrics
import java.time.Instant
import kotlin.random.Random

fun Layer.copy(random: Random = Random(Instant.now().toEpochMilli())): Layer = when (this) {
    is Dense -> Dense(
        outputSize = outputSize,
        activation = activation,
        kernelInitializer = GlorotNormal(random.nextLong()),
        biasInitializer = GlorotNormal(random.nextLong())
    )

    is Conv2D -> Conv2D(
        filters = filters,
        kernelSize = kernelSize,
        strides = strides,
        activation = activation,
        kernelInitializer = GlorotNormal(random.nextLong()),
        biasInitializer = GlorotNormal(random.nextLong()),
        padding = padding
    )

    is AvgPool2D -> AvgPool2D(
        poolSize = poolSize,
        strides = strides,
        padding = padding
    )

    is MaxPool2D -> MaxPool2D(
        poolSize = poolSize,
        strides = strides,
        padding = padding
    )

    is BatchNorm -> BatchNorm()

    else -> throw Exception("unhandled Layer")
}

fun Layer.softRandomize(random: Random): Layer {
    return when (this) {
        is Dense -> {
            val new = LayerFactory.randomDenseLayer() as Dense
            Dense(
                outputSize = random.nextInt(outputSize - 25, outputSize + 25),
                activation = randomChoice(random, activation, new.activation),
                kernelInitializer = randomChoice(random, kernelInitializer, new.kernelInitializer),
                biasInitializer = randomChoice(random, biasInitializer, new.biasInitializer)
            )
        }

        is Conv2D -> {
            val new = LayerFactory.randomConv2DLayer() as Conv2D
            Conv2D(
                filters = random.nextInt(filters - 5, filters + 5),
                kernelSize = kernelSize,
                strides = strides,
                activation = randomChoice(random, activation, new.activation),
                kernelInitializer = randomChoice(random, kernelInitializer, new.kernelInitializer),
                biasInitializer = randomChoice(random, biasInitializer, new.biasInitializer),
                padding = padding
            )
        }

        is AvgPool2D -> {
            val new = LayerFactory.randomAvg2DLayer() as AvgPool2D
            val changed = AvgPool2D(
                poolSize = randomChoice(random, poolSize, new.poolSize),
                strides = randomChoice(random, strides, new.strides),
                padding = randomChoice(random, padding, new.padding)
            )
            randomChoice(random, changed, LayerFactory.randomConvLayer())
        }

        is MaxPool2D -> {
            val new = LayerFactory.randomMax2DLayer() as MaxPool2D
            val changed = MaxPool2D(
                poolSize = randomChoice(random, poolSize, new.poolSize),
                strides = randomChoice(random, strides, new.strides),
                padding = randomChoice(random, padding, new.padding)
            )
            randomChoice(random, changed, LayerFactory.randomConvLayer())
        }

        else -> this
    }
}

fun <T> randomChoice(random: Random, first: T, second: T) = if (random.nextBoolean()) {
    first
} else {
    second
}

object LayerFactory {
    private val random = Random(Instant.now().toEpochMilli())

    private fun defaultOutputLayer() = Dense(
        outputSize = 7,
        activation = Activations.Linear,
        kernelInitializer = GlorotNormal(random.nextLong()),
        biasInitializer = Constant(0.1f)
    )

    private val activations = listOf(
        Activations.Linear,
        Activations.Sigmoid,
        Activations.Tanh,
        Activations.Relu,
        Activations.Relu6,
        Activations.Elu,
        Activations.Selu,
        Activations.Softmax,
        Activations.LogSoftmax,
        Activations.Exponential,
        Activations.SoftPlus,
        Activations.SoftSign,
        Activations.HardSigmoid,
        Activations.SoftShrink,
        Activations.Swish,
        Activations.Mish,
        Activations.HardShrink,
        Activations.Gelu,
        Activations.LiSHT,
        Activations.Snake,
        Activations.TanhShrink,
        Activations.Sparsemax
    )

    private val initializers = listOf<(Long) -> Initializer>(
        { Constant(random.nextFloat()) },
        { GlorotNormal(it) },
        { GlorotUniform(it) },
        { HeNormal(it) },
        { HeUniform(it) },
        { LeCunNormal(it) },
        { LeCunUniform(it) },
        { Orthogonal() },
        { RandomNormal() },
        { RandomUniform() },
        // { VarianceScaling() }, Exporting VarianceScaling is not supported yet.
        { TruncatedNormal() },
        { Ones() },
        { Zeros() },
    )

    private val losses = listOf(
        Losses.HINGE,
        Losses.SQUARED_HINGE,
        Losses.MAE,
        Losses.BINARY_CROSSENTROPY,
        Losses.HUBER,
        Losses.MAPE,
        Losses.LOG_COSH,
        Losses.MSE,
        Losses.MSLE,
        Losses.POISSON,
        Losses.SOFT_MAX_CROSS_ENTROPY_WITH_LOGITS
    )

    private val metrics = listOf(
        Metrics.ACCURACY,
        Metrics.MAE,
        Metrics.MSE,
        Metrics.MSLE
    )

    fun randomActivation() = activations.random()

    fun randomInitializer(seed: Long) = initializers.random()(seed)

    fun randomLosses() = losses.random()

    fun randomMetrics() = metrics.random()

    fun conv2D(
        filters: Int,
        kernelSize: Int,
        activation: Activations,
        initializer: Initializer,
        biasInitializer: Initializer
    ): Layer = Conv2D(
        filters = filters,
        kernelSize = intArrayOf(kernelSize, kernelSize),
        strides = intArrayOf(1, 1, 1, 1),
        activation = activation,
        kernelInitializer = initializer,
        biasInitializer = biasInitializer,
        padding = ConvPadding.SAME
    )

    fun avg2D(size: Int, strides: Int, padding: ConvPadding): Layer = AvgPool2D(
        poolSize = intArrayOf(1, size, size, 1),
        strides = intArrayOf(1, strides, strides, 1),
        padding = padding
    )

    fun max2D(size: Int, strides: Int, padding: ConvPadding): Layer = MaxPool2D(
        poolSize = intArrayOf(1, size, size, 1),
        strides = intArrayOf(1, strides, strides, 1),
        padding = padding
    )

    fun randomConv2DLayer(): Layer {
        val filters = random.nextInt(2, 64)
        val kernelSize = random.nextInt(2, 6)
        val activation = randomActivation()
        val initializer = randomInitializer(random.nextLong())
        val biasInitializer = randomInitializer(random.nextLong())
        return conv2D(
            filters,
            kernelSize,
            activation,
            initializer,
            biasInitializer
        )
    }

    fun randomAvg2DLayer(): Layer {
        val size = random.nextInt(2, 5)
        val strides = random.nextInt(1, 3)
        val padding = randomChoice(random, ConvPadding.VALID, ConvPadding.SAME)
        return avg2D(size, strides, padding)
    }

    fun randomMax2DLayer(): Layer {
        val size = random.nextInt(2, 5)
        val strides = random.nextInt(1, 3)
        val padding = randomChoice(random, ConvPadding.VALID, ConvPadding.SAME)
        return max2D(size, strides, padding)
    }

    fun randomConvLayer(): Layer = when (random.nextInt(0, 4)) {
        0 -> randomAvg2DLayer()
        1 -> randomMax2DLayer()
        else -> randomConv2DLayer() // twice as likely
    }

    fun getRandomConvLayers(batchNorm: Boolean): List<Layer> {
        val convLayers = (0..(random.nextInt(0, 3))).map { randomConvLayer() }

        return if(batchNorm) {
            convLayers.map { listOf(it, BatchNorm()) }.flatten()
        } else {
            convLayers
        }
    }

    fun dense(size: Int, activation: Activations, initializer: Initializer, biasInitializer: Initializer) =
        Dense(
            outputSize = size,
            activation = activation,
            kernelInitializer = initializer,
            biasInitializer = biasInitializer
        )

    fun randomDenseLayer(outputSize: Int? = null): Layer {
        val size = outputSize ?: random.nextInt(12, 300)
        val activation = randomActivation()
        val initializer = randomInitializer(random.nextLong())
        val biasInitializer = randomInitializer(random.nextLong())

        return dense(size, activation, initializer, biasInitializer)
    }

    fun getRandomDenseLayers(): List<Layer> =
        (0..(random.nextInt(0, 3))).map { randomDenseLayer() }

    fun getOutputLayer(): Layer = randomChoice(random, defaultOutputLayer(), randomDenseLayer(7))
}
