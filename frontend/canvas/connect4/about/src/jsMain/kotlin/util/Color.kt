package util

import about.util.ColorValue
import about.util.LimitedDescription
import about.util.colorValue
import connect4.messages.NeuralDescription
import web.cssom.Color
import web.cssom.rgb

private fun List<Int>.colorValue() = (127 + (64 * this.sumOf { it * (1 / this.size.toDouble()) })).toInt()

fun ColorValue.rgb(): Color {
    val r = listOf(c, c, k).colorValue()
    val g = listOf(m, m, k).colorValue()
    val b = listOf(y, y, k).colorValue()

    return rgb(r, g, b)
}

fun NeuralDescription.rgb(group: TrainingGroupColor) = group(
    LimitedDescription(
        this.inputType,
        this.batchNorm,
        this.conv.padding,
        this.conv.size,
        this.conv.activation,
        this.dense.size,
        this.dense.activation,
        this.outputLayer
    )
).rgb()

typealias TrainingGroupColor = (LimitedDescription) -> ColorValue

object TrainingGroupColors {
    val inputExperiment: TrainingGroupColor = { description ->
        ColorValue(
            description.convLayerSize.colorValue(),
            description.inputType.colorValue(),
            description.convLayerActivation.colorValue(),
            description.padding.colorValue()
        )
    }

    val outputExperiment: TrainingGroupColor = { description ->
        ColorValue(
            description.convLayerSize.colorValue(),
            description.output.colorValue(),
            description.convLayerActivation.colorValue(),
            description.padding.colorValue()
        )
    }


    val batchNormExperiment: TrainingGroupColor = { description ->
        ColorValue(
            description.convLayerSize.colorValue(),
            description.denseLayerSize.colorValue(),
            description.batchNorm.colorValue(),
            description.padding.colorValue()
        )
    }

    val mixedLayerExperiment: TrainingGroupColor = { description ->
        ColorValue(
            description.convLayerSize.colorValue(),
            description.denseLayerSize.colorValue(),
            description.convLayerActivation.colorValue(),
            description.denseLayerActivation.colorValue()
        )
    }


    val longTrainingExperiment: TrainingGroupColor = { description ->
        ColorValue(
            description.convLayerSize.colorValue(),
            description.denseLayerSize.colorValue(),
            description.convLayerActivation.colorValue(),
            description.padding.colorValue()
        )
    }
}