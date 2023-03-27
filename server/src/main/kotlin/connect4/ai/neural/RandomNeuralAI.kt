package connect4.ai.neural

import connect4.game.Player
import connect4.game.sizeX
import connect4.game.sizeY
import org.jetbrains.kotlinx.dl.api.core.Sequential
import org.jetbrains.kotlinx.dl.api.core.activation.Activations
import org.jetbrains.kotlinx.dl.api.core.initializer.*
import org.jetbrains.kotlinx.dl.api.core.layer.Layer
import org.jetbrains.kotlinx.dl.api.core.layer.convolutional.Conv2D
import org.jetbrains.kotlinx.dl.api.core.layer.convolutional.ConvPadding
import org.jetbrains.kotlinx.dl.api.core.layer.core.Dense
import org.jetbrains.kotlinx.dl.api.core.layer.core.Input
import org.jetbrains.kotlinx.dl.api.core.layer.pooling.AvgPool2D
import org.jetbrains.kotlinx.dl.api.core.layer.pooling.MaxPool2D
import org.jetbrains.kotlinx.dl.api.core.layer.reshaping.Flatten
import org.jetbrains.kotlinx.dl.api.core.loss.Losses
import org.jetbrains.kotlinx.dl.api.core.metric.Metrics
import org.jetbrains.kotlinx.dl.api.core.optimizer.Adam
import java.time.Instant
import kotlin.random.Random

fun <T> randomChoice(random: Random, first: T, second: T) = if (random.nextBoolean()) {
    first
} else {
    second
}

object NeuralAIFactory {
    private val random = Random(Instant.now().toEpochMilli())

    fun inputLayer(dims: Long) = Input(
        sizeX.toLong(),
        sizeY.toLong(),
        dims
    )

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

    private fun randomActivation() = activations.random()

    private fun randomInitializer(seed: Long) = initializers.random()(seed)

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

    fun randomConvLayer(): Layer = when (random.nextInt(0, 3)) {
        0 -> randomConv2DLayer()
        1 -> randomAvg2DLayer()
        2 -> randomMax2DLayer()
        else -> throw Exception("illegal random")
    }

    fun getRandomConvLayers(): List<Layer> = (0..(random.nextInt(0, 6))).map { randomConvLayer() }

    fun dense(size: Int, activation: Activations, initializer: Initializer, biasInitializer: Initializer) =
        Dense(
            outputSize = size,
            activation = activation,
            kernelInitializer = initializer,
            biasInitializer = biasInitializer
        )

    fun randomDenseLayer(outputSize: Int): Layer {
        val activation = randomActivation()
        val initializer = randomInitializer(random.nextLong())
        val biasInitializer = randomInitializer(random.nextLong())

        return dense(outputSize, activation, initializer, biasInitializer)
    }

    fun getRandomDenseLayers(): List<Layer> =
        (0..(random.nextInt(0, 5))).map { randomDenseLayer(random.nextInt(12, 300)) }

    fun getOutputLayer(): Layer = randomChoice(random, defaultOutputLayer(), randomDenseLayer(7))
}


fun Layer.name(inputSingular: Boolean): String = when (this) {
    is Input -> "Input(${
        if (inputSingular) {
            1
        } else {
            2
        }
    })"

    is Dense -> "Dense($outputSize/${activation.name})"
    is Conv2D -> "Conv($filters/${kernelSize.contentToString()}/${activation.name})"
    is AvgPool2D -> "Avg2D(${poolSize.contentToString()}/${strides.contentToString()}/$padding)"
    is MaxPool2D -> "Max2D(${poolSize.contentToString()}/${strides.contentToString()}/$padding)"
    is Flatten -> "Flatten()"
    else -> throw Exception("unhandled Layer")
}

