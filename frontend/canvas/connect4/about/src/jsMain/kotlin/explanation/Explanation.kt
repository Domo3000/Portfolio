package about.explanation

import connect4.game.InputType
import connect4.game.TrainingGroup
import react.FC
import react.Props
import react.dom.html.ReactHTML
import react.useState

external interface ExplanationProps : Props {
    var group: TrainingGroup // TODO remove, always show all
}

val Explanation = FC<ExplanationProps> {
    val (shownInput, setShownInput) = useState(InputType.entries[2])

    /*
     TODO this could be refactored
     decide if they should be filtered by third or if it should just be a Pair
     */
    /*
    val shown: List<Triple<String, FC<ExplanationProps>, (TrainingGroup) -> Boolean>> =
        listOf(
            Triple("Input Layer", InputExplanation) { group -> group.input.size > 1 },
            Triple("BatchNorm", ConvLayerExplanation) { group -> group.batchNorm.size > 1 },
            Triple("Padding", ConvLayerExplanation) { group -> group.padding.size > 1 }, // TODO move to Conv
            Triple("Convolutional Layers", ConvLayerExplanation) { group -> group.convLayerSize.size > 1 },
            Triple("Dense Layers", DenseLayerExplanation) { group -> group.convLayerSize.size > 1 },
            Triple("Activation Functions", ActivationExplanations) { group -> group.convLayerActivation.size > 1 },
            Triple("Output Activation Functions", OutputActivationExplanations) { group -> group.output.size > 1 },
        )

     */

    listOf(
        "Input Layer" to {
            InputExplanation {
                this.shownInput = shownInput
                this.setShownInput = { setShownInput(it) }
            }
        },
        "Convolutional Layers" to {
            ConvLayerExplanation {
                //input = shownInput
                //batchNorm = true
                //padding = Padding.Valid
            }
        },
        "Dense Layers" to {
            DenseLayerExplanation {
                //inputs = 1000
                //batchNorm = false
            }
        }
    ).forEach {
        ReactHTML.div {
            ReactHTML.h3 {
                +it.first
            }
            it.second()
        }
    }
}
