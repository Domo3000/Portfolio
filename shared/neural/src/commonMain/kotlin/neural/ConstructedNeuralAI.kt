package neural

import org.jetbrains.kotlinx.dl.api.core.Sequential
import org.jetbrains.kotlinx.dl.api.core.layer.Layer
import org.jetbrains.kotlinx.dl.api.core.layer.core.Input
import org.jetbrains.kotlinx.dl.api.core.layer.normalization.BatchNorm
import org.jetbrains.kotlinx.dl.api.core.layer.reshaping.Flatten
import org.jetbrains.kotlinx.dl.api.core.loss.Losses
import org.jetbrains.kotlinx.dl.api.core.metric.Metrics
import org.jetbrains.kotlinx.dl.api.core.optimizer.Adam
import java.time.Instant

class ConstructedNeuralAI(
    override val brain: Sequential,
    private val age: Int = 0,
    name: String? = null
) : NeuralAI() {
    companion object {
        fun createSequential(
            input: Input,
            conv: List<Layer>,
            dense: List<Layer>,
            output: Layer,
            batchNorm: Boolean
        ): Sequential {
            val convLayer = conv.map {
                if (batchNorm) {
                    listOf(it, BatchNorm())
                } else {
                    listOf(it)
                }
            }.flatten()

            val sequential = Sequential.of(listOf(input) + convLayer + Flatten() + dense + output)

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
        conv: List<Layer>,
        dense: List<Layer>,
        output: Layer,
        batchNorm: Boolean,
        age: Int = 0
    ) : this(createSequential(input, conv, dense, output, batchNorm), age = age)

    private val timeStamp = Instant.now().toEpochMilli()

    override val name: String = "RandomNeural(${name ?: "$timeStamp:$age"})"
}