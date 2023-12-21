package neural

import connect4.game.InputType
import connect4.game.sizeX
import connect4.game.sizeY
import org.jetbrains.kotlinx.dl.api.core.Sequential
import org.jetbrains.kotlinx.dl.api.core.layer.Layer
import org.jetbrains.kotlinx.dl.api.core.layer.core.Input
import org.jetbrains.kotlinx.dl.api.core.layer.normalization.BatchNorm
import org.jetbrains.kotlinx.dl.api.core.layer.reshaping.Flatten
import org.jetbrains.kotlinx.dl.api.core.loss.Losses
import org.jetbrains.kotlinx.dl.api.core.metric.Metrics
import org.jetbrains.kotlinx.dl.api.core.optimizer.Adam
import java.time.Instant

private fun inputLayer(dims: Long) = Input(
    sizeX.toLong(),
    sizeY.toLong(),
    dims
)

class ConstructedNeuralAI(
    override val brain: Sequential,
    override val inputType: InputType,
    private val age: Int = 0,
    name: String? = null
) : NeuralAI() {
    companion object {
        fun createSequential(
            inputType: InputType,
            conv: List<Layer>,
            dense: List<Layer>,
            output: Layer,
            batchNorm: Boolean
        ): Sequential {
            val input = listOf(when(inputType) {
                InputType.SingularMinus, InputType.SingularPlus -> inputLayer(1L)
                else -> inputLayer(2L)
            })

            val convLayer = if (batchNorm) {
                conv.flatMap { listOf(it, BatchNorm()) }
            } else conv

            val denseLayer = if (batchNorm) {
                dense.flatMap { listOf(it, BatchNorm(axis = arrayListOf(1))) }
            } else dense

            val sequential = Sequential.of(input + convLayer + Flatten() + denseLayer + output)

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
        inputType: InputType,
        conv: List<Layer>,
        dense: List<Layer>,
        output: Layer,
        batchNorm: Boolean,
        age: Int = 0
    ) : this(createSequential(inputType, conv, dense, output, batchNorm), inputType, age = age)

    private val timeStamp = Instant.now().toEpochMilli()

    override val name: String = "RandomNeural(${name ?: "$timeStamp:$age"})"
}