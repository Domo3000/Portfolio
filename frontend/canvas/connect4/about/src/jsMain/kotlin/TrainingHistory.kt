package about

import about.util.LimitedDescription
import canvas.clear
import canvas.drawCircle
import canvas.resetDimensions
import connect4.game.*
import connect4.game.Padding
import connect4.messages.TrainingResultMessage
import css.Classes
import emotion.react.css
import react.FC
import react.Props
import react.dom.html.ReactHTML
import react.useEffect
import react.useState
import util.Button
import util.ColoredTrainingGroup
import util.buttonRow
import util.rgb
import web.canvas.CanvasRenderingContext2D
import web.canvas.RenderingContextId
import web.cssom.*
import web.dom.document
import web.html.HTMLCanvasElement
import kotlin.math.pow

private fun List<Double>.getOrElse(index: Int, defaultValue: Double) = this.getOrElse(index) { _ -> defaultValue }

private fun List<Double>.averaged(factor: Int): List<Double> = if (factor == 0) {
    this
} else {
    this.mapIndexed { i, n ->
        (listOf(n) + ((1..factor).map { offset ->
            listOf(this.getOrElse(i - offset, n), this.getOrElse(i + offset, n))
        }).flatten()).average()
    }
}

private fun List<Double>.averaged(factor: Int, smoothness: Int): List<Double> {
    var list = this
    repeat(smoothness) {
        list = list.averaged(factor)
    }
    return list
}

private fun List<Boolean>.toTriple() = Triple(this[0], this[1], this[2])
private fun Triple<Boolean, Boolean, Boolean>.toList() = listOf(first, second, third)

private fun HTMLCanvasElement.drawPoint(
    renderingContext: CanvasRenderingContext2D,
    relativeX: Double,
    relativeY: Double,
    color: Color,
) {
    val absoluteX: Double = width * relativeX
    val absoluteY: Double = height * relativeY

    renderingContext.drawCircle(absoluteX, absoluteY, 4.0, color)
}

private fun HTMLCanvasElement.drawLine(
    renderingContext: CanvasRenderingContext2D,
    round: Int,
    count: Int,
    color: Color,
    result: Double,
    previousResult: Double
) {
    val previousX: Double = width * (round / count.toDouble())
    val previousY: Double = height * (1.0 - previousResult)
    val currentX: Double = previousX + (width / count.toDouble())
    val currentY: Double = height * (1.0 - result)

    renderingContext.strokeStyle = color
    renderingContext.beginPath()
    renderingContext.moveTo(previousX, previousY)
    renderingContext.lineTo(currentX, currentY)
    renderingContext.stroke()
}

enum class TrainingView {
    History,
    Time
}

external interface ChoiceButtonRowProps<T> : Props {
    var choices: List<Pair<String, T>>
    var color: (T) -> Color
    var shown: List<T>
    var setState: (List<T>) -> Unit
}

fun <T> ChoiceButtonRow() = FC<ChoiceButtonRowProps<T>> { props ->
    if (props.choices.size > 1) {
        buttonRow {
            buttons = props.choices.map { (label, choice) ->
                Button(
                    label,
                    props.color(choice),
                    props.shown.contains(choice),
                ) {
                    if (props.shown.contains(choice)) {
                        props.setState(props.shown.filterNot { it == choice })
                    } else {
                        props.setState(props.shown + choice)
                    }
                }
            }
        }
    }
}

private external interface LegendProps : Props {
    var leftText: String
    var middleText: String
    var rightText: String
}

private val Legend = FC<LegendProps> { props ->
    ReactHTML.div {
        css {
            display = Display.flex
            alignItems = AlignItems.flexStart
            justifyContent = JustifyContent.spaceBetween
            textAlign = TextAlign.center
        }
        ReactHTML.p {
            +props.leftText
        }
        ReactHTML.p {
            +props.middleText
        }
        ReactHTML.p {
            +props.rightText
        }
    }
}

external interface TrainingHistoryProps : Props {
    var results: List<TrainingResultMessage>
    var group: ColoredTrainingGroup
    var view: TrainingView
}

