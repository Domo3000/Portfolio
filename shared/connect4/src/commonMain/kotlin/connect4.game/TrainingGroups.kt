package connect4.game

import connect4.messages.NeuralDescription

sealed interface TrainingGroup {
    val max: Int
    val input: List<Boolean>
    val batchNorm: List<Boolean>
    val padding: List<Padding>
    val convLayerSize: List<LayerSize>
    val convLayerActivation: List<Activation>
    val output: List<OutputActivation>
    fun specificContains(description: NeuralDescription): Boolean

    fun commonContains(description: NeuralDescription): Boolean {
        if (!input.contains(description.inputSingular)) return false

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
    override val input: List<Boolean>,
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
    override val input: List<Boolean>,
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
    override val input: List<Boolean>,
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
    /*
        val inputOutputExperiment = ComplexGroup(
            304,
            listOf(true, false),
            listOf(true),
            listOf(Padding.Valid),
            Triple(false, false, true),
            LayerSize.entries,
            Activation.entries,
            OutputActivation.entries
        )

        val batchNormExperiment = ComplexGroup(
            504,
            listOf(false),
            listOf(true, false),
            Padding.entries,
            Triple(true, true, true),
            LayerSize.entries.filterNot { it == LayerSize.None },
            Activation.entries,
            listOf(OutputActivation.Linear)
        )

        val validPaddingExperiment = LimitedCombinationGroup(
            304,
            listOf(false),
            listOf(true, false),
            listOf(Padding.Valid),
            LayerSize.entries.filterNot { it == LayerSize.None },
            Activation.entries,
            OutputActivation.entries
        )

        val samePaddingExperiment = LimitedCombinationGroup(
            304,
            listOf(false),
            listOf(true, false),
            listOf(Padding.Same),
            LayerSize.entries.filterNot { it == LayerSize.None },
            Activation.entries,
            OutputActivation.entries
        )

        val mixedLayerExperiment = CombinationGroup(
            304,
            listOf(false),
            listOf(false),
            listOf(Padding.Valid),
            LayerSize.entries,
            Activation.entries,
            LayerSize.entries,
            Activation.entries,
            listOf(OutputActivation.Linear)
        )

        val validLayerExperiment = CombinationGroup(
            304,
            listOf(false),
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
            listOf(false),
            listOf(false),
            listOf(Padding.Same),
            LayerSize.entries,
            listOf(Activation.LiSHT, Activation.Mish, Activation.Relu),
            LayerSize.entries,
            listOf(Activation.LiSHT, Activation.Mish, Activation.Relu),
            listOf(OutputActivation.Linear)
        )


     */
    /*
    val longTrainingExperiment = LimitedCombinationGroup(
        1004,
        listOf(false),
        listOf(false),
        Padding.entries,
        LayerSize.entries,
        listOf(Activation.LiSHT, Activation.Mish, Activation.Relu),
        listOf(OutputActivation.Linear, OutputActivation.Relu, OutputActivation.Tanh)
    )

     */

    /*
        val longTrainingExperiment = ComplexGroup(
            1004,
            listOf(false),
            listOf(false),
            Padding.entries,
            Triple(true, true, true),
            LayerSize.entries,
            listOf(Activation.LiSHT, Activation.Mish, Activation.Relu),
            listOf(OutputActivation.Linear, OutputActivation.Relu, OutputActivation.Tanh)
        )


     */
    /*
    val validPaddingExperiment = CombinationGroup(
        304,
        listOf(false),
        listOf(true),
        listOf(Padding.Valid),
        LayerSize.entries.filterNot { it == LayerSize.None },
        Activation.entries,
        LayerSize.entries,
        Activation.entries,
        listOf(OutputActivation.Linear)
    )

    val allowedLongExperiment = listOf(
        ConvLayerDescription(
            size = LayerSize.Giant, padding = Padding.Valid, batchNorm = true
        ), ConvLayerDescription(
            size = LayerSize.Large, padding = Padding.Valid, batchNorm = true
        ), ConvLayerDescription(
            size = LayerSize.Medium, padding = Padding.Valid, batchNorm = true
        ), ConvLayerDescription(
            size = LayerSize.Small, padding = Padding.Valid, batchNorm = true
        ), ConvLayerDescription(
            size = LayerSize.Medium, padding = Padding.Same, batchNorm = false
        )
    )

    // TODO F-LRSF-LR-L, F-GRSF-GR-L, F-GRST-GR-L, F-LRST-GR-L, F-LRSF-GR-L
    // and whatevers better BN/N and all valid/Conv combinations
    // TODO add: Small Valid BN
    val longExperiment = SpecificGroup(
        1004,
        allowedLongExperiment,
        listOf(Activation.LiSHT, Activation.Relu),
        listOf(LayerSize.Large, LayerSize.Giant),
        listOf(Activation.LiSHT, Activation.Relu)
    )

     */

    val inputOutputExperiment = SameLayerGroup(
        304,
        listOf(true, false),
        listOf(true),
        listOf(Padding.Valid),
        LayerSize.entries,
        Activation.entries,
        OutputActivation.entries
    )

    val batchNormExperiment = CombinationGroup(
        504,
        listOf(false),
        listOf(true, false),
        Padding.entries,
        LayerSize.entries,
        listOf(Activation.Relu),
        LayerSize.entries,
        listOf(Activation.Relu),
        listOf(OutputActivation.Linear)
    )

    val validLayerExperiment = CombinationGroup(
        304,
        listOf(false),
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
        listOf(false),
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
        listOf(false),
        listOf(false),
        Padding.entries,
        LayerSize.entries,
        listOf(Activation.LiSHT, Activation.Mish, Activation.Relu),
        LayerSize.entries,
        listOf(OutputActivation.Linear)
    )

    val all = listOf(
        inputOutputExperiment,
        batchNormExperiment,
        validLayerExperiment,
        sameLayerExperiment,
        longTrainingExperiment
    )
}