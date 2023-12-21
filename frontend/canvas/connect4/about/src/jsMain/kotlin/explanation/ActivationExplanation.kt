package about.explanation

import about.util.LimitedDescription
import canvas.drawBackground
import canvas.setDimensions
import connect4.game.Activation
import connect4.game.OutputActivation
import emotion.react.css
import react.*
import react.dom.html.ReactHTML
import util.Button
import util.TrainingGroupColors
import util.buttonRow
import util.rgb
import web.canvas.CanvasRenderingContext2D
import web.canvas.RenderingContextId
import web.cssom.*
import web.dom.document
import web.html.HTMLCanvasElement
import web.window.WindowTarget
import kotlin.math.*

private const val offset = 3.0

private external interface ActivationExplanationProps : Props {
    var title: String
    var text: String?
    var link: String?
}

private val ActivationExplanation = FC<ActivationExplanationProps> { props ->
    ReactHTML.h4 {
        +props.title
    }
    props.link?.let { link ->
        ReactHTML.a {
            css {
                margin = Auto.auto
            }
            href = link
            target = WindowTarget._blank
            +(props.text ?: props.title)
        }
    }
}

private fun HTMLCanvasElement.drawLine(
    renderingContext: CanvasRenderingContext2D,
    number: Double,
    nextNumber: Double,
    result: Double,
    nextResult: Double
) {
    val currentX: Double = width * ((number + offset) / (offset * 2.0))
    val currentY: Double = height * (0.5 - (result / (offset * 2.0)))
    val nextX: Double = width * ((nextNumber + offset) / (offset * 2.0))
    val nextY: Double = height * (0.5 - (nextResult / (offset * 2.0)))

    renderingContext.strokeStyle = NamedColor.black
    renderingContext.beginPath()
    renderingContext.moveTo(currentX, currentY)
    renderingContext.lineTo(nextX, nextY)
    renderingContext.stroke()
}

private fun scaledNumber(number: Double) = (number / (100.0 / (offset * 2.0))) - offset

private fun HTMLCanvasElement.draw(function: (Double) -> Double) {
        this.setDimensions(400, 400)
        val renderingContext = this.getContext(RenderingContextId.canvas) as CanvasRenderingContext2D
        renderingContext.drawBackground()

        (0..99).map { number ->
            val scaledNumber = (number.toDouble() / (100.0 / (offset * 2.0))) - offset
            val nextScaledNumber = ((number + 1).toDouble() / (100.0 / (offset * 2.0))) - offset

            this.drawLine(
                renderingContext,
                scaledNumber(number.toDouble()),
                scaledNumber((number + 1).toDouble()),
                function(scaledNumber),
                function(nextScaledNumber)
            )
        }
}

val ActivationExplanations = FC<Props> {
    val (canvasElement, setCanvasElement) = useState<HTMLCanvasElement?>(null)
    val (shownActivation, setShownActivation) = useState(Activation.entries[2])

    val function: (Double) -> Double = when (shownActivation) {
        Activation.LiSHT -> { x ->
            x * tanh(x)
        }

        Activation.Elu -> { x ->
            if (x > 0) x else exp(x) - 1.0
        }

        Activation.Snake -> { x ->
            x + ((1 - cos(2 * x)) / 2)
        }

        Activation.Mish -> { x ->
            x * tanh(ln(1 + exp(x)))
        }

        Activation.Relu -> { x ->
            if (x > 0.0) x else 0.0
        }
    }

    buttonRow {
        buttons = Activation.entries.map { activation ->
            Button(
                activation.toShortString(),
                color = TrainingGroupColors.mixedLayerExperiment(LimitedDescription(convLayerActivation = activation)).rgb(),
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

    useEffect(shownActivation) {
        canvasElement?.draw(function)
    }

    useEffectOnce {
        val canvas = document.getElementById("activation-canvas") as HTMLCanvasElement
        setCanvasElement(canvas)
        canvasElement?.draw(function)
    }
}


val OutputActivationExplanations = FC<Props> {
    val (canvasElement, setCanvasElement) = useState<HTMLCanvasElement?>(null)
    val (shownOutputActivation, setShownOutputActivation) = useState(OutputActivation.entries[2])

    val function: (Double) -> Double = when (shownOutputActivation) {
        OutputActivation.Linear -> { x ->
            x
        }
        OutputActivation.Relu -> { x ->
            if (x > 0.0) x else 0.0
        }
        OutputActivation.Sigmoid -> { x ->
            (1.0 / (1.0 + exp(-x))) * offset
        }
        OutputActivation.Softmax -> { x ->

            (exp(x) / ((0..99).sumOf { exp(scaledNumber(it.toDouble())) })) * offset * offset
        }
        OutputActivation.Tanh -> { x ->
            sinh(x) / cosh(x)
        }
    }

    buttonRow {
        buttons = OutputActivation.entries.map { activation ->
            Button(
                activation.toShortString(),
                color = TrainingGroupColors.outputExperiment(LimitedDescription(output = activation)).rgb(),
                shownOutputActivation == activation
            ) {
                if (shownOutputActivation != activation) {
                    setShownOutputActivation(activation)
                }
            }
        }
    }

    ReactHTML.div {
        css {
            display = Display.grid
        }
        when (shownOutputActivation) { // TODO links
            OutputActivation.Linear -> ActivationExplanation {
                title = shownOutputActivation.name
            }
            OutputActivation.Relu -> ActivationExplanation {
                title = shownOutputActivation.name
                text = "Rectified Linear Units"
                link = "https://arxiv.org/pdf/1803.08375.pdf"
            }
            OutputActivation.Sigmoid -> ActivationExplanation {
                title = shownOutputActivation.name
                text = shownOutputActivation.name
                link = "https://humphryscomputing.com/Notes/Neural/sigmoid.html"
            }
            OutputActivation.Softmax -> ActivationExplanation {
                title = shownOutputActivation.name
            }
            OutputActivation.Tanh -> ActivationExplanation {
                title = shownOutputActivation.name
            }
        }

        ReactHTML.canvas {
            id = "output-activation-canvas"
            css {
                margin = Auto.auto
                width = 33.pct
                borderStyle = LineStyle.solid
                borderWidth = LineWidth.thin
                backgroundColor = NamedColor.white
            }
        }
    }

    useEffect(shownOutputActivation) {
        canvasElement?.draw(function)
    }

    useEffectOnce {
        val canvas = document.getElementById("output-activation-canvas") as HTMLCanvasElement
        setCanvasElement(canvas)
        canvasElement?.draw(function)
    }
}
