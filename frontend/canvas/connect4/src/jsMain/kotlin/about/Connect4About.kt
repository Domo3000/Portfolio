package about

import Requests
import canvas.ExternalCanvas
import canvas.clear
import canvas.resetDimensions
import canvas.setDimensions
import connect4.messages.*
import css.Classes
import emotion.react.css
import react.*
import react.dom.html.ReactHTML
import util.Button
import util.buttonRow
import util.color
import web.canvas.CanvasRenderingContext2D
import web.canvas.RenderingContextId
import web.events.Event
import web.html.HTMLCanvasElement
import kotlin.math.pow

private fun List<Double>.shorten(description: NeuralDescription, withMargin: Boolean): List<Double> {
    val margin = if(withMargin) 3 else 0
    return when (description.conv.size) {
        LayerSize.Giant -> this.take(100 + margin)
        else -> this.take(200 + margin)
    }
}

private fun List<Double>.averaged(factor: Int = 2): List<Double> {
    val minusOne = this.drop(1) + this.takeLast(1)
    val minusTwo = this.drop(2) + this.takeLast(2)
    val minusThree = this.drop(3) + this.takeLast(3)
    val plusOne = listOf(0.0) + this.dropLast(1)
    val plusTwo = listOf(0.0, 0.0) + this.dropLast(2)
    val plusThree = listOf(0.0, 0.0, 0.0) + this.dropLast(3)

    return this.mapIndexed { i, n ->
        when(factor) {
            1 -> listOf(minusOne[i], n, plusOne[i])
            2 -> listOf(minusTwo[i], minusOne[i], n, plusOne[i], plusTwo[i])
            else -> listOf(minusThree[i], minusTwo[i], minusOne[i], n, plusOne[i], plusTwo[i], plusThree[i])
        }.average()
    }
}

private fun HTMLCanvasElement.drawLine(
    round: Int,
    description: NeuralDescription,
    result: Double,
    previousResult: Double
) {
    val renderingContext = getContext(RenderingContextId.canvas) as CanvasRenderingContext2D

    val color = description.color()
    val scale = 1.2345679 // TODO adjust by highest value, like 1 / highest^2 (currently 0.851)

    val previousX: Double = width * (round / 200.0)
    val previousY: Double = height * (1.0 - previousResult * scale)
    val currentX: Double = previousX + (width / 200.0)
    val currentY: Double = height * (1.0 - result * scale)

    renderingContext.strokeStyle = color
    renderingContext.beginPath()
    renderingContext.moveTo(previousX, previousY)
    renderingContext.lineTo(currentX, currentY)
    renderingContext.stroke()
}

private class TrainingHistoryHolder {
    val results = mutableListOf<Pair<NeuralDescription, List<Double>>>()
}

class Connect4About : ExternalCanvas() {
    override val name: String = "Connect4About"

