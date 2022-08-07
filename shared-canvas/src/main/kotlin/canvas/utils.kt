package canvas

import csstype.NamedColor
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement

fun HTMLCanvasElement.resetBitmapSize() {
    width = getBoundingClientRect().width.toInt()
}

fun HTMLCanvasElement.resetDimensions(ratio: Double = 3.0 / 4.0) {
    resetBitmapSize()
    height = (width * ratio).toInt()
}

fun CanvasRenderingContext2D.drawBackground(borderWidth: Double = 1.0) {
    fillStyle = NamedColor.black
    fillRect(0.0, 0.0, canvas.width.toDouble(), canvas.height.toDouble())
    fillStyle = NamedColor.white
    fillRect(
        borderWidth,
        borderWidth,
        canvas.width - borderWidth * 2,
        canvas.height - borderWidth * 2
    )
}