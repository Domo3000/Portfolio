package connect4.ai.neural

import org.jetbrains.kotlinx.dl.api.core.activation.Activations
import org.jetbrains.kotlinx.dl.api.core.initializer.Constant
import org.jetbrains.kotlinx.dl.api.core.initializer.GlorotNormal
import org.jetbrains.kotlinx.dl.api.core.initializer.RandomNormal
import org.jetbrains.kotlinx.dl.api.core.initializer.Zeros
import org.jetbrains.kotlinx.dl.api.core.layer.convolutional.ConvPadding
import org.jetbrains.kotlinx.dl.api.core.loss.Losses
import org.jetbrains.kotlinx.dl.api.core.metric.Metrics
import kotlin.random.Random

object PredefinedNeurals {
    private fun simpleConv(input: Boolean, conv: Int, moves: List<Move>, random: Random): RandomNeuralAI = RandomNeuralAI(
        training = moves,
        inputType = input,
        conv = listOf(
            NeuralAIFactory.conv2D(conv, 4, Activations.LiSHT, GlorotNormal(random.nextLong()), Zeros())
        ),
        dense = emptyList(),
        output = NeuralAIFactory.dense(
            7,
            Activations.Linear,
            GlorotNormal(random.nextLong()),
            Constant(0.5f)
        ),
        losses = Losses.SOFT_MAX_CROSS_ENTROPY_WITH_LOGITS,
        metrics = Metrics.ACCURACY
    )

    private fun simpleConvDense(input: Boolean, conv: Int, dense: Int, moves: List<Move>, random: Random): RandomNeuralAI =
        RandomNeuralAI(
            training = moves,
            inputType = input,
            conv = listOf(
                NeuralAIFactory.conv2D(conv, 4, Activations.LiSHT, GlorotNormal(random.nextLong()), Zeros())
            ),
            dense = listOf(
                NeuralAIFactory.dense(
                    dense,
                    Activations.HardSigmoid,
                    GlorotNormal(random.nextLong()),
                    RandomNormal(0.0f, 0.25f)
                ),
            ),
            output = NeuralAIFactory.dense(
                7,
                Activations.Linear,
                GlorotNormal(random.nextLong()),
                Constant(0.5f)
            ),
            losses = Losses.SOFT_MAX_CROSS_ENTROPY_WITH_LOGITS,
            metrics = Metrics.ACCURACY
        )

    private fun conv21conv7(input: Boolean, moves: List<Move>, random: Random): RandomNeuralAI =
        RandomNeuralAI(
            training = moves,
            inputType = input,
            conv = listOf(
                NeuralAIFactory.conv2D(
                    21,
                    4,
                    Activations.LiSHT,
                    GlorotNormal(random.nextLong()),
                    RandomNormal(0.0f, 0.2f)
                ),
                NeuralAIFactory.conv2D(
                    7,
                    3,
                    Activations.LiSHT,
                    GlorotNormal(random.nextLong()),
                    RandomNormal(0.0f, 0.2f)
                )
            ),
            dense = listOf(
                NeuralAIFactory.dense(
                    21,
                    Activations.HardSigmoid,
                    GlorotNormal(random.nextLong()),
                    RandomNormal(0.0f, 0.2f)
                ),
            ),
            output = NeuralAIFactory.dense(
                7,
                Activations.Linear,
                GlorotNormal(random.nextLong()),
                Constant(0.5f)
            ),
            losses = Losses.SOFT_MAX_CROSS_ENTROPY_WITH_LOGITS,
            metrics = Metrics.ACCURACY
        )

    private fun conv32max2D(input: Boolean, moves: List<Move>, random: Random): RandomNeuralAI =
        RandomNeuralAI(
            training = moves,
            inputType = input,
            conv = listOf(
                NeuralAIFactory.conv2D(
                    32,
                    3,
                    Activations.Tanh,
                    GlorotNormal(random.nextLong()),
                    RandomNormal(0.0f, 0.5f)
                ),
                NeuralAIFactory.max2D(2, 1, ConvPadding.SAME)
            ),
            dense = listOf(
                NeuralAIFactory.dense(
                    105,
                    Activations.Gelu,
                    GlorotNormal(random.nextLong()),
                    RandomNormal(0.0f, 0.5f)
                )
            ),
            output = NeuralAIFactory.dense(
                7,
                Activations.Linear,
                GlorotNormal(random.nextLong()),
                RandomNormal(0.0f, 0.5f)
            ),
            losses = Losses.SOFT_MAX_CROSS_ENTROPY_WITH_LOGITS,
            metrics = Metrics.ACCURACY
        )

