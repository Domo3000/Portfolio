package projects.shuffle

import canvas.drawBackground
import canvas.resetDimensions
import csstype.NamedColor
import csstype.pct
import emotion.react.css
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import projects.ProjectSubState
import projects.ProjectSubStates
import react.FC
import react.Props
import react.dom.html.InputType
import react.dom.html.ReactHTML.canvas
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.input
import react.useEffect
import react.useState

private fun drawRules(canvasElement: HTMLCanvasElement, renderingContext: CanvasRenderingContext2D) {
    renderingContext.fillStyle = NamedColor.black
    val fontSize = canvasElement.height / 20

    renderingContext.font = "${fontSize}px Courier New"
    renderingContext.fillText(
        "Not implemented yet!",
        canvasElement.width / 3.0,
        canvasElement.height / 2.0 - 2.5 * fontSize
    )
}

private fun draw(canvasElement: HTMLCanvasElement, renderingContext: CanvasRenderingContext2D) {
    canvasElement.resetDimensions()
    renderingContext.drawBackground()
    drawRules(canvasElement, renderingContext)
}

val Canvas = FC<Props> {
    val elementId = "layout-canvas"

    val canvasElement by lazy { document.getElementById(elementId) as HTMLCanvasElement }
    val renderingContext: CanvasRenderingContext2D by lazy { canvasElement.getContext("2d") as CanvasRenderingContext2D }

    val (sliderState, setSliderState) = useState("52.0")

    window.addEventListener("resize", {
        draw(canvasElement, renderingContext)
    })

    input {
        type = InputType.range
        min = 2.0
        max = 100.0
        step = 1.0
        value = sliderState
        css {
            width = 100.pct
        }
        onChange = {
            setSliderState(it.target.value)
        }
    }

    canvas {
        className = Classnames.responsiveCanvas
        id = elementId
    }

    div {
        // buttons
    }

    useEffect {
        draw(canvasElement, renderingContext)
    }
}
