package neural

import connect4.game.*
import connect4.messages.ConvLayerDescription
import connect4.messages.DenseLayerDescription
import connect4.messages.NeuralDescription
import org.jetbrains.kotlinx.dl.api.core.activation.Activations
import org.jetbrains.kotlinx.dl.api.core.initializer.*
import org.jetbrains.kotlinx.dl.api.core.layer.Layer
import org.jetbrains.kotlinx.dl.api.core.layer.core.Dense
import kotlin.random.Random

private fun Activation.toActivations() = when (this) {
    Activation.Elu -> Activations.Elu
    Activation.LiSHT -> Activations.LiSHT
    Activation.Mish -> Activations.Mish
    Activation.Relu -> Activations.Relu
    Activation.Snake -> Activations.Snake
}

private fun OutputActivation.toActivations() = when (this) {
    OutputActivation.Linear -> Activations.Linear
    OutputActivation.Relu -> Activations.Relu
    OutputActivation.Sigmoid -> Activations.Sigmoid
    OutputActivation.Softmax -> Activations.Softmax
    OutputActivation.Tanh -> Activations.Tanh
}

private fun Activation.getInitializers(): Pair<(Long) -> Initializer, (Long) -> Initializer> = when (this) {
    Activation.Elu -> Pair({ HeNormal(it) }, { Constant(0.1f) })
    Activation.Relu -> Pair({ HeNormal(it) }, { Constant(0.01f) })
    Activation.Snake -> Pair({ LeCunNormal(it) }, { Constant(0.01f) })
    else -> Pair({ HeNormal(it) }, { HeUniform(it) })
}

private fun OutputActivation.getInitializers(): Pair<(Long) -> Initializer, (Long) -> Initializer> = when (this) {
    OutputActivation.Linear -> Pair({ GlorotNormal(it) }, { Constant(0.1f) })
    OutputActivation.Relu -> Pair({ LeCunNormal(it) }, { Constant(0.5f) })
    OutputActivation.Sigmoid -> Pair({ LeCunNormal(it) }, { LeCunUniform(it) })
    OutputActivation.Softmax -> Pair({ GlorotNormal(it) }, { Constant(0.5f) })
    OutputActivation.Tanh -> Pair({ HeNormal(it) }, { Constant(0.1f) })
}

private data class ConvLayer(val filters: Int, val kernelSize: Int, val padding: Padding) {
    fun toLayer(
        activation: Activation,
        seed: Long,
        initializers: Pair<(Long) -> Initializer, (Long) -> Initializer>? = null
    ): Layer {
        val (initializer, biasInitializer) = initializers ?: activation.getInitializers()

        return LayerFactory.conv2D(
            filters,
            kernelSize,
            padding,
            activation.toActivations(),
            initializer(seed),
            biasInitializer(seed)
        )
    }
}

private data class DenseLayer(val size: Int) {
    fun toLayer(
        activation: Activation,
        seed: Long,
        initializers: Pair<(Long) -> Initializer, (Long) -> Initializer>? = null
    ): Dense {
        val (initializer, biasInitializer) = initializers ?: activation.getInitializers()

        return LayerFactory.dense(
            size,
            activation.toActivations(),
            initializer(seed),
            biasInitializer(seed)
        )
    }
}

object NeuralBuilder {
    fun build(
        inputType: InputType,
        batchNorm: Boolean,
        conv: List<Layer>,
        dense: List<Dense>,
        output: Dense
    ) = ConstructedNeuralAI(
        inputType = inputType,
        batchNorm = batchNorm,
        conv = conv,
        dense = dense,
        output = output
    )

    fun buildConv(
        description: ConvLayerDescription,
        random: Random,
        initializers: Pair<(Long) -> Initializer, (Long) -> Initializer>? = null
    ): List<Layer> =
        when (description.size) {
            LayerSize.None -> emptyList()

            LayerSize.Small -> listOf(
                ConvLayer(32, 4, description.padding!!)
            )

            LayerSize.Medium -> listOf(
                ConvLayer(32, 4, description.padding!!),
                ConvLayer(32, 3, description.padding!!)
            )

            LayerSize.Large -> listOf(
                ConvLayer(64, 4, description.padding!!),
                ConvLayer(64, 3, description.padding!!)
            )

            LayerSize.Giant -> listOf(
                ConvLayer(64, 4, description.padding!!),
                ConvLayer(64, 2, description.padding!!),
                ConvLayer(64, 2, description.padding!!)
            )
        }.map { it.toLayer(description.activation!!, random.nextLong(), initializers) }

    fun buildDense(
        description: DenseLayerDescription,
        random: Random,
        initializers: Pair<(Long) -> Initializer, (Long) -> Initializer>? = null
    ): List<Dense> = when (description.size) {
        LayerSize.None -> emptyList()
        LayerSize.Small -> listOf(DenseLayer(70))
        LayerSize.Medium -> listOf(DenseLayer(350))
        LayerSize.Large -> listOf(210, 210, 210).map { DenseLayer(it) }
        LayerSize.Giant -> listOf(420, 350, 280, 210, 140, 70).map { DenseLayer(it) }
        else -> emptyList()
    }.map { it.toLayer(description.activation!!, random.nextLong(), initializers) }

    fun buildOutput(
        activation: OutputActivation,
        random: Random,
        initializers: Pair<(Long) -> Initializer, (Long) -> Initializer>? = null
    ): Dense {
        val (initializer, biasInitializer) = initializers ?: activation.getInitializers()

        return LayerFactory.dense(
            7,
            activation.toActivations(),
            initializer(random.nextLong()),
            biasInitializer(random.nextLong())
        )
    }

    fun build(
        description: NeuralDescription,
        random: Random
    ): ConstructedNeuralAI = build(
        inputType = description.inputType,
        batchNorm = description.batchNorm,
        conv = buildConv(description.conv, random),
        dense = buildDense(description.dense, random),
        output = buildOutput(description.outputLayer, random)
    )
}