    private fun convAvg(input: Boolean, moves: List<Move>, random: Random): RandomNeuralAI =
        RandomNeuralAI(
            training = moves,
            inputType = input,
            conv = listOf(
                NeuralAIFactory.avg2D(2, 2, ConvPadding.SAME),
            ),
            dense = listOf(
                NeuralAIFactory.dense(
                    180,
                    Activations.Softmax,
                    GlorotNormal(random.nextLong()),
                    Constant(0.5f)
                ),
                NeuralAIFactory.dense(
                    49,
                    Activations.Elu,
                    GlorotNormal(random.nextLong()),
                    Constant(0.5f)
                )
            ),
            output = NeuralAIFactory.dense(
                7,
                Activations.Linear,
                GlorotNormal(random.nextLong()),
                Constant(0.5f)
            ),
            losses = Losses.SOFT_MAX_CROSS_ENTROPY_WITH_LOGITS,
            metrics = Metrics.ACCURACY
        )

    fun basic(moves: List<Move>, random: Random) = listOf(
        simpleConv(true, 3, moves, random),
        simpleConv(false, 3, moves, random),
        simpleConv(true, 32, moves, random),
        simpleConv(false, 32, moves, random),
        simpleConvDense(true, 3, 28, moves, random),
        simpleConvDense(false, 3, 28, moves, random),
        simpleConvDense(true, 3, 74, moves, random),
        simpleConvDense(false, 3, 74, moves, random),
        simpleConvDense(true, 32, 294, moves, random),
        simpleConvDense(false, 32, 294, moves, random),
        conv21conv7(true, moves, random),
        conv21conv7(false, moves, random),
        conv32max2D(true, moves, random),
        conv32max2D(false, moves, random),
        convAvg(false, moves, random),
        convAvg(false, moves, random),
    )

    private fun conv6432(input: Boolean, moves: List<Move>, random: Random): RandomNeuralAI =
        RandomNeuralAI(
            training = moves,
            inputType = input,
            conv = listOf(
                NeuralAIFactory.conv2D(
                    64,
                    4,
                    Activations.Tanh,
                    GlorotNormal(random.nextLong()),
                    RandomNormal(0.0f, 0.5f)
                ),
                NeuralAIFactory.conv2D(
                    32,
                    3,
                    Activations.Tanh,
                    GlorotNormal(random.nextLong()),
                    RandomNormal(0.0f, 0.5f)
                )
            ),
            dense = listOf(
                NeuralAIFactory.dense(
                    64,
                    Activations.Exponential,
                    GlorotNormal(random.nextLong()),
                    RandomNormal(0.0f, 0.5f)
                )
            ),
            output = NeuralAIFactory.dense(
                7,
                Activations.Relu,
                GlorotNormal(random.nextLong()),
                Constant(0.5f)
            ),
            losses = Losses.SOFT_MAX_CROSS_ENTROPY_WITH_LOGITS,
            metrics = Metrics.ACCURACY
        )

    private fun conv643216(input: Boolean, moves: List<Move>, random: Random): RandomNeuralAI =
        RandomNeuralAI(
            training = moves,
            inputType = input,
            conv = listOf(
                NeuralAIFactory.conv2D(
                    64,
                    3,
                    Activations.Tanh,
                    GlorotNormal(random.nextLong()),
                    RandomNormal(0.0f, 0.5f)
                ),
                NeuralAIFactory.conv2D(
                    32,
                    3,
                    Activations.Tanh,
                    GlorotNormal(random.nextLong()),
                    RandomNormal(0.0f, 0.5f)
                ),
                NeuralAIFactory.conv2D(
                    16,
                    2,
                    Activations.Tanh,
                    GlorotNormal(random.nextLong()),
                    RandomNormal(0.0f, 0.5f)
                )
            ),
            dense = listOf(
                NeuralAIFactory.dense(
                    32,
                    Activations.Exponential,
                    GlorotNormal(random.nextLong()),
                    RandomNormal(0.0f, 0.5f)
                )
            ),
            output = NeuralAIFactory.dense(
                7,
                Activations.Relu,
                GlorotNormal(random.nextLong()),
                Constant(0.5f)
            ),
            losses = Losses.SOFT_MAX_CROSS_ENTROPY_WITH_LOGITS,
            metrics = Metrics.ACCURACY
        )

    private fun convDouble643216(input: Boolean, moves: List<Move>, random: Random): RandomNeuralAI =
        RandomNeuralAI(
            training = moves,
            inputType = input,
            conv = (0..1).map {
                NeuralAIFactory.conv2D(
                    64,
                    3,
                    Activations.LiSHT,
                    GlorotNormal(random.nextLong()),
                    RandomNormal(0.0f, 0.5f)
                )
            } + (0..1).map {
                NeuralAIFactory.conv2D(
                    32,
                    3,
                    Activations.LiSHT,
                    GlorotNormal(random.nextLong()),
                    RandomNormal(0.0f, 0.5f)
                )
            } + (0..1).map {
                NeuralAIFactory.conv2D(
                    16,
                    3,
                    Activations.LiSHT,
                    GlorotNormal(random.nextLong()),
                    RandomNormal(0.0f, 0.5f)
                )
            } + NeuralAIFactory.conv2D(
                3,
                1,
                Activations.LiSHT,
                GlorotNormal(random.nextLong()),
                RandomNormal(0.0f, 0.5f)
            ),
            dense = listOf(
                NeuralAIFactory.dense(64, Activations.Linear, GlorotNormal(random.nextLong()), Constant(0.5f))
            ),
            output = NeuralAIFactory.dense(
                7,
                Activations.Relu,
                GlorotNormal(random.nextLong()),
                Constant(0.5f)
            ),
            losses = Losses.SOFT_MAX_CROSS_ENTROPY_WITH_LOGITS,
            metrics = Metrics.ACCURACY
        )

