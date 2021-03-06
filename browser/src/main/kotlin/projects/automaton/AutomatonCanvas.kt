package projects.automaton

import Classnames
import canvas.drawBackground
import canvas.resetDimensions
import csstype.NamedColor
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import react.FC
import react.Props
import react.dom.html.ReactHTML.canvas
import react.dom.html.ReactHTML.div
import react.useEffect

private fun drawRules(canvasElement: HTMLCanvasElement, renderingContext: CanvasRenderingContext2D) {
    renderingContext.fillStyle = NamedColor.black
    val fontSize = canvasElement.height / 20

    renderingContext.font = "${fontSize}px Courier New"
    renderingContext.fillText("Not implemented yet!", canvasElement.width / 3.0, canvasElement.height / 2.0 - 2.5 * fontSize)
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

    window.addEventListener("resize", {
        draw(canvasElement, renderingContext)
    })

    div {
        canvas {
            className = Classnames.responsiveCanvas
            id = elementId
        }
    }

    useEffect {
        draw(canvasElement, renderingContext)
    }
}
