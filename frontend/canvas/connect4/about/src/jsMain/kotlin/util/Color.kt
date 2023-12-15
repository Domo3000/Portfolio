package util

import about.util.ColorValue
import about.util.LimitedDescription
import about.util.colorValue
import connect4.game.TrainingGroup
import connect4.game.TrainingGroups
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

fun NeuralDescription.rgb(group: ColoredTrainingGroup) = group.colorValue(
    LimitedDescription(
        this.inputSingular,
        this.batchNorm,
        this.conv.padding,
        this.conv.size,
        this.conv.activation,
        this.dense.size,
        this.dense.activation,
        this.outputLayer
    )
).rgb()

data class ColoredTrainingGroup(val trainingGroup: TrainingGroup, val colorValue: (LimitedDescription) -> ColorValue)

object ColoredTrainingGroups {
    val inputOutputExperiment = ColoredTrainingGroup(
        TrainingGroups.inputOutputExperiment
    ) { description ->
        ColorValue(
            description.convLayerSize.colorValue(),
            description.output.colorValue(),
            description.convLayerActivation.colorValue(),
            description.input.colorValue()
        )
    }

    val batchNormExperiment = ColoredTrainingGroup(
        TrainingGroups.batchNormExperiment
    ) { description ->
        ColorValue(
            description.convLayerSize.colorValue(),
            description.denseLayerSize.colorValue(),
            description.batchNorm.colorValue(),
            description.padding.colorValue()
        )
    }

    fun mixedLayerExperiment(trainingGroup: TrainingGroup) = ColoredTrainingGroup(
        trainingGroup
    ) { description ->
        ColorValue(
            description.convLayerSize.colorValue(),
            description.denseLayerSize.colorValue(),
            description.convLayerActivation.colorValue(),
            description.denseLayerActivation.colorValue()
        )
    }


    val longTrainingExperiment = ColoredTrainingGroup(
        TrainingGroups.longTrainingExperiment
    ) { description ->
        ColorValue(
            description.convLayerSize.colorValue(),
            description.denseLayerSize.colorValue(),
            description.convLayerActivation.colorValue(),
            description.padding.colorValue()
        )
    }

    /*
    val validPaddingExperiment = ColoredTrainingGroup(
        TrainingGroups.validPaddingExperiment
    ) { description ->
        ColorValue(
            description.convLayerSize.colorValue(),
            description.convLayerActivation.colorValue(),
            description.denseLayerSize.colorValue(),
            description.denseLayerActivation.colorValue()
        )
    }

    val longExperiment = ColoredTrainingGroup(
        TrainingGroups.longExperiment
    ) { description ->
        val allowedIndex = TrainingGroups.allowedLongExperiment.mapIndexed { index, convLayerDescription ->
            index to (
                    convLayerDescription.size == description.convLayerSize &&
                            convLayerDescription.padding == description.padding &&
                            convLayerDescription.batchNorm == description.batchNorm
                    )
        }.filter { it.second }.map { it.first }.firstOrNull() ?: (TrainingGroups.allowedLongExperiment.size / 2)

        ColorValue(
            -allowedIndex + (TrainingGroups.allowedLongExperiment.size / 2),
            description.convLayerActivation.colorValue(),
            description.denseLayerSize.colorValue(),
            description.denseLayerActivation.colorValue()
        )
    }

     */
}