    private fun maxConvAvgSimple(input: Boolean, moves: List<Move>, random: Random): RandomNeuralAI =
        RandomNeuralAI(
            training = moves,
            inputType = input,
            conv = listOf(
                NeuralAIFactory.max2D(
                    3,
                    1,
                    ConvPadding.VALID
                ),
                NeuralAIFactory.conv2D(
                    64,
                    3,
                    Activations.SoftSign,
                    GlorotNormal(random.nextLong()),
                    RandomNormal(0.0f, 0.5f)
                ),
                NeuralAIFactory.avg2D(
                    3,
                    1,
                    ConvPadding.VALID
                )
            ),
            dense = listOf(
                NeuralAIFactory.dense(
                    49,
                    Activations.Exponential,
                    GlorotNormal(random.nextLong()),
                    RandomNormal(0.0f, 0.5f)
                ),
            ),
            output = NeuralAIFactory.dense(
                7,
                Activations.Relu,
                GlorotNormal(random.nextLong()),
                Constant(0.5f)
            ),
            losses = Losses.SOFT_MAX_CROSS_ENTROPY_WITH_LOGITS,
            metrics = Metrics.ACCURACY
        )

    private fun maxConvAvg(input: Boolean, moves: List<Move>, random: Random): RandomNeuralAI =
        RandomNeuralAI(
            training = moves,
            inputType = input,
            conv = listOf(
                NeuralAIFactory.max2D(
                    2,
                    1,
                    ConvPadding.VALID
                ),
                NeuralAIFactory.conv2D(
                    64,
                    2,
                    Activations.SoftSign,
                    GlorotNormal(random.nextLong()),
                    RandomNormal(0.0f, 0.5f)
                ),
                NeuralAIFactory.avg2D(
                    3,
                    1,
                    ConvPadding.VALID
                )
            ),
            dense = listOf(
                NeuralAIFactory.dense(
                    256,
                    Activations.Exponential,
                    GlorotNormal(random.nextLong()),
                    RandomNormal(0.0f, 0.5f)
                ),
                NeuralAIFactory.dense(
                    64,
                    Activations.TanhShrink,
                    GlorotNormal(random.nextLong()),
                    RandomNormal(0.0f, 0.5f)
                ),
                NeuralAIFactory.dense(
                    28,
                    Activations.Tanh,
                    GlorotNormal(random.nextLong()),
                    RandomNormal(0.0f, 0.5f)
                ),
            ),
            output = NeuralAIFactory.dense(
                7,
                Activations.Relu,
                GlorotNormal(random.nextLong()),
                Constant(0.5f)
            ),
            losses = Losses.SOFT_MAX_CROSS_ENTROPY_WITH_LOGITS,
            metrics = Metrics.ACCURACY
        )

    private fun convAvg3216(input: Boolean, moves: List<Move>, random: Random): RandomNeuralAI =
        RandomNeuralAI(
            training = moves,
            inputType = input,
            conv = listOf(
                NeuralAIFactory.avg2D(
                    4,
                    1,
                    ConvPadding.SAME
                ),
                NeuralAIFactory.conv2D(
                    32,
                    3,
                    Activations.Tanh,
                    GlorotNormal(random.nextLong()),
                    RandomNormal(0.0f, 0.5f)
                ),
                NeuralAIFactory.conv2D(
                    16,
                    2,
                    Activations.Tanh,
                    GlorotNormal(random.nextLong()),
                    RandomNormal(0.0f, 0.5f)
                )
            ),
            dense = listOf(
                NeuralAIFactory.dense(
                    32,
                    Activations.Exponential,
                    GlorotNormal(random.nextLong()),
                    RandomNormal(0.0f, 0.5f)
                )
            ),
            output = NeuralAIFactory.dense(
                7,
                Activations.Relu,
                GlorotNormal(random.nextLong()),
                Constant(0.5f)
            ),
            losses = Losses.SOFT_MAX_CROSS_ENTROPY_WITH_LOGITS,
            metrics = Metrics.ACCURACY
        )

    fun complex(moves: List<Move>, random: Random) = listOf(
        conv6432(true, moves, random),
        conv6432(false, moves, random),
        conv643216(true, moves, random),
        conv643216(false, moves, random),
        convDouble643216(true, moves, random),
        convDouble643216(false, moves, random),
        maxConvAvgSimple(true, moves, random),
        maxConvAvgSimple(false, moves, random),
        maxConvAvg(true, moves, random),
        maxConvAvg(false, moves, random),
        convAvg3216(true, moves, random),
        convAvg3216(false, moves, random)
    )
}