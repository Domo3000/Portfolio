package neural

import connect4.messages.Activation
import connect4.messages.LayerDescription
import connect4.messages.LayerSize
import org.jetbrains.kotlinx.dl.api.core.activation.Activations
import org.jetbrains.kotlinx.dl.api.core.initializer.*
import org.jetbrains.kotlinx.dl.api.core.layer.Layer
import org.jetbrains.kotlinx.dl.api.core.layer.core.Dense
import kotlin.random.Random

private fun Activation.toActivations() = when (this) {
    Activation.Relu -> Activations.Relu
    Activation.Swish -> Activations.Swish
    Activation.LiSHT -> Activations.LiSHT
    Activation.Elu -> Activations.Elu
    Activation.Mish -> Activations.Mish
}

private fun Activation.getInitializers(): Pair<(Long) -> Initializer, (Long) -> Initializer> = when (this) {
    Activation.Relu -> Pair({ HeNormal(it) }, { Constant(0.01f) })
    Activation.Swish, Activation.Mish -> Pair({ HeNormal(it) }, { HeUniform(it) })
    Activation.Elu -> Pair({ HeNormal(it) }, { Zeros() })
    else -> Pair({ GlorotNormal(it) }, { RandomNormal(0.0f, 0.2f) })
}

private data class ConvLayer(val filters: Int, val kernelSize: Int) {
    fun toLayer(activation: Activation, seed: Long): Layer {
        val (initializer, biasInitializer) = activation.getInitializers()

        return LayerFactory.conv2D(
            filters,
            kernelSize,
            activation.toActivations(),
            initializer(seed),
            biasInitializer(seed)
        )
    }
}

private data class DenseLayer(val size: Int) {
    fun toLayer(activation: Activation, seed: Long): Dense {
        val (initializer, biasInitializer) = activation.getInitializers()

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
        conv: LayerDescription,
        dense: LayerDescription,
        random: Random
    ): RandomNeuralAI {
        val convLayer = when (conv.size) {
            LayerSize.None -> emptyList()
            LayerSize.Small -> listOf(
                ConvLayer(32, 4)
            )

            LayerSize.Medium -> listOf(
                ConvLayer(64, 4)
            )

            LayerSize.Large -> listOf(
                ConvLayer(64, 4),
                ConvLayer(64, 3)
            )

            LayerSize.Giant -> listOf(
                ConvLayer(128, 4),
                ConvLayer(64, 4),
                ConvLayer(32, 4)
            )
        }.map { it.toLayer(conv.activation, random.nextLong()) }

        val denseLayer = when (dense.size) {
            LayerSize.None -> emptyList()
            LayerSize.Small -> listOf(DenseLayer(210))
            LayerSize.Medium -> listOf(DenseLayer(210), DenseLayer(210), DenseLayer(210))
            LayerSize.Large -> listOf(DenseLayer(630))
            LayerSize.Giant -> listOf(
                DenseLayer(630),
                DenseLayer(420),
                DenseLayer(210),
                DenseLayer(140),
                DenseLayer(70)
            )
        }.map { it.toLayer(dense.activation, random.nextLong()) }

        return RandomNeuralAI(
            input = InputType.toInput(false),
            batchNorm = when (conv.size) {
                LayerSize.Giant -> true
                else -> false
            },
            conv = convLayer,
            dense = denseLayer,
            output = LayerFactory.dense(
                7,
                Activations.Linear,
                GlorotNormal(random.nextLong()),
                Constant(0.5f)
            )
        )
    }

    fun basic(random: Random): List<RandomNeuralAI> {
        val smaller = listOf(LayerSize.None, LayerSize.Small, LayerSize.Medium)

        return smaller.flatMap { conv ->
            smaller.flatMap { dense ->
                Activation.entries.map { activation ->
                    build(LayerDescription(conv, activation), LayerDescription(dense, activation), random)
                }
            }
        }
    }

    fun noConv(random: Random) = LayerSize.entries.flatMap { dense ->
        Activation.entries.map { activation ->
            build(LayerDescription(LayerSize.None, activation), LayerDescription(dense, activation), random)
        }
    }

    fun medium(random: Random): List<RandomNeuralAI> {
        val medium = listOf(LayerSize.Small, LayerSize.Medium, LayerSize.Large)

        return medium.flatMap { conv ->
            medium.flatMap { dense ->
                Activation.entries.map { activation ->
                    build(LayerDescription(conv, activation), LayerDescription(dense, activation), random)
                }
            }
        }
    }

    fun big(random: Random): List<RandomNeuralAI> {
        val big = listOf(LayerSize.Medium, LayerSize.Large, LayerSize.Giant)

        return big.flatMap { conv ->
            big.flatMap { dense ->
                Activation.entries.map { activation ->
                    build(LayerDescription(conv, activation), LayerDescription(dense, activation), random)
                }
            }
        }
    }
}
