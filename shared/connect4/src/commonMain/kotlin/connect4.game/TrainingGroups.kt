package connect4.game

import connect4.messages.NeuralDescription

sealed interface TrainingGroup {
    val max: Int
    val input: List<InputType>
    val batchNorm: List<Boolean>
    val padding: List<Padding>
    val convLayerSize: List<LayerSize>
    val convLayerActivation: List<Activation>
    val output: List<OutputActivation>
    fun specificContains(description: NeuralDescription): Boolean

    fun commonContains(description: NeuralDescription): Boolean {
        if (!input.contains(description.inputType)) return false

        if (!batchNorm.contains(description.batchNorm)) return false

        if (description.conv.size != LayerSize.None && !padding.contains(description.conv.padding)) return false

        if (!convLayerSize.contains(description.conv.size)) return false

        if (description.conv.size != LayerSize.None && !convLayerActivation.contains(description.conv.activation)) return false

        if (!output.contains(description.outputLayer)) return false

        return true
    }

    fun contains(description: NeuralDescription): Boolean = commonContains(description) && specificContains(description)
}

data class CombinationGroup(
    override val max: Int,
    override val input: List<InputType>,
    override val batchNorm: List<Boolean>,
    override val padding: List<Padding>,
    override val convLayerSize: List<LayerSize>,
    override val convLayerActivation: List<Activation>,
    val denseLayerSize: List<LayerSize>,
    val denseLayerActivation: List<Activation>,
    override val output: List<OutputActivation>
) : TrainingGroup {
    override fun specificContains(description: NeuralDescription): Boolean {
        if (!denseLayerSize.contains(description.dense.size)) return false

        if (description.dense.size != LayerSize.None && !denseLayerActivation.contains(description.dense.activation)) return false

        return true
    }
}

data class SameActivationGroup(
    override val max: Int,
    override val input: List<InputType>,
    override val batchNorm: List<Boolean>,
    override val padding: List<Padding>,
    override val convLayerSize: List<LayerSize>,
    override val convLayerActivation: List<Activation>,
    val denseLayerSize: List<LayerSize>,
    override val output: List<OutputActivation>
) : TrainingGroup {
    override fun specificContains(description: NeuralDescription): Boolean {
        if (!denseLayerSize.contains(description.dense.size)) return false

        if (description.dense.size != LayerSize.None && !convLayerActivation.contains(description.dense.activation)) return false

        if (description.conv.size != LayerSize.None
            && description.dense.size != LayerSize.None
            && description.conv.activation != description.dense.activation
        ) return false

        return true
    }
}

data class SameLayerGroup(
    override val max: Int,
    override val input: List<InputType>,
    override val batchNorm: List<Boolean>,
    override val padding: List<Padding>,
    override val convLayerSize: List<LayerSize>,
    override val convLayerActivation: List<Activation>,
    override val output: List<OutputActivation>
) : TrainingGroup {
    override fun specificContains(description: NeuralDescription): Boolean {
        if (description.conv.size != description.dense.size) return false

        if (description.conv.activation != description.dense.activation) return false

        return true
    }
}

object TrainingGroups {
    val inputExperiment = SameLayerGroup(
        304,
        InputType.entries,
        listOf(false),
        Padding.entries,
        LayerSize.entries,
        Activation.entries,
        listOf(OutputActivation.Linear)
    )

    val outputExperiment = SameLayerGroup(
        304,
        listOf(InputType.DualNeutral),
        listOf(false),
        Padding.entries,
        LayerSize.entries,
        Activation.entries,
        OutputActivation.entries
    )

    val batchNormExperiment = CombinationGroup(
        504,
        listOf(InputType.DualNeutral),
        listOf(true, false),
        Padding.entries,
        LayerSize.entries,
        listOf(Activation.Relu),
        LayerSize.entries,
        listOf(Activation.Relu),
        listOf(OutputActivation.Linear)
    )

    val mixedLayerExperiment = CombinationGroup(
        304,
        listOf(InputType.DualNeutral),
        listOf(false),
        listOf(Padding.Valid),
        LayerSize.entries,
        Activation.entries,
        LayerSize.entries,
        Activation.entries,
        listOf(OutputActivation.Linear)
    )

    val sameLayerExperiment = CombinationGroup(
        204,
        listOf(InputType.DualNeutral),
        listOf(false),
        listOf(Padding.Same),
        LayerSize.entries,
        listOf(Activation.LiSHT, Activation.Mish, Activation.Relu),
        LayerSize.entries,
        listOf(Activation.LiSHT, Activation.Mish, Activation.Relu),
        listOf(OutputActivation.Linear)
    )

    val longTrainingExperiment = SameActivationGroup(
        1004,
        listOf(InputType.DualNeutral),
        listOf(false),
        Padding.entries,
        LayerSize.entries,
        Activation.entries,
        LayerSize.entries,
        listOf(OutputActivation.Linear)
    )

    val all = listOf(
        inputExperiment,
        outputExperiment,
        batchNormExperiment,
        mixedLayerExperiment,
        sameLayerExperiment,
        longTrainingExperiment
    )
}