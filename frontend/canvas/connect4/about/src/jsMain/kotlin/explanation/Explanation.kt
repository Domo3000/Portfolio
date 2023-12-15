package about.explanation

import connect4.game.CombinationGroup
import emotion.react.css
import react.FC
import react.Props
import react.dom.html.ReactHTML
import util.ColoredTrainingGroup
import web.cssom.Clear

external interface ExplanationProps : Props {
    var group: ColoredTrainingGroup
}

val Explanation = FC<ExplanationProps> { props ->
    val shown: List<Triple<String, FC<ExplanationProps>, (CombinationGroup) -> Boolean>> =
        listOf<Triple<String, FC<ExplanationProps>, (CombinationGroup) -> Boolean>>(
            Triple("Input Layer", ConvLayerExplanation) { group -> group.input.size > 1 },
            Triple("Convolutional Layers", ConvLayerExplanation) { true },
            Triple("Dense Layers", ConvLayerExplanation) { true },
            Triple("Activation Functions", ActivationExplanations) { group ->
                group.convLayerActivation.size > 1 || group.denseLayerActivation.size > 1
            },
        ).filter { it.third(props.group.trainingGroup as CombinationGroup) } // TODO fix

    shown.forEach {
        ReactHTML.details {
            css {
                clear = Clear.left
            }
            ReactHTML.summary {
                ReactHTML.strong {
                    +it.first
                }
            }
            it.second {
                group = props.group
            }
        }
    }
}
