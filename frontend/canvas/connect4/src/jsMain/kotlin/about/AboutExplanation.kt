package about
import connect4.messages.Activation
import connect4.messages.LayerDescription
import connect4.messages.LayerSize
import connect4.messages.NeuralDescription
import emotion.react.css
import react.FC
import react.Props
import react.dom.html.ReactHTML
import react.useState
import util.Button
import util.buttonRow
import util.color
import web.cssom.*
import web.window.WindowTarget

sealed interface Layer

private data object None : Layer
private data object Input : Layer
private data object Output : Layer
private data object BatchNorm : Layer
private data object Flatten : Layer
private data class Conv(val filters: Int, val size: Int) : Layer
private data class Dense(val size: Int) : Layer

private external interface LayerExplanationProps : Props {
    var text: String
    var rounded: Boolean
}

private val LayerExplanation = FC<LayerExplanationProps> { props ->
    ReactHTML.div {
        css {
            float = Float.left
            width = 70.px
            height = 70.px
            margin = 20.px
            borderStyle = LineStyle.solid
            borderRadius = 10.px // TODO dense rounded, conv not
            borderWidth = LineWidth.thin
        }
        +props.text
    }
}

private external interface LayerExplanationRowProps : Props {
    var conv: Boolean
}

private val LayerExplanationRow = FC<LayerExplanationRowProps> { props ->
    val (shownLayer, setShownLayer) = useState(LayerSize.entries.first())

    val color = if(props.conv) {
        NeuralDescription(
            LayerDescription(shownLayer, Activation.neutral),
            LayerDescription(LayerSize.neutral, Activation.neutral)
        ).color()
    } else {
        NeuralDescription(
            LayerDescription(LayerSize.neutral, Activation.neutral),
            LayerDescription(shownLayer, Activation.neutral)
        ).color()
    }

    val layers = if(props.conv) {
        when(shownLayer) {
            connect4.messages.LayerSize.None -> listOf(None)
            connect4.messages.LayerSize.Small -> listOf(
                Conv(32, 4)
            )
            connect4.messages.LayerSize.Medium -> listOf(
                Conv(64, 4)
            )
            connect4.messages.LayerSize.Large -> listOf(
                Conv(64, 4),
                Conv(64, 3)
            )
            connect4.messages.LayerSize.Giant -> listOf(
                Conv(128, 4),
                BatchNorm,
                Conv(64, 4),
                BatchNorm,
                Conv(32, 4)
            )
        }
    } else {
        when(shownLayer) {
            LayerSize.None -> listOf(None)
            LayerSize.Small -> listOf(Dense(210))
            LayerSize.Medium -> listOf(Dense(210), Dense(210), Dense(210))
            LayerSize.Large -> listOf(Dense(630))
            LayerSize.Giant -> listOf(Dense(630), Dense(420), Dense(210), Dense(140), Dense(70))
        }
    }

    buttonRow {
        buttons = LayerSize.entries.map { size ->
            Button(
                size.toShortString(),
                color,
                shownLayer == size
            ) {
                if (shownLayer  != size) {
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
            width = 100.pct
            margin = Auto.auto
        }
        layers.forEach {
            when (it) {
                BatchNorm -> LayerExplanation {
                    text = "BN"
                }
                is Conv -> LayerExplanation {
                    text = "${it.filters}/${it.size}x${it.size}"
                }

                is Dense -> LayerExplanation {
                    text = "${it.size}"
                }
                //Flatten -> "Flatten"
                //Input -> "Input(7x6)"
                //Output -> "Output(7)"
                //None -> null
                //else -> it.toString()
                else -> null
            }
        }
    }
}

/*
private val ConvLayerExplanation = FC<Props> {
    val (shownConvLayer, setShownConvLayer) = useState(LayerSize.entries.first())

    buttonRow {
        buttons = LayerSize.entries.map { size ->
            Button(
                size.toShortString(),
                NeuralDescription(
                    LayerDescription(size, Activation.neutral),
                    LayerDescription(LayerSize.neutral, Activation.neutral)
                ).color(),
                shownConvLayer == size
            ) {
                if (shownConvLayer  != size) {
                    setShownConvLayer(size)
                }
            }
        }
    }

    ReactHTML.div {
        ReactHTML.strong {
            css {
                margin = Auto.auto
            }
            +shownConvLayer.name
        }
    }

    LayerExplanationRow {
        layers = when(shownConvLayer) { // TODO update
            LayerSize.None -> listOf(None)
            LayerSize.Small -> listOf(
                Conv(32, 4)
            )
            LayerSize.Medium -> listOf(
                Conv(64, 4)
            )
            LayerSize.Large -> listOf(
                Conv(64, 4),
                Conv(64, 3)
            )
            LayerSize.Giant -> listOf(
                Conv(128, 4),
                BatchNorm,
                Conv(64, 4),
                BatchNorm,
                Conv(32, 4)
            )
        }
    }
}

private val DenseLayerExplanation = FC<Props> {
    LayerSize.entries.forEach {
        ReactHTML.summary {
            +it.name
            ReactHTML.details {
                LayerExplanationRow {
                    layers = when(it) {
                        LayerSize.None -> listOf(None)
                        LayerSize.Small -> listOf(Dense(210))
                        LayerSize.Medium -> listOf(Dense(210), Dense(210), Dense(210))
                        LayerSize.Large -> listOf(Dense(630))
                        LayerSize.Giant -> listOf(Dense(630), Dense(420), Dense(210), Dense(140), Dense(70))
                    }
                }
            }
        }
    }
}

 */

private val ActivationExplanation = FC<Props> { props ->
    Activation.entries.forEach {
        ReactHTML.summary {
            +it.name
            ReactHTML.details {
                when(it) {
                    Activation.Elu -> {
                        ReactHTML.a {
                            href = "https://arxiv.org/abs/1901.05894"
                            target = WindowTarget._blank
                            +"Non-Parameteric Linearly Scaled Hyperbolic Tangent Activation Function"
                        }
                    }
                    Activation.Swish -> "TODO() url"
                    Activation.Relu -> "TODO() url"
                    Activation.LiSHT -> {
                        ReactHTML.a {
                            href = "https://arxiv.org/abs/1901.05894"
                            target = WindowTarget._blank
                            +"Non-Parameteric Linearly Scaled Hyperbolic Tangent Activation Function"
                        }
                    }
                    Activation.Mish -> "TODO() url"
                }
            }
        }
    }
}

val AboutExplanation = FC<Props> {
    ReactHTML.div {
        ReactHTML.details {
            ReactHTML.summary {
                ReactHTML.strong {
                    +"Convolutional"
                }
            }
            LayerExplanationRow {
                conv = true
            }
        }

        ReactHTML.details {
            ReactHTML.summary {
                ReactHTML.strong {
                    +"Dense"
                }
            }
            LayerExplanationRow {
                conv = false
            }
        }

        ReactHTML.h3 {
            +"Activations"
        }
        ActivationExplanation {}
    }
}