    override val component: FC<Props>
        get() = FC {
            val (trainingHistoryHolder, _) = useState(TrainingHistoryHolder())
            val (shownConvLayerSizes, setShownConvLayerSizes) = useState(LayerSize.entries.toList())
            val (shownDenseLayerSizes, setShownDenseLayerSizes) = useState(LayerSize.entries.toList())
            val (shownConvActivations, setShownConvActivations) = useState(Activation.entries.toList())
            val (shownDenseActivations, setShownDenseActivations) = useState(Activation.entries.toList())

            fun drawState() {
                trainingHistoryHolder.results.filter { (description, _) ->
                    run {
                        val convCheck = shownConvLayerSizes.contains(description.conv.size) &&
                                (shownConvActivations.contains(description.conv.activation) || (description.conv.size == LayerSize.None))

                        val denseCheck = shownDenseLayerSizes.contains(description.dense.size) &&
                                (shownDenseActivations.contains(description.dense.activation) || (description.dense.size == LayerSize.None))

                        convCheck && denseCheck
                    }
                }.forEach { (description, scores) ->
                    val smooth = scores
                        .shorten(description, true)
                        .map { it.pow(2.0) }
                        .averaged().averaged().averaged() // best so far
                        .shorten(description, false)
                    //val smooth = scores.map { it.pow(2.0) }.averaged(3).averaged(2).averaged(1)
                    //val smooth = scores.map { it.pow(2.0) }.averaged(1).averaged(2).averaged(3)
                    smooth.zip(listOf(0.0) + smooth.dropLast(1))
                        .mapIndexed { round, (result, previousResult) ->
                            canvasElement.drawLine(
                                round,
                                description,
                                result,
                                previousResult
                            )
                        }
                }
            }

            fun draw() {
                canvasElement.resetDimensions(1.0)
                renderingContext.clear()
                drawState()
            }

            val resizeHandler: (Event) -> Unit = {
                draw()
            }

            ReactHTML.div {
                css(Classes.project)

                ReactHTML.h2 { // TODO everywhere
                    +"Connect Four"
                }

                // TrainingExplanation: ~5000 moves per training - 25 epochs => 1 million moves per 8 rounds
                AboutExplanation {}

                buttonRow {
                    buttons = LayerSize.entries.map { size ->
                        Button(
                            size.toShortString(),
                            NeuralDescription(
                                LayerDescription(size, Activation.neutral),
                                LayerDescription(LayerSize.neutral, Activation.neutral)
                            ).color(),
                            shownConvLayerSizes.contains(size)
                        ) {
                            if (shownConvLayerSizes.contains(size)) {
                                setShownConvLayerSizes(shownConvLayerSizes.filterNot { it == size })
                            } else {
                                setShownConvLayerSizes(shownConvLayerSizes + size)
                            }
                        }
                    }
                }

                buttonRow {
                    buttons = Activation.entries.map { activation ->
                        Button(
                            activation.toShortString(),
                            NeuralDescription(
                                LayerDescription(LayerSize.neutral, activation),
                                LayerDescription(LayerSize.neutral, Activation.neutral)
                            ).color(),
                            shownConvActivations.contains(activation)
                        ) {
                            if (shownConvActivations.contains(activation)) {
                                setShownConvActivations(shownConvActivations.filterNot { it == activation })
                            } else {
                                setShownConvActivations(shownConvActivations + activation)
                            }
                        }
                    }
                }

                buttonRow {
                    buttons = LayerSize.entries.map { size ->
                        Button(
                            size.toShortString(),
                            NeuralDescription(
                                LayerDescription(LayerSize.neutral, Activation.neutral),
                                LayerDescription(size, Activation.neutral)
                            ).color(),
                            shownDenseLayerSizes.contains(size)
                        ) {
                            if (shownDenseLayerSizes.contains(size)) {
                                setShownDenseLayerSizes(shownDenseLayerSizes.filterNot { it == size })
                            } else {
                                setShownDenseLayerSizes(shownDenseLayerSizes + size)
                            }
                        }
                    }
                }

                buttonRow {
                    buttons = Activation.entries.map { activation ->
                        Button(
                            activation.toShortString(),
                            NeuralDescription(
                                LayerDescription(LayerSize.neutral, Activation.neutral),
                                LayerDescription(LayerSize.neutral, activation)
                            ).color(),
                            shownDenseActivations.contains(activation)
                        ) {
                            if (shownDenseActivations.contains(activation)) {
                                setShownDenseActivations(shownDenseActivations.filterNot { it == activation })
                            } else {
                                setShownDenseActivations(shownDenseActivations + activation)
                            }
                        }
                    }
                }

                ReactHTML.canvas {
                    css(Classes.canvas)
                    id = canvasId
                }
            }

            useEffect(shownConvLayerSizes) {
                draw()
            }

            useEffect(shownConvActivations) {
                draw()
            }

            useEffect(shownDenseLayerSizes) {
                draw()
            }

            useEffect(shownDenseActivations) {
                draw()
            }

            useEffectOnce {
                canvasElement.setDimensions()
                addEventListener("resize" to resizeHandler)
                draw()
                Requests.get("/static/training.json") { response ->
                    (Connect4Messages.decode(response) as? TrainingResultsMessage)?.let {
                        trainingHistoryHolder.results.addAll(it.results)
                        draw()
                    }
                }
            }
        }

    override fun cleanUp() {}

    override fun initialize() {}

    init {
        initEventListeners()
    }
}