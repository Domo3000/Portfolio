package about.explanation

import about.util.LimitedDescription
import connect4.game.LayerSize
import emotion.react.css
import react.FC
import react.Props
import react.dom.html.ReactHTML
import react.useState
import util.Button
import util.TrainingGroupColors
import util.buttonRow
import util.rgb
import web.cssom.*

private data class Input(val rows: Int, val columns: Int, val dimensions: Int)

sealed interface Layer

private data object None : Layer
private data object Output : Layer
private data object BatchNorm : Layer
private data object Flatten : Layer
private data class Conv(val size: Int, val filters: Int) : Layer
private data class Dense(val size: Int) : Layer

/* TODO cleanup
sealed interface LayerType {
    val batchNorm: Boolean
}
private data class ConvLayer(override val batchNorm: Boolean, val inputType: InputType, val padding: Padding) : LayerType
private data class DenseLayer(override val batchNorm: Boolean, val inputs: Int) : LayerType
 */

private enum class LayerType {
    ConvLayerType,
    DenseLayerType
}

private external interface LayerExplanationProps : Props {
    var texts: List<String>
    var rounded: Boolean
    var width: Length?
}

private val LayerExplanation = FC<LayerExplanationProps> { props ->
    ReactHTML.div {
        css {
            float = Float.left
            width = props.width ?: 70.px
            height = 70.px
            margin = 20.px
            borderStyle = LineStyle.solid
            borderWidth = LineWidth.thin
            display = Display.grid
            if (props.rounded) {
                borderRadius = 20.px
            }
        }
        props.texts.forEach { text ->
            ReactHTML.p {
                css {
                    margin = Auto.auto
                }
                +text
            }
        }
    }
}

private external interface LayerExplanationRowProps : Props {
    var layerType: LayerType
}

private val LayerExplanationRow = FC<LayerExplanationRowProps> { props ->
    val (shownLayer, setShownLayer) = useState(LayerSize.entries[2])

    val layers = if (props.layerType == LayerType.ConvLayerType) {
        when (shownLayer) {
            LayerSize.None -> listOf(None)
            LayerSize.Small -> listOf(
                Conv(4, 32)
            )

            LayerSize.Medium -> listOf(
                Conv(4, 32),
                Conv(4, 32)
            )

            LayerSize.Large -> listOf(
                Conv(4, 64),
                Conv(3, 64)
            )

            LayerSize.Giant -> listOf(
                Conv(4, 64),
                Conv(2, 64),
                Conv(2, 64),
            )
        }
    } else {
        when (shownLayer) {
            LayerSize.None -> listOf(None)
            LayerSize.Small -> listOf(Dense(70))
            LayerSize.Medium -> listOf(Dense(350))
            LayerSize.Large -> listOf(210, 210, 210).map { Dense(it) }
            LayerSize.Giant -> listOf(420, 350, 280, 210, 140, 70).map { Dense(it) }
        }
    }

    /*
    // TODO more functional solution
    var previousOutput: Input? = null
    val inputLayerPairs = layers.map { layer ->
        val layerType = props.layerType
        val input = if (layerType == LayerType.ConvLayerType && layer is Conv) {
            val currentInput = (previousOutput ?: Input(6, 7, when(layerType.inputType) {
                InputType.SingularMinus, InputType.SingularPlus -> 1
                else -> 2
            }))

            // TODO if Valid Padding
            previousOutput = Input(currentInput.rows - layer.size + 1, currentInput.columns - layer.size + 1, layer.filters)

            currentInput
        } else {
            val input = previousOutput ?: Input((layerType as DenseLayer).inputs, 1, 1)

            previousOutput = Input((layer as Dense).size, 1, 1)

            input
        }

        layer to input
    }

     */

    buttonRow {
        buttons = LayerSize.entries.map { size ->
            Button(
                size.toShortString(),
                if (props.layerType == LayerType.ConvLayerType) {
                    TrainingGroupColors.mixedLayerExperiment(LimitedDescription(convLayerSize = size)).rgb()
                } else {
                    TrainingGroupColors.mixedLayerExperiment(LimitedDescription(denseLayerSize = size)).rgb()
                },
                shownLayer == size
            ) {
                if (shownLayer != size) {
                    setShownLayer(size)
                }
            }
        }
    }

    ReactHTML.div {
        ReactHTML.strong {
            css {
                width = 100.px
                margin = Auto.auto
            }
            +shownLayer.name
        }
    }

    ReactHTML.div {
        css {
            height = 100.px
            display = Display.flex
            alignItems = AlignItems.center
            justifyContent = JustifyContent.center
        }
        // TODO also display how outputSize and such changes depending on Padding
        // e.g. 6x7 -> 4x4 Valid -> 3x4 -> 2x2 Valid -> 2x3
        layers.forEach { layer ->
            when (layer) {
                is Conv -> LayerExplanation {
                    //texts = listOf("Input: ${input.rows}x${input.columns}x${input.dimensions}", "Size: ${layer.size}x${layer.size}", "Filters: ${layer.filters}")
                    texts = listOf("${layer.size}x${layer.size}", "${layer.filters}")
                }

                is Dense -> LayerExplanation {
                    //texts = listOf("Input: ${input.rows}", "${layer.size}")
                    texts = listOf("${layer.size}")
                    rounded = true
                }

                else -> {}
            }

            /*
            if(props.layerType.batchNorm) {
                LayerExplanation {
                    texts = listOf("BN")
                    width = 20.px
                }
            }

             */
        }
    }
}

/*
external interface ConvLayerExplanationProps : Props {
    var input: InputType
    var batchNorm: Boolean
    var padding: Padding
}

 */

val ConvLayerExplanation = FC<Props> {
    LayerExplanationRow {
        //layerType = ConvLayer(props.batchNorm, props.input, props.padding)
        layerType = LayerType.ConvLayerType
    }
}

/*
external interface DenseLayerExplanationProps : Props {
    var inputs: Int
    var batchNorm: Boolean
}

 */

val DenseLayerExplanation = FC<Props> {
    LayerExplanationRow {
        //layerType = DenseLayer(props.batchNorm, props.inputs)
        layerType = LayerType.DenseLayerType
    }
}