package canvas

import csstype.NamedColor
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement

fun HTMLCanvasElement.getElementWidth(size: Int, lineWidth: Double = 1.0) = (width - 2 * lineWidth) / size

fun HTMLCanvasElement.getX(x: Double, size: Int, lineWidth: Double = 1.0) =
    ((x - lineWidth) / getElementWidth(size, lineWidth)).toInt()

fun HTMLCanvasElement.getRelativeX(x: Int, size: Int, lineWidth: Double = 1.0) =
    lineWidth + x * getElementWidth(size, lineWidth)

fun HTMLCanvasElement.getElementHeight(size: Int, lineWidth: Double = 1.0) = (height - 2 * lineWidth) / size

fun HTMLCanvasElement.getY(y: Double, size: Int, lineWidth: Double = 1.0) =
    ((y - lineWidth) / getElementHeight(size, lineWidth)).toInt()

fun HTMLCanvasElement.getRelativeY(y: Int, size: Int, lineWidth: Double = 1.0) =
    lineWidth + y * getElementHeight(size, lineWidth)

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