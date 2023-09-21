package connect4.ai.neural

import org.jetbrains.kotlinx.dl.api.core.activation.Activations
import org.jetbrains.kotlinx.dl.api.core.initializer.Constant
import org.jetbrains.kotlinx.dl.api.core.initializer.GlorotNormal
import org.jetbrains.kotlinx.dl.api.core.initializer.RandomNormal
import org.jetbrains.kotlinx.dl.api.core.layer.convolutional.ConvPadding
import kotlin.random.Random

sealed interface ConvLayer
data class Conv(val filters: Int, val kernelSize: Int) : ConvLayer
data class Avg(val size: Int) : ConvLayer
data class Max(val size: Int) : ConvLayer

object PredefinedNeurals {
    fun builder(
        //inputSingular: Boolean,
        conv: List<Pair<ConvLayer, Activations>>,
        dense: List<Pair<Int, Activations>>,
        random: Random,
        batchNorm: Boolean = false
    ) = RandomNeuralAI(
        input = InputType.toInput(false),
        batchNorm = batchNorm,
        conv = conv.map { (layer, activation) ->
            when (layer) {
                is Avg -> LayerFactory.avg2D(layer.size, 1, ConvPadding.SAME)
                is Max -> LayerFactory.max2D(layer.size, 1, ConvPadding.SAME)
                is Conv -> LayerFactory.conv2D(
                    layer.filters,
                    layer.kernelSize,
                    activation,
                    GlorotNormal(random.nextLong()),
                    RandomNormal(0.0f, 0.2f)
                )
            }
        },
        dense = dense.map { (size, activation) ->
            LayerFactory.dense(
                size,
                activation,
                GlorotNormal(random.nextLong()),
                RandomNormal(0.0f, 0.2f)
            )
        },
        output = LayerFactory.dense(
            7,
            Activations.Linear,
            GlorotNormal(random.nextLong()),
            Constant(0.5f)
        )
    )

    fun basic(random: Random) = listOf(
        builder(listOf(Conv(32, 4) to Activations.Mish), emptyList(), random),
        builder(listOf(Conv(64, 4) to Activations.Mish), emptyList(), random),
        builder(emptyList(), listOf(300, 120).map { it to Activations.LiSHT }, random),
        builder(emptyList(), listOf(300, 200, 100).map { it to Activations.LiSHT }, random),
        builder(listOf(Conv(32, 4) to Activations.Mish), listOf(300 to Activations.LiSHT), random),
        builder(listOf(Conv(64, 4) to Activations.Mish), listOf(300 to Activations.LiSHT), random),
    )

    fun noConv(random: Random) = listOf(
        listOf(300, 200, 100),
        listOf(300, 300, 300),
        listOf(300, 300, 300, 120),
        listOf(300, 210, 140, 70),
        listOf(500, 400, 300, 210, 140, 70),
        listOf(600, 500, 400, 300, 200, 100),
        listOf(600, 500, 400, 300, 210, 140, 70),
        listOf(700, 600, 500, 400, 300, 210, 140, 70)
    ).flatMap { dense ->
        listOf(dense.map { it to Activations.LiSHT }, dense.map { it to Activations.Mish })
    }.map { dense ->
        builder(emptyList(), dense, random, false)
    }

    fun complex(random: Random) = listOf(
        builder(
            listOf(Conv(64, 4), Conv(32, 3)).map { it to Activations.Mish },
            listOf(300 to Activations.LiSHT),
            random,
            true
        ),
        builder(
            listOf(Conv(64, 4), Conv(32, 3)).map { it to Activations.Mish },
            listOf(300 to Activations.LiSHT),
            random,
            false
        ),
        builder(
            listOf(Conv(64, 4)).map { it to Activations.Mish },
            listOf(300, 200, 100).map { it to Activations.LiSHT },
            random,
            true
        ),
        builder(
            listOf(Conv(64, 4)).map { it to Activations.Mish },
            listOf(300, 200, 100).map { it to Activations.LiSHT },
            random,
            false
        ),
        builder(
            listOf(Conv(64, 4)).map { it to Activations.LiSHT },
            listOf(300, 200, 100).map { it to Activations.Mish },
            random,
            true
        ),
        builder(
            listOf(Conv(64, 4)).map { it to Activations.LiSHT },
            listOf(300, 200, 100).map { it to Activations.Mish },
            random,
            false
        ),
        builder(
            listOf(Conv(64, 4), Conv(32, 3)).map { it to Activations.Mish },
            listOf(300, 100).map { it to Activations.LiSHT },
            random,
            true
        ),
        builder(
            listOf(Conv(64, 4), Conv(32, 3)).map { it to Activations.Mish },
            listOf(300, 100).map { it to Activations.LiSHT },
            random,
            false
        ),
        builder(
            listOf(Conv(64, 4), Conv(64, 4)).map { it to Activations.Mish },
            listOf(300, 200, 100).map { it to Activations.LiSHT },
            random,
            true
        ),
        builder(
            listOf(Conv(64, 4), Conv(64, 4)).map { it to Activations.LiSHT },
            listOf(300, 200, 100).map { it to Activations.Mish },
            random,
            true
        ),
        builder(emptyList(), listOf(600, 500, 400, 300, 210, 140, 70).map { it to Activations.Mish }, random, false),
        builder(emptyList(), listOf(600, 500, 400, 300, 210, 140, 70).map { it to Activations.Relu6 }, random, false),
        builder(emptyList(), listOf(600, 500, 400, 300, 210, 140, 70).map { it to Activations.Relu }, random, false),
        builder(emptyList(), listOf(600, 500, 400, 300, 210, 140, 70).map { it to Activations.Swish }, random, false)
    )
}