val TrainingHistory = FC<TrainingHistoryProps> { props ->
    val (canvasElement, setCanvasElement) = useState<HTMLCanvasElement?>(null)
    val (renderingContext, setRenderingContext) = useState<CanvasRenderingContext2D?>(null)
    val (groupId, setGroupId) = useState(0)
    val (shownInputSingular, setShownInputSingular) = useState<List<Boolean>>(emptyList())
    val (shownBatchNorm, setShownBatchNorm) = useState<List<Boolean>>(emptyList())
    val (shownPadding, setShownPadding) = useState<List<Padding>>(emptyList())
    val (shownModes, setShownModes) = useState(Triple(false, false, false))
    val (shownConvLayerSizes, setShownConvLayerSizes) = useState<List<LayerSize>>(emptyList())
    val (shownConvActivations, setShownConvActivations) = useState<List<Activation>>(emptyList())
    val (shownDenseLayerSizes, setShownDenseLayerSizes) = useState<List<LayerSize>>(emptyList())
    val (shownDenseActivations, setShownDenseActivations) = useState<List<Activation>>(emptyList())
    val (shownOutput, setShownOutput) = useState<List<OutputActivation>>(emptyList())

    fun filterResults(): List<TrainingResultMessage> {

        val group = when (props.group.trainingGroup) {
            is CombinationGroup -> CombinationGroup(
                1,
                shownInputSingular,
                shownBatchNorm,
                shownPadding,
                shownConvLayerSizes,
                shownConvActivations,
                shownDenseLayerSizes,
                shownDenseActivations,
                shownOutput
            )

            is SameLayerGroup -> SameLayerGroup(
                1,
                shownInputSingular,
                shownBatchNorm,
                shownPadding,
                shownConvLayerSizes,
                shownConvActivations,
                shownOutput
            )

            is SameActivationGroup -> SameActivationGroup(
                1,
                shownInputSingular,
                shownBatchNorm,
                shownPadding,
                shownConvLayerSizes,
                shownConvActivations,
                shownDenseLayerSizes,
                shownOutput
            )
        }

        return props.results.filter { (description, _) ->
            group.contains(description)
        }
    }

    // TODO better name
    fun drawLines() {
        (1 until 10).forEach {
            val relativeY = 1.0 - ((0.1 * it).pow(pow) * scale)
            canvasElement?.let { c ->
                renderingContext?.let { r ->
                    //r.strokeStyle = NamedColor.lightgrey
                    r.strokeStyle = rgb(0, 0, 0, 0.5)
                    r.beginPath()
                    r.moveTo(0, c.height * relativeY)
                    r.lineTo(c.width, c.height * relativeY)
                    r.stroke()
                }
            }
        }
    }

    fun drawTimeState() {
        val upTo = (filterResults().maxOfOrNull { it.trainingTime } ?: 0.0) * 1.05
        filterResults().map {
            Triple(
                it.description,
                it.trainingTime,
                (it.results.takeLast(10).average() + it.results.max()) / 2.0
            )
        }
            .forEach { (description, time, score) ->
                val relativeX = time / upTo
                val relativeY = 1.0 - (score.pow(pow) * scale)

                canvasElement!!.drawPoint(
                    renderingContext!!,
                    relativeX,
                    relativeY,
                    description.rgb(props.group)
                )
            }
    }

    fun drawHistoryState() {
        filterResults().map { it.description to it.results }
            .forEach { (description, scores) ->
                val smooth = (listOf(0.0) + scores)
                    //.shorten(200, margin)
                    .averaged(margin, 1)
                    .map { it.pow(pow) * scale }
                    .averaged(margin, 1)
                    .dropLast(margin)

                val count = smooth.size - 1

                smooth.drop(1).zip(smooth.dropLast(1))
                    .mapIndexed { round, (result, previousResult) ->
                        canvasElement!!.drawLine(
                            renderingContext!!,
                            round,
                            count,
                            description.rgb(props.group),
                            result,
                            previousResult
                        )
                    }
            }
    }

    fun draw() {
        canvasElement?.let {
            it.resetDimensions()
            renderingContext!!.clear()
            drawLines()
            if (props.view == TrainingView.Time) {
                drawTimeState()
            } else {
                drawHistoryState()
            }
        }
    }

    val trainingGroup = props.group.trainingGroup
    val colorValue = props.group.colorValue

    if (groupId != trainingGroup.hashCode()) {
        setGroupId(trainingGroup.hashCode())
    }

    ChoiceButtonRow<Boolean>()() {
        choices = trainingGroup.input.map {
            when (it) {
                true -> "1D Input" to it
                false -> "2D Input" to it
            }
        }
        color = { colorValue(LimitedDescription(input = it)).rgb() }
        shown = shownInputSingular
        setState = { setShownInputSingular(it) }
    }

    ChoiceButtonRow<Boolean>()() {
        choices = trainingGroup.batchNorm.map {
            when (it) {
                true -> "BatchNorm" to it
                false -> "No" to it
            }
        }
        color = { colorValue(LimitedDescription(batchNorm = it)).rgb() }
        shown = shownBatchNorm
        setState = { setShownBatchNorm(it) }
    }

    ChoiceButtonRow<Padding>()() {
        choices = trainingGroup.padding.map {
            when (it) {
                Padding.Same -> "Same Padding" to it
                Padding.Valid -> "Valid Padding" to it
            }
        }
        color = { colorValue(LimitedDescription(padding = it)).rgb() }
        shown = shownPadding
        setState = { setShownPadding(it) }
    }

    ChoiceButtonRow<LayerSize>()() {
        choices = trainingGroup.convLayerSize.map { it.toString() to it }
        color = { colorValue(LimitedDescription(convLayerSize = it)).rgb() }
        shown = shownConvLayerSizes
        setState = { setShownConvLayerSizes(it) }
    }

    ChoiceButtonRow<Activation>()() {
        choices = trainingGroup.convLayerActivation.map { it.toString() to it }
        color = { colorValue(LimitedDescription(convLayerActivation = it)).rgb() }
        shown = shownConvActivations
        setState = { setShownConvActivations(it) }
    }

    when (trainingGroup) {
        is CombinationGroup -> {
            ChoiceButtonRow<LayerSize>()() {
                choices = trainingGroup.denseLayerSize.map { it.toString() to it }
                color = { colorValue(LimitedDescription(denseLayerSize = it)).rgb() }
                shown = shownDenseLayerSizes
                setState = { setShownDenseLayerSizes(it) }
            }

            ChoiceButtonRow<Activation>()() {
                choices = trainingGroup.denseLayerActivation.map { it.toString() to it }
                color = { colorValue(LimitedDescription(denseLayerActivation = it)).rgb() }
                shown = shownDenseActivations
                setState = { setShownDenseActivations(it) }
            }
        }


        is SameActivationGroup -> {
            ChoiceButtonRow<LayerSize>()() {
                choices = trainingGroup.denseLayerSize.map { it.toString() to it }
                color = { colorValue(LimitedDescription(denseLayerSize = it)).rgb() }
                shown = shownDenseLayerSizes
                setState = { setShownDenseLayerSizes(it) }
            }
        }

        is SameLayerGroup -> {}
    }

    ChoiceButtonRow<OutputActivation>()() {
        choices = trainingGroup.output.map {
            "$it Output" to it
        }
        color = { colorValue(LimitedDescription(output = it)).rgb() }
        shown = shownOutput
        setState = { setShownOutput(it) }
    }

    ReactHTML.canvas {
        css(Classes.canvas)
        id = "external-canvas"
    }

    if (filterResults().isNotEmpty()) {
        val (leftText, middleText, rightText) = if (props.view == TrainingView.Time) {
            val max = filterResults().maxOf { it.trainingTime }

            Triple("0.0s", "${(max / 2.0).toString().take(4)}s", "${max.toString().take(4)}s")
        } else {
            val max = filterResults().maxOf { it.results.size - margin }
            Triple("0", "${max / 2.0}", "$max")
        }

        Legend {
            this.leftText = leftText
            this.middleText = middleText
            this.rightText = rightText
        }
    }

    useEffect(groupId) {
        val canvas = document.getElementById("external-canvas") as HTMLCanvasElement
        setCanvasElement(canvas)
        setRenderingContext(canvas.getContext(RenderingContextId.canvas) as CanvasRenderingContext2D)

        props.group.trainingGroup.let { group ->
            setShownInputSingular(group.input)
            setShownBatchNorm(group.batchNorm)
            setShownPadding(group.padding)
            setShownConvLayerSizes(group.convLayerSize)
            setShownConvActivations(group.convLayerActivation)
            setShownOutput(group.output)

            when (group) {
                is CombinationGroup -> {
                    setShownDenseLayerSizes(group.denseLayerSize)
                    setShownDenseActivations(group.denseLayerActivation)
                }

                is SameActivationGroup -> {
                    setShownDenseLayerSizes(group.denseLayerSize)
                }

                is SameLayerGroup -> {}
            }
        }
    }

    useEffect {
        draw()
    }
}