fun Layer.copy(): Layer = when (this) {
    is Dense -> Dense(
        outputSize = outputSize,
        activation = activation,
        kernelInitializer = kernelInitializer,
        biasInitializer = biasInitializer
    )

    is Conv2D -> Conv2D(
        filters = filters,
        kernelSize = kernelSize,
        strides = strides,
        activation = activation,
        kernelInitializer = kernelInitializer,
        biasInitializer = biasInitializer,
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

    else -> throw Exception("unhandled Layer")
}

fun Layer.softRandomize(): Layer {
    val random = Random(Instant.now().toEpochMilli())

    return when (this) {
        is Dense -> {
            NeuralAIFactory.randomDenseLayer(outputSize) as Dense
        }

        is Conv2D -> {
            val new = NeuralAIFactory.randomConv2DLayer() as Conv2D
            Conv2D(
                filters = filters,
                kernelSize = kernelSize,
                strides = strides,
                activation = randomChoice(random, activation, new.activation),
                kernelInitializer = randomChoice(random, kernelInitializer, new.kernelInitializer),
                biasInitializer = randomChoice(random, biasInitializer, new.biasInitializer),
                padding = padding
            )
        }

        is AvgPool2D -> {
            val new = NeuralAIFactory.randomAvg2DLayer() as AvgPool2D
            AvgPool2D(
                poolSize = randomChoice(random, poolSize, new.poolSize),
                strides = randomChoice(random, strides, new.strides),
                padding = randomChoice(random, padding, new.padding)
            )
        }

        is MaxPool2D -> {
            val new = NeuralAIFactory.randomMax2DLayer() as MaxPool2D
            MaxPool2D(
                poolSize = randomChoice(random, poolSize, new.poolSize),
                strides = randomChoice(random, strides, new.strides),
                padding = randomChoice(random, padding, new.padding)
            )
        }

        else -> throw Exception("unhandled Layer")
    }
}

fun Layer.randomize(): Layer {
    val random = Random(Instant.now().toEpochMilli())

    return when (this) {
        is Dense -> {
            val new = NeuralAIFactory.randomDenseLayer(random.nextInt(40, 200)) as Dense
            Dense(
                outputSize = randomChoice(random, outputSize, new.outputSize),
                activation = randomChoice(random, activation, new.activation),
                kernelInitializer = randomChoice(random, kernelInitializer, new.kernelInitializer),
                biasInitializer = randomChoice(random, biasInitializer, new.biasInitializer)
            )
        }

        is Conv2D -> {
            val new = NeuralAIFactory.randomConv2DLayer() as Conv2D
            Conv2D(
                filters = randomChoice(random, filters, new.filters),
                kernelSize = randomChoice(random, kernelSize, new.kernelSize),
                strides = randomChoice(random, strides, new.strides),
                activation = randomChoice(random, activation, new.activation),
                kernelInitializer = randomChoice(random, kernelInitializer, new.kernelInitializer),
                biasInitializer = randomChoice(random, biasInitializer, new.biasInitializer),
                padding = randomChoice(random, padding, new.padding)
            )
        }

        is AvgPool2D -> {
            val new = NeuralAIFactory.randomAvg2DLayer() as AvgPool2D
            AvgPool2D(
                poolSize = randomChoice(random, poolSize, new.poolSize),
                strides = randomChoice(random, strides, new.strides),
                padding = randomChoice(random, padding, new.padding)
            )
        }

        is MaxPool2D -> {
            val new = NeuralAIFactory.randomMax2DLayer() as MaxPool2D
            MaxPool2D(
                poolSize = randomChoice(random, poolSize, new.poolSize),
                strides = randomChoice(random, strides, new.strides),
                padding = randomChoice(random, padding, new.padding)
            )
        }

        else -> throw Exception("unhandled Layer")
    }
}

class RandomNeuralAI(
    training: List<Move> = emptyList(),
    inputType: Boolean,
    conv: List<Layer>? = null,
    dense: List<Layer>? = null,
    output: Layer? = null,
    losses: Losses? = null,
    metrics: Metrics? = null,
    private val age: Int = 0
) : NeuralAI(inputType) {
    private val timeStamp = Instant.now().toEpochMilli()
    private val inputSingular = inputType
    val convLayer = conv?.map { it.copy() } ?: NeuralAIFactory.getRandomConvLayers()
    val denseLayer = dense?.map { it.copy() } ?: NeuralAIFactory.getRandomDenseLayers()
    val outputLayer = output?.copy() ?: NeuralAIFactory.getOutputLayer()
    val loss = losses ?: NeuralAIFactory.randomLosses()
    val metric = metrics ?: NeuralAIFactory.randomMetrics()

    private val fullBrain: List<Layer> = run {
        val dims = if (inputSingular) {
            1L
        } else {
            2L
        }
        listOf(NeuralAIFactory.inputLayer(dims)) + convLayer + Flatten() + denseLayer + outputLayer
    }

    override val brain = Sequential.of(fullBrain.map { it })

    override val name: String = "RandomNeural(${timeStamp}:${age})"

    override fun nextMove(field: List<List<Player?>>, availableColumns: List<Int>, player: Player): Int =
        nextMoveRanked(field, availableColumns, player)
            .map { it.first }
            .first()

    override fun info() =
        "$name: {$loss, $metric, " + fullBrain.joinToString(", ", " ", " ") { it.name(inputSingular) } + "}"

    fun copy(
        training: List<Move>,
        conv: List<Layer>? = convLayer,
        dense: List<Layer>? = denseLayer,
        output: Layer? = outputLayer,
        losses: Losses? = loss,
        metrics: Metrics? = metric
    ) = RandomNeuralAI(training, inputSingular, conv, dense, output, losses, metrics, age + 1)

    init {
        brain.compile(
            optimizer = Adam(),
            loss = Losses.SOFT_MAX_CROSS_ENTROPY_WITH_LOGITS,
            metric = Metrics.ACCURACY
        )

        brain.init()

        brain.fit(
            training.toDataset(Player.FirstPlayer, inputSingular),
            epochs,
            100
        )
    }
}