package about.explanation

import canvas.drawBackground
import canvas.setDimensions
import connect4.game.Activation
import emotion.react.css
import props.hslColor
import react.FC
import react.Props
import react.dom.html.ReactHTML
import react.useEffect
import react.useState
import util.Button
import util.buttonRow
import web.canvas.CanvasRenderingContext2D
import web.canvas.RenderingContextId
import web.cssom.*
import web.dom.document
import web.html.HTMLCanvasElement
import web.window.WindowTarget
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.tanh

// TODO copy for OutputActivation
private external interface ActivationExplanationProps : Props {
    var title: String
    var text: String
    var link: String
}

private val ActivationExplanation = FC<ActivationExplanationProps> { props ->
    ReactHTML.h4 {
        +props.title
    }
    ReactHTML.a {
        css {
            margin = Auto.auto
        }
        href = props.link
        target = WindowTarget._blank
        +props.text
    }
}

private fun HTMLCanvasElement.drawLine(
    renderingContext: CanvasRenderingContext2D,
    number: Double,
    nextNumber: Double,
    result: Double,
    nextResult: Double
) {
    val currentX: Double = width * ((number + 3.0) / 6.0)
    val currentY: Double = height * (0.5 - (result / 6.0))
    val nextX: Double = width * ((nextNumber + 3.0) / 6.0)
    val nextY: Double = height * (0.5 - (nextResult / 6.0))

    renderingContext.strokeStyle = NamedColor.black
    renderingContext.beginPath()
    renderingContext.moveTo(currentX, currentY)
    renderingContext.lineTo(nextX, nextY)
    renderingContext.stroke()
}

val ActivationExplanations = FC<ExplanationProps> {
    val (shownActivation, setShownActivation) = useState(Activation.entries[2])

    /*
    val color = { activation: Activation ->
        CombinationNeuralDescription(
            LayerDescription(LayerSize.neutral, activation),
            LayerDescription(LayerSize.neutral, Activation.neutral)
        ).color()
    }

     */

    buttonRow {
        buttons = Activation.entries.map { activation ->
            Button(
                activation.toShortString(),
                //color(activation),
                180.hslColor(),
                shownActivation == activation
            ) {
                if (shownActivation != activation) {
                    setShownActivation(activation)
                }
            }
        }
    }

    ReactHTML.div {
        css {
            display = Display.grid
        }
        when (shownActivation) {
            Activation.Elu -> ActivationExplanation {
                title = shownActivation.name
                text = "Exponential Linear Units"
                link = "https://arxiv.org/abs/1511.07289"
            }

            Activation.Snake -> ActivationExplanation {
                title = shownActivation.name
                text = shownActivation.name
                link = "https://arxiv.org/pdf/2006.08195.pdf"
            }

            Activation.Relu -> ActivationExplanation {
                title = shownActivation.name
                text = "Rectified Linear Units"
                link = "https://arxiv.org/pdf/1803.08375.pdf"
            }

            Activation.LiSHT -> ActivationExplanation {
                title = shownActivation.name
                text = "Linearly Scaled Hyperbolic Tangent"
                link = "https://arxiv.org/abs/1901.05894"
            }

            Activation.Mish -> ActivationExplanation {
                title = shownActivation.name
                text = shownActivation.name
                link = "https://arxiv.org/abs/1908.08681"
            }
        }

        ReactHTML.canvas {
            id = "activation-canvas"
            css {
                margin = Auto.auto
                width = 33.pct
                borderStyle = LineStyle.solid
                borderWidth = LineWidth.thin
                backgroundColor = NamedColor.white
            }
        }
    }

    fun draw(function: (Double) -> Double) {
        val canvasElement = document.getElementById("activation-canvas") as? HTMLCanvasElement

        canvasElement?.let { c ->
            c.setDimensions(400, 400)
            val renderingContext = c.getContext(RenderingContextId.canvas) as CanvasRenderingContext2D
            renderingContext.drawBackground()

            (0..99).map { number ->
                val scaledNumber = (number.toDouble() / (100.0 / 6.0)) - 3.0
                val nextScaledNumber = ((number + 1).toDouble() / (100.0 / 6.0)) - 3.0

                c.drawLine(
                    renderingContext,
                    scaledNumber,
                    nextScaledNumber,
                    function(scaledNumber),
                    function(nextScaledNumber)
                )
            }
        }
    }

    useEffect(shownActivation) {
        val function: (Double) -> Double = when (shownActivation) {
            Activation.LiSHT -> { x -> // TODO fill in all functions
                x * tanh(x)
            }

            Activation.Elu -> { x ->
                if (x > 0) x else exp(x) - 1.0
            }

            Activation.Snake -> { x ->
                //x + (1 - cos(2 * frequency * x)) / (2 * frequency)
                //x / (1 + exp(-1 * x))
                x + ((1 - cos(2 * x)) / 2)
            }

            Activation.Mish -> { x ->
                x * tanh(ln(1 + exp(x)))
            }

            Activation.Relu -> { x ->
                if (x > 0.0) x else 0.0
            }
        }

        draw(function)
    }
}
