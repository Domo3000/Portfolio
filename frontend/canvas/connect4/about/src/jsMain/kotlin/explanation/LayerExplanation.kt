package about.explanation

import about.util.LimitedDescription
import connect4.game.LayerSize
import emotion.react.css
import props.hslColor
import react.FC
import react.Props
import react.dom.html.ReactHTML
import react.useState
import util.Button
import util.buttonRow
import util.rgb
import web.cssom.*

sealed interface Layer

private data object None : Layer
private data object Input : Layer
private data object Output : Layer
private data object BatchNorm : Layer
private data object Flatten : Layer
private data class Conv(val filters: Int, val size: Int) : Layer
private data class Dense(val size: Int) : Layer

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

external interface LayerExplanationRowProps : Props {
    var conv: Boolean
}

val LayerExplanationRow = FC<LayerExplanationRowProps> { props ->
    val (shownLayer, setShownLayer) = useState(LayerSize.entries[2])

    /*
    val color = { layer: LayerSize ->
        if (props.conv) {
            CombinationNeuralDescription(
                LayerDescription(layer, Activation.neutral),
                LayerDescription(LayerSize.neutral, Activation.neutral)
            ).color()
        } else {
            CombinationNeuralDescription(
                LayerDescription(LayerSize.neutral, Activation.neutral),
                LayerDescription(layer, Activation.neutral)
            ).color()
        }
    }

     */

    val layers = if (props.conv) {
        when (shownLayer) {
            LayerSize.None -> listOf(None)
            LayerSize.Small -> listOf(
                Conv(64, 4)
            )

            LayerSize.Medium -> listOf(
                Conv(64, 4),
                Conv(64, 3)
            )

            LayerSize.Large -> listOf(
                Conv(64, 4),
                BatchNorm,
                Conv(64, 4)
            )

            LayerSize.Giant -> listOf(
                Conv(64, 4),
                Conv(64, 3),
                Conv(64, 2),
            )
        }
    } else {
        when (shownLayer) {
            LayerSize.None -> listOf(None)
            LayerSize.Small -> listOf(Dense(70))
            LayerSize.Medium -> listOf(Dense(420))
            LayerSize.Large -> listOf(Dense(210), Dense(210), Dense(210))
            LayerSize.Giant -> listOf(
                420,
                350,
                280,
                210,
                140
            ).map { Dense(it) }
        }
    }

    buttonRow {
        buttons = LayerSize.entries.map { size ->
            Button(
                size.toShortString(),
                180.hslColor(),
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
        layers.forEach {
            when (it) {
                BatchNorm -> LayerExplanation {
                    texts = listOf("BN")
                    width = 20.px
                }

                is Conv -> LayerExplanation {
                    texts = listOf("${it.filters}", "${it.size}x${it.size}")
                }

                is Dense -> LayerExplanation {
                    texts = listOf("${it.size}")
                    rounded = true
                }

                else -> {}
            }
        }
    }
}

val ConvLayerExplanation = FC<ExplanationProps> { props ->
    val (shownLayer, setShownLayer) = useState(LayerSize.entries[2])

    /*
    val color = { layer: LayerSize ->
        if (props.conv) {
            CombinationNeuralDescription(
                LayerDescription(layer, Activation.neutral),
                LayerDescription(LayerSize.neutral, Activation.neutral)
            ).color()
        } else {
            CombinationNeuralDescription(
                LayerDescription(LayerSize.neutral, Activation.neutral),
                LayerDescription(layer, Activation.neutral)
            ).color()
        }
    }

     */

    val layers = when (shownLayer) {
            LayerSize.None -> listOf(None)
            LayerSize.Small -> listOf(
                Conv(64, 4)
            )

            LayerSize.Medium -> listOf(
                Conv(64, 4),
                Conv(64, 3)
            )

            LayerSize.Large -> listOf(
                Conv(64, 4),
                BatchNorm,
                Conv(64, 4)
            )

            LayerSize.Giant -> listOf(
                Conv(64, 4),
                Conv(64, 3),
                Conv(64, 2),
            )
        }

    buttonRow {
        buttons = LayerSize.entries.map { size ->
            Button(
                size.toShortString(),
                props.group.colorValue(LimitedDescription(convLayerSize = size)).rgb(),
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
        layers.forEach {
            when (it) {
                BatchNorm -> LayerExplanation {
                    texts = listOf("BN")
                    width = 20.px
                }

                is Conv -> LayerExplanation {
                    texts = listOf("${it.filters}", "${it.size}x${it.size}")
                }

                is Dense -> LayerExplanation {
                    texts = listOf("${it.size}")
                    rounded = true
                }

                else -> {}
            }
        }
    }
}
