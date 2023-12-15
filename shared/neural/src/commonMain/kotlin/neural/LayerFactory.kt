package neural

import connect4.game.Padding
import org.jetbrains.kotlinx.dl.api.core.activation.Activations
import org.jetbrains.kotlinx.dl.api.core.initializer.Initializer
import org.jetbrains.kotlinx.dl.api.core.layer.Layer
import org.jetbrains.kotlinx.dl.api.core.layer.convolutional.Conv2D
import org.jetbrains.kotlinx.dl.api.core.layer.convolutional.ConvPadding
import org.jetbrains.kotlinx.dl.api.core.layer.core.Dense
import org.jetbrains.kotlinx.dl.api.core.layer.normalization.BatchNorm
import org.jetbrains.kotlinx.dl.api.core.layer.pooling.AvgPool2D
import org.jetbrains.kotlinx.dl.api.core.layer.pooling.MaxPool2D

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

    is BatchNorm -> BatchNorm()

    else -> throw Exception("unhandled Layer")
}

object LayerFactory {
    fun conv2D(
        filters: Int,
        kernelSize: Int,
        padding: Padding,
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
        padding = when(padding) {
            Padding.Same -> ConvPadding.SAME
            Padding.Valid -> ConvPadding.VALID
        }
    )

    fun dense(size: Int, activation: Activations, initializer: Initializer, biasInitializer: Initializer) =
        Dense(
            outputSize = size,
            activation = activation,
            kernelInitializer = initializer,
            biasInitializer = biasInitializer
        )
